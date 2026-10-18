package java.util.concurrent

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.AbstractQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual fun interface Executor {
    actual fun execute(command: Runnable)
}

actual interface ExecutorService : Executor {
    actual fun shutdown()
    actual fun shutdownNow(): List<Runnable>
    actual fun isShutdown(): Boolean
    actual fun isTerminated(): Boolean
    actual fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    actual fun <T> submit(task: Callable<T>): Future<T>
    actual fun <T> submit(task: Runnable, result: T): Future<T>
    actual fun submit(task: Runnable): Future<*>
    actual fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>
    actual fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>
    actual fun <T> invokeAny(tasks: Collection<Callable<T>>): T
    actual fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T
}

actual abstract class AbstractExecutorService actual constructor() : ExecutorService {
    actual override fun <T> submit(task: Callable<T>): Future<T> {
        val ftask = FutureTask(task)
        execute(ftask)
        return ftask
    }

    actual override fun <T> submit(task: Runnable, result: T): Future<T> {
        val ftask = FutureTask(task, result)
        execute(ftask)
        return ftask
    }

    actual override fun submit(task: Runnable): Future<*> {
        val ftask = FutureTask<Any?>(task, null)
        execute(ftask)
        return ftask
    }

    actual override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        val futures = ArrayList<Future<T>>(tasks.size)
        var done = false
        try {
            for (t in tasks) {
                val f = FutureTask(t)
                futures.add(f)
                execute(f)
            }
            for (f in futures) {
                if (!f.isDone()) {
                    try {
                        f.get()
                    } catch (_: CancellationException) {
                    } catch (_: ExecutionException) {
                    }
                }
            }
            done = true
            return futures
        } finally {
            if (!done) {
                for (f in futures) f.cancel(true)
            }
        }
    }

    actual override fun <T> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): List<Future<T>> {
        val nanos = unit.toNanos(timeout)
        val deadline = System.nanoTime() + nanos
        val futures = ArrayList<Future<T>>(tasks.size)
        var done = false
        try {
            for (t in tasks) {
                futures.add(FutureTask(t))
            }
            for (f in futures) {
                if (System.nanoTime() >= deadline) return futures
                execute(f as Runnable)
            }
            for (f in futures) {
                if (!f.isDone()) {
                    val rem = deadline - System.nanoTime()
                    if (rem <= 0L) return futures
                    try {
                        f.get(rem, TimeUnit.NANOSECONDS)
                    } catch (_: CancellationException) {
                    } catch (_: ExecutionException) {
                    } catch (_: TimeoutException) {
                        return futures
                    }
                }
            }
            done = true
            return futures
        } finally {
            if (!done) {
                for (f in futures) f.cancel(true)
            }
        }
    }

    actual override fun <T> invokeAny(tasks: Collection<Callable<T>>): T {
        if (tasks.isEmpty()) throw IllegalArgumentException()
        val ecs = ExecutorCompletionService<T>(this)
        val futures = ArrayList<Future<T>>(tasks.size)
        var lastEx: ExecutionException? = null
        try {
            for (t in tasks) {
                futures.add(ecs.submit(t))
            }
            var remaining = futures.size
            while (remaining > 0) {
                val f = ecs.take()
                remaining--
                try {
                    return f.get()
                } catch (e: ExecutionException) {
                    lastEx = e
                } catch (re: RuntimeException) {
                    lastEx = ExecutionException(re)
                }
            }
            throw lastEx ?: ExecutionException(null as Throwable?)
        } finally {
            for (f in futures) f.cancel(true)
        }
    }

    actual override fun <T> invokeAny(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): T {
        if (tasks.isEmpty()) throw IllegalArgumentException()
        val deadline = System.nanoTime() + unit.toNanos(timeout)
        val ecs = ExecutorCompletionService<T>(this)
        val futures = ArrayList<Future<T>>(tasks.size)
        var lastEx: ExecutionException? = null
        try {
            for (t in tasks) {
                futures.add(ecs.submit(t))
            }
            var remaining = futures.size
            while (remaining > 0) {
                val rem = deadline - System.nanoTime()
                if (rem <= 0L) throw TimeoutException()
                val f = ecs.poll(rem, TimeUnit.NANOSECONDS) ?: throw TimeoutException()
                remaining--
                try {
                    return f.get()
                } catch (e: ExecutionException) {
                    lastEx = e
                } catch (re: RuntimeException) {
                    lastEx = ExecutionException(re)
                }
            }
            throw lastEx ?: ExecutionException(null as Throwable?)
        } finally {
            for (f in futures) f.cancel(true)
        }
    }
}

actual interface CompletionService<V> {
    actual fun submit(task: Callable<V>): Future<V>
    actual fun submit(task: Runnable, result: V): Future<V>
    actual fun take(): Future<V>
    actual fun poll(): Future<V>?
    actual fun poll(timeout: Long, unit: TimeUnit): Future<V>?
}

actual open class ExecutorCompletionService<V> : CompletionService<V> {
    private val executor: Executor
    private val completionQueue: BlockingQueue<Future<V>>

    actual constructor(executor: Executor) : this(executor, LinkedBlockingQueue())

    actual constructor(executor: Executor, completionQueue: BlockingQueue<Future<V>>) {
        this.executor = executor
        this.completionQueue = completionQueue
    }

    private fun wrapAndExecute(ftask: FutureTask<V>): Future<V> {
        executor.execute(Runnable {
            try {
                ftask.run()
            } finally {
                completionQueue.add(ftask)
            }
        })
        return ftask
    }

    actual override fun submit(task: Callable<V>): Future<V> {
        return wrapAndExecute(FutureTask(task))
    }

    actual override fun submit(task: Runnable, result: V): Future<V> {
        return wrapAndExecute(FutureTask(task, result))
    }

    actual override fun take(): Future<V> = completionQueue.take()

    actual override fun poll(): Future<V>? = completionQueue.poll()

    actual override fun poll(timeout: Long, unit: TimeUnit): Future<V>? =
        completionQueue.poll(timeout, unit)
}

actual fun interface ThreadFactory {
    actual fun newThread(r: Runnable): Thread
}

actual open class ThreadPoolExecutor actual constructor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    private val threadFactory: ThreadFactory
) : AbstractExecutorService() {
    private var isShutdownState = false

    actual override fun execute(command: Runnable) {
        if (isShutdownState) throw RejectedExecutionException("Executor is shutdown")
        threadFactory.newThread(command).start()
    }

    actual override fun <T> submit(task: Callable<T>): Future<T> = super.submit(task)

    actual override fun <T> submit(task: Runnable, result: T): Future<T> = super.submit(task, result)

    actual override fun submit(task: Runnable): Future<*> = super.submit(task)

    actual override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return isShutdownState
    }

    actual override fun shutdown() {
        isShutdownState = true
    }

    actual override fun shutdownNow(): List<Runnable> {
        isShutdownState = true
        return emptyList()
    }

    actual override fun isShutdown(): Boolean = isShutdownState
    actual override fun isTerminated(): Boolean = isShutdownState
}

actual interface TransferQueue<E> : BlockingQueue<E> {
    actual fun tryTransfer(e: E): Boolean
    actual fun transfer(e: E)
    actual fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean
    actual fun hasWaitingConsumer(): Boolean
    actual fun getWaitingConsumerCount(): Int
}

private class TransferSlot<E>(var item: E? = null, var fulfilled: Boolean = false)

actual open class LinkedTransferQueue<E> : AbstractQueue<E>, TransferQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val transferCondition = lock.newCondition()
    private val items = ArrayList<E>()
    private val waitingConsumers = ArrayList<TransferSlot<E>>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun isEmpty(): Boolean = lock.withLock { items.isEmpty() }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
            } else {
                items.add(e)
                notEmpty.signal()
            }
        }
        return true
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun tryTransfer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                true
            } else {
                false
            }
        }
    }

    actual override fun transfer(e: E) {
        if (e == null) throw NullPointerException()
        if (Thread.interrupted()) throw InterruptedException()
        lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                return
            }
            items.add(e)
            notEmpty.signal()
            while (items.contains(e)) {
                transferCondition.await()
            }
        }
    }

    actual override fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean {
        if (e == null) throw NullPointerException()
        if (Thread.interrupted()) throw InterruptedException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                return@withLock true
            }
            if (nanos <= 0L) return@withLock false
            items.add(e)
            notEmpty.signal()
            while (items.contains(e)) {
                if (nanos <= 0L) {
                    val removed = items.remove(e)
                    return@withLock !removed
                }
                nanos = transferCondition.awaitNanos(nanos)
            }
            true
        }
    }

    actual override fun poll(): E? = lock.withLock {
        if (items.isEmpty()) {
            null
        } else {
            val x = items.removeAt(0)
            transferCondition.signalAll()
            x
        }
    }

    actual override fun take(): E {
        if (Thread.interrupted()) throw InterruptedException()
        return lock.withLock {
            if (items.isNotEmpty()) {
                val x = items.removeAt(0)
                transferCondition.signalAll()
                return@withLock x
            }
            val slot = TransferSlot<E>()
            waitingConsumers.add(slot)
            try {
                while (!slot.fulfilled) {
                    if (items.isNotEmpty()) {
                        waitingConsumers.remove(slot)
                        val x = items.removeAt(0)
                        transferCondition.signalAll()
                        return@withLock x
                    }
                    notEmpty.await()
                }
                slot.item!!
            } catch (ie: InterruptedException) {
                waitingConsumers.remove(slot)
                throw ie
            }
        }
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        if (Thread.interrupted()) throw InterruptedException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            if (items.isNotEmpty()) {
                val x = items.removeAt(0)
                transferCondition.signalAll()
                return@withLock x
            }
            if (nanos <= 0L) return@withLock null
            val slot = TransferSlot<E>()
            waitingConsumers.add(slot)
            try {
                while (!slot.fulfilled) {
                    if (items.isNotEmpty()) {
                        waitingConsumers.remove(slot)
                        val x = items.removeAt(0)
                        transferCondition.signalAll()
                        return@withLock x
                    }
                    if (nanos <= 0L) {
                        waitingConsumers.remove(slot)
                        return@withLock if (slot.fulfilled) slot.item else null
                    }
                    nanos = notEmpty.awaitNanos(nanos)
                }
                slot.item
            } catch (ie: InterruptedException) {
                waitingConsumers.remove(slot)
                throw ie
            }
        }
    }

    actual override fun peek(): E? = lock.withLock { items.firstOrNull() }

    actual override fun hasWaitingConsumer(): Boolean = lock.withLock { waitingConsumers.isNotEmpty() }

    actual override fun getWaitingConsumerCount(): Int = lock.withLock { waitingConsumers.size }

    actual override fun remainingCapacity(): Int = Int.MAX_VALUE

    actual override fun remove(element: E): Boolean = lock.withLock {
        if (element == null) return@withLock false
        val removed = items.remove(element)
        if (removed) transferCondition.signalAll()
        removed
    }

    actual override fun contains(element: E): Boolean = lock.withLock {
        if (element == null) false else items.contains(element)
    }

    actual override fun clear() = lock.withLock {
        items.clear()
        transferCondition.signalAll()
    }

    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)

    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            val n = minOf(maxElements, items.size)
            for (i in 0 until n) {
                c.add(items.removeAt(0))
            }
            if (n > 0) transferCondition.signalAll()
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(items) }
        return object : MutableIterator<E> {
            private var cursor = 0
            private var lastRet = -1
            override fun hasNext(): Boolean = cursor < snapshot.size
            override fun next(): E {
                if (cursor >= snapshot.size) throw NoSuchElementException()
                lastRet = cursor
                return snapshot[cursor++]
            }
            override fun remove() {
                if (lastRet < 0) throw IllegalStateException()
                this@LinkedTransferQueue.remove(snapshot[lastRet])
                lastRet = -1
            }
        }
    }
}

actual open class SynchronousQueue<E> actual constructor() : AbstractQueue<E>(), BlockingQueue<E>, Serializable {
    actual override val size: Int get() = 0
    actual override fun iterator(): MutableIterator<E> = mutableListOf<E>().iterator()
    actual override fun offer(e: E): Boolean = true
    actual override fun put(e: E) {}
    actual override fun take(): E = throw NoSuchElementException()
    actual override fun poll(timeout: Long, unit: TimeUnit): E? = null
    actual override fun poll(): E? = null
    actual override fun peek(): E? = null
    override fun remove(element: E): Boolean = false
    override fun clear() {}
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = true
    actual override fun remainingCapacity(): Int = 0
    actual override fun drainTo(c: MutableCollection<in E>): Int = 0
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int = 0
}

actual open class RejectedExecutionException : RuntimeException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class ConcurrentHashMap<K, V> actual constructor() : MutableMap<K, V>, Serializable {
    private val map = HashMap<K, V>()
    actual override fun put(key: K, value: V): V? = map.put(key, value)
    actual override operator fun get(key: K): V? = map.get(key)
    actual override fun remove(key: K): V? = map.remove(key)
    actual override fun clear() = map.clear()
    actual override val size: Int get() = map.size
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    actual override fun isEmpty(): Boolean = map.isEmpty()
    actual override fun containsKey(key: K): Boolean = map.containsKey(key)
    actual override fun containsValue(value: V): Boolean = map.containsValue(value)
    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)
    actual open fun keySet(): MutableSet<K> = map.keys
}

actual open class LinkedBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val list = mutableListOf<E>()

    actual constructor() : super()
    actual constructor(capacity: Int) : super()

    actual override val size: Int get() = lock.withLock { list.size }
    actual override fun iterator(): MutableIterator<E> = lock.withLock { ArrayList(list).iterator() }
    actual override fun offer(e: E): Boolean = lock.withLock {
        list.add(e)
        notEmpty.signal()
        true
    }
    actual override fun put(e: E) {
        offer(e)
    }
    actual override fun take(): E = lock.withLock {
        while (list.isEmpty()) {
            notEmpty.await()
        }
        list.removeAt(0)
    }
    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (list.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            list.removeAt(0)
        }
    }
    actual override fun poll(): E? = lock.withLock { if (list.isNotEmpty()) list.removeAt(0) else null }
    actual override fun peek(): E? = lock.withLock { list.firstOrNull() }
    override fun remove(element: E): Boolean = lock.withLock { list.remove(element) }
    override fun clear() = lock.withLock { list.clear() }
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)
    actual override fun remainingCapacity(): Int = Int.MAX_VALUE
    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int = lock.withLock {
        var n = 0
        while (n < maxElements && list.isNotEmpty()) {
            val el = list.removeAt(0)
            c.add(el)
            n++
        }
        n
    }
}

actual object Executors {
    actual fun newSingleThreadExecutor(): ExecutorService = newSingleThreadExecutor(ThreadFactory { r -> Thread(r) })

    actual fun newSingleThreadExecutor(threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
        threadFactory
    )

    actual fun newFixedThreadPool(nThreads: Int): ExecutorService = newFixedThreadPool(nThreads, ThreadFactory { r -> Thread(r) })

    actual fun newFixedThreadPool(nThreads: Int, threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
        threadFactory
    )

    actual fun newCachedThreadPool(): ExecutorService = newCachedThreadPool(ThreadFactory { r -> Thread(r) })

    actual fun newCachedThreadPool(threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        0, Int.MAX_VALUE, 60L, TimeUnit.SECONDS,
        SynchronousQueue(),
        threadFactory
    )

    actual fun newSingleThreadScheduledExecutor(): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(1)

    actual fun newSingleThreadScheduledExecutor(threadFactory: ThreadFactory): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(1, threadFactory)

    actual fun newScheduledThreadPool(corePoolSize: Int): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(corePoolSize)

    actual fun newScheduledThreadPool(corePoolSize: Int, threadFactory: ThreadFactory): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(corePoolSize, threadFactory)

    actual fun newWorkStealingPool(parallelism: Int): ExecutorService =
        ForkJoinPool(parallelism)

    actual fun newWorkStealingPool(): ExecutorService =
        ForkJoinPool()

    actual fun unconfigurableExecutorService(executor: ExecutorService): ExecutorService =
        object : ExecutorService by executor {}

    actual fun unconfigurableScheduledExecutorService(executor: ScheduledExecutorService): ScheduledExecutorService =
        object : ScheduledExecutorService by executor {}

    actual fun defaultThreadFactory(): ThreadFactory = ThreadFactory { r -> Thread(r) }

    actual fun <T> callable(task: Runnable, result: T): Callable<T> = Callable {
        task.run()
        result
    }

    actual fun callable(task: Runnable): Callable<Any?> = Callable {
        task.run()
        null
    }
}

