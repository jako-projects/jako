package java.util.concurrent

import java.io.Serializable
import java.lang.Runnable
import java.lang.Thread
import java.util.AbstractQueue

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
) : ExecutorService {
    private var isShutdownState = false

    actual override fun execute(command: Runnable) {
        if (isShutdownState) throw RejectedExecutionException("Executor is shutdown")
        threadFactory.newThread(command).start()
    }

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
    private val list = mutableListOf<E>()

    actual constructor() : super()
    actual constructor(capacity: Int) : super()

    actual override val size: Int get() = list.size
    actual override fun iterator(): MutableIterator<E> = list.iterator()
    actual override fun offer(e: E): Boolean { list.add(e); return true }
    actual override fun put(e: E) { list.add(e) }
    actual override fun take(): E = if (list.isNotEmpty()) list.removeAt(0) else throw NoSuchElementException()
    actual override fun poll(timeout: Long, unit: TimeUnit): E? = poll()
    actual override fun poll(): E? = if (list.isNotEmpty()) list.removeAt(0) else null
    actual override fun peek(): E? = list.firstOrNull()
    override fun remove(element: E): Boolean = list.remove(element)
    override fun clear() { list.clear() }
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)
    actual override fun remainingCapacity(): Int = Int.MAX_VALUE
    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        var n = 0
        while (n < maxElements && isNotEmpty()) {
            val el = poll() ?: break
            c.add(el)
            n++
        }
        return n
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
}
