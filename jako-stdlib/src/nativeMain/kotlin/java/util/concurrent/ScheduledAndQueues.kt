package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.lang.ThreadLocal
import java.util.AbstractQueue
import java.util.Deque
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.PriorityQueue
import java.util.Random
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual interface Delayed : Comparable<Delayed> {
    actual fun getDelay(unit: TimeUnit): Long
}

actual interface ScheduledFuture<V> : Delayed, Future<V>

actual interface ScheduledExecutorService : ExecutorService {
    actual fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    actual fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    actual fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    actual fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}

private class ScheduledFutureTask<V>(
    callable: Callable<V>,
    private val triggerTimeNanos: Long
) : FutureTask<V>(callable), ScheduledFuture<V> {
    override fun getDelay(unit: TimeUnit): Long {
        val remainingNanos = triggerTimeNanos - System.nanoTime()
        return unit.convert(remainingNanos, TimeUnit.NANOSECONDS)
    }

    override fun compareTo(other: Delayed): Int {
        if (other === this) return 0
        val diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
        return when {
            diff < 0L -> -1
            diff > 0L -> 1
            else -> 0
        }
    }
}

actual open class ScheduledThreadPoolExecutor actual constructor(
    corePoolSize: Int,
    private val threadFactory: ThreadFactory
) : ThreadPoolExecutor(
    corePoolSize,
    Int.MAX_VALUE,
    0L,
    TimeUnit.NANOSECONDS,
    LinkedBlockingQueue(),
    threadFactory
), ScheduledExecutorService {

    actual constructor(corePoolSize: Int) : this(corePoolSize, ThreadFactory { r -> Thread(r) })

    init {
        if (corePoolSize < 0) throw IllegalArgumentException()
    }

    actual override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return schedule(Callable<Any?> { command.run(); null }, delay, unit)
    }

    actual override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> {
        if (isShutdown()) throw RejectedExecutionException("Executor is shutdown")
        val delayNanos = maxOf(0L, unit.toNanos(delay))
        val triggerNanos = System.nanoTime() + delayNanos
        val task = ScheduledFutureTask(callable, triggerNanos)
        threadFactory.newThread(Runnable {
            val sleepMillis = TimeUnit.NANOSECONDS.toMillis(delayNanos)
            val sleepNanos = (delayNanos % 1_000_000L).toInt()
            if (sleepMillis > 0 || sleepNanos > 0) {
                try {
                    Thread.sleep(sleepMillis, sleepNanos)
                } catch (_: InterruptedException) {
                    return@Runnable
                }
            }
            if (!task.isCancelled() && !isShutdown()) {
                task.run()
            }
        }).start()
        return task
    }

    actual override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        if (period <= 0L) throw IllegalArgumentException("period must be positive")
        return schedulePeriodic(command, initialDelay, period, unit, fixedRate = true)
    }

    actual override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        if (delay <= 0L) throw IllegalArgumentException("delay must be positive")
        return schedulePeriodic(command, initialDelay, delay, unit, fixedRate = false)
    }

    private fun schedulePeriodic(
        command: Runnable,
        initialDelay: Long,
        interval: Long,
        unit: TimeUnit,
        fixedRate: Boolean
    ): ScheduledFuture<*> {
        if (isShutdown()) throw RejectedExecutionException("Executor is shutdown")
        val initNanos = maxOf(0L, unit.toNanos(initialDelay))
        val intervalNanos = unit.toNanos(interval)
        val cancelled = AtomicBoolean(false)
        val done = AtomicBoolean(false)
        var workerThread: Thread? = null
        var nextTriggerNanos = System.nanoTime() + initNanos

        val future = object : ScheduledFuture<Any?> {
            override fun getDelay(unit: TimeUnit): Long {
                return unit.convert(nextTriggerNanos - System.nanoTime(), TimeUnit.NANOSECONDS)
            }

            override fun compareTo(other: Delayed): Int {
                if (other === this) return 0
                val d = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
                return if (d < 0) -1 else if (d > 0) 1 else 0
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                if (done.get()) return false
                val changed = cancelled.compareAndSet(false, true)
                done.set(true)
                if (changed && mayInterruptIfRunning) {
                    workerThread?.interrupt()
                }
                return changed
            }

            override fun isCancelled(): Boolean = cancelled.get()
            override val isCancelled: Boolean get() = cancelled.get()
            override fun isDone(): Boolean = done.get()
            override val isDone: Boolean get() = done.get()

            override fun get(): Any? {
                while (!done.get() && !isShutdown()) {
                    Thread.sleep(5)
                }
                if (cancelled.get()) throw CancellationException()
                return null
            }

            override fun get(timeout: Long, unit: TimeUnit): Any? {
                val deadline = System.nanoTime() + unit.toNanos(timeout)
                while (!done.get() && !isShutdown()) {
                    if (System.nanoTime() >= deadline) throw TimeoutException()
                    Thread.sleep(5)
                }
                if (cancelled.get()) throw CancellationException()
                return null
            }
        }

        val t = threadFactory.newThread(Runnable {
            try {
                if (initNanos > 0) {
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(initNanos), (initNanos % 1_000_000L).toInt())
                }
                while (!cancelled.get() && !isShutdown()) {
                    val startNanos = System.nanoTime()
                    command.run()
                    if (cancelled.get() || isShutdown()) break
                    val sleepNanos = if (fixedRate) {
                        nextTriggerNanos += intervalNanos
                        maxOf(0L, nextTriggerNanos - System.nanoTime())
                    } else {
                        nextTriggerNanos = System.nanoTime() + intervalNanos
                        intervalNanos
                    }
                    if (sleepNanos > 0) {
                        Thread.sleep(TimeUnit.NANOSECONDS.toMillis(sleepNanos), (sleepNanos % 1_000_000L).toInt())
                    }
                }
            } catch (_: InterruptedException) {
                // Exit on interrupt
            } catch (_: Throwable) {
                done.set(true)
            } finally {
                done.set(true)
            }
        })
        workerThread = t
        t.start()
        return future
    }
}

actual class ThreadLocalRandom private constructor(
    initialSeed: Long
) : Random(initialSeed) {
    private var initialized = true

    actual override fun setSeed(seed: Long) {
        if (initialized) {
            throw UnsupportedOperationException()
        }
        super.setSeed(seed)
    }

    actual override fun nextBoolean(): Boolean = super.nextBoolean()

    actual override fun nextInt(): Int = super.nextInt()

    actual override fun nextInt(bound: Int): Int {
        if (bound <= 0) throw IllegalArgumentException("bound must be positive")
        return super.nextInt(bound)
    }

    actual fun nextInt(origin: Int, bound: Int): Int {
        if (origin >= bound) throw IllegalArgumentException("bound must be greater than origin")
        val n = bound - origin
        if (n > 0) {
            return nextInt(n) + origin
        }
        var r: Int
        do {
            r = nextInt()
        } while (r < origin || r >= bound)
        return r
    }

    actual override fun nextLong(): Long = super.nextLong()

    actual fun nextLong(bound: Long): Long {
        if (bound <= 0L) throw IllegalArgumentException("bound must be positive")
        var r = nextLong() ushr 1
        val m = bound - 1L
        if ((bound and m) == 0L) {
            r = r and m
        } else {
            var u = r
            while (u - (u % bound) + m < 0L) {
                u = nextLong() ushr 1
            }
            r = u % bound
        }
        return r
    }

    actual fun nextLong(origin: Long, bound: Long): Long {
        if (origin >= bound) throw IllegalArgumentException("bound must be greater than origin")
        val n = bound - origin
        if (n > 0L) {
            return nextLong(n) + origin
        }
        var r: Long
        do {
            r = nextLong()
        } while (r < origin || r >= bound)
        return r
    }

    actual override fun nextFloat(): Float = super.nextFloat()

    actual override fun nextDouble(): Double = super.nextDouble()

    actual fun nextDouble(bound: Double): Double {
        if (!(bound > 0.0)) throw IllegalArgumentException("bound must be positive")
        val result = nextDouble() * bound
        return if (result < bound) result else Double.fromBits(bound.toRawBits() - 1)
    }

    actual fun nextDouble(origin: Double, bound: Double): Double {
        if (!(origin < bound)) throw IllegalArgumentException("bound must be greater than origin")
        var r = nextDouble() * (bound - origin) + origin
        if (r >= bound) {
            r = Double.fromBits(bound.toRawBits() - 1)
        }
        return r
    }

    actual companion object {
        private val localInstance = ThreadLocal.withInitial {
            ThreadLocalRandom(kotlin.random.Random.nextLong())
        }

        actual fun current(): ThreadLocalRandom = localInstance.get()
    }
}

actual open class ArrayBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val capacity: Int
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val notFull = lock.newCondition()
    private val items = ArrayList<E>()

    actual constructor(capacity: Int) : this(capacity, false)

    actual constructor(capacity: Int, fair: Boolean) : super() {
        if (capacity <= 0) throw IllegalArgumentException()
        this.capacity = capacity
    }

    actual constructor(capacity: Int, fair: Boolean, c: Collection<E>) : this(capacity, fair) {
        if (capacity < c.size) throw IllegalArgumentException()
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun add(element: E): Boolean = super.add(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            if (items.size == capacity) {
                false
            } else {
                items.add(e)
                notEmpty.signal()
                true
            }
        }
    }

    actual override fun put(e: E) {
        if (e == null) throw NullPointerException()
        lock.withLock {
            while (items.size == capacity) {
                notFull.await()
            }
            items.add(e)
            notEmpty.signal()
        }
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean {
        if (e == null) throw NullPointerException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (items.size == capacity) {
                if (nanos <= 0L) return@withLock false
                nanos = notFull.awaitNanos(nanos)
            }
            items.add(e)
            notEmpty.signal()
            true
        }
    }

    actual override fun poll(): E? = lock.withLock {
        if (items.isEmpty()) {
            null
        } else {
            val x = items.removeAt(0)
            notFull.signal()
            x
        }
    }

    actual override fun take(): E = lock.withLock {
        while (items.isEmpty()) {
            notEmpty.await()
        }
        val x = items.removeAt(0)
        notFull.signal()
        x
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (items.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            val x = items.removeAt(0)
            notFull.signal()
            x
        }
    }

    actual override fun peek(): E? = lock.withLock {
        items.firstOrNull()
    }

    actual override fun remainingCapacity(): Int = lock.withLock {
        capacity - items.size
    }

    actual override fun remove(element: E): Boolean = lock.withLock {
        if (element == null) return@withLock false
        val removed = items.remove(element)
        if (removed) notFull.signal()
        removed
    }

    actual override fun contains(element: E): Boolean = lock.withLock {
        if (element == null) false else items.contains(element)
    }

    actual override fun clear() = lock.withLock {
        val hadElements = items.isNotEmpty()
        items.clear()
        if (hadElements) notFull.signalAll()
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
            if (n > 0) notFull.signalAll()
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
                this@ArrayBlockingQueue.remove(snapshot[lastRet])
                lastRet = -1
            }
        }
    }
}

actual open class PriorityBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val pq: PriorityQueue<E>

    actual constructor() : this(11, null)

    actual constructor(initialCapacity: Int) : this(initialCapacity, null)

    actual constructor(initialCapacity: Int, comparator: Comparator<in E>?) : super() {
        if (initialCapacity < 1) throw IllegalArgumentException()
        this.pq = PriorityQueue(initialCapacity, comparator)
    }

    actual constructor(c: Collection<E>) : super() {
        this.pq = PriorityQueue(c)
    }

    actual override val size: Int
        get() = lock.withLock { pq.size }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            pq.offer(e)
            notEmpty.signal()
            true
        }
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun poll(): E? = lock.withLock { pq.poll() }

    actual override fun take(): E = lock.withLock {
        while (pq.isEmpty()) {
            notEmpty.await()
        }
        pq.poll()!!
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (pq.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            pq.poll()
        }
    }

    actual override fun peek(): E? = lock.withLock { pq.peek() }

    actual open fun comparator(): Comparator<in E>? = pq.comparator()

    actual override fun remainingCapacity(): Int = Int.MAX_VALUE

    actual override fun remove(element: E): Boolean = lock.withLock { pq.remove(element) }

    actual override fun contains(element: E): Boolean = lock.withLock { pq.contains(element) }

    actual override fun clear() = lock.withLock { pq.clear() }

    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)

    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            var n = 0
            while (n < maxElements) {
                val e = pq.poll() ?: break
                c.add(e)
                n++
            }
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(pq) }
        return snapshot.iterator()
    }
}

actual open class DelayQueue<E : Delayed> actual constructor() : AbstractQueue<E>(), BlockingQueue<E> {
    private val lock = ReentrantLock()
    private val available = lock.newCondition()
    private val pq = PriorityQueue<E>()

    actual constructor(c: Collection<E>) : this() {
        addAll(c)
    }

    actual override val size: Int
        get() = lock.withLock { pq.size }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        return lock.withLock {
            pq.offer(e)
            if (pq.peek() === e) {
                available.signal()
            }
            true
        }
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun poll(): E? = lock.withLock {
        val first = pq.peek()
        if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L) {
            null
        } else {
            pq.poll()
        }
    }

    actual override fun take(): E = lock.withLock {
        while (true) {
            val first = pq.peek()
            if (first == null) {
                available.await()
            } else {
                val delay = first.getDelay(TimeUnit.NANOSECONDS)
                if (delay <= 0L) {
                    val x = pq.poll()!!
                    if (pq.isNotEmpty()) {
                        available.signal()
                    }
                    return@withLock x
                }
                available.awaitNanos(delay)
            }
        }
        @Suppress("UNREACHABLE_CODE")
        throw IllegalStateException()
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (true) {
                val first = pq.peek()
                if (first == null) {
                    if (nanos <= 0L) return@withLock null
                    nanos = available.awaitNanos(nanos)
                } else {
                    val delay = first.getDelay(TimeUnit.NANOSECONDS)
                    if (delay <= 0L) {
                        val x = pq.poll()
                        if (pq.isNotEmpty()) {
                            available.signal()
                        }
                        return@withLock x
                    }
                    if (nanos <= 0L) return@withLock null
                    val waitTime = minOf(nanos, delay)
                    val left = available.awaitNanos(waitTime)
                    nanos -= (waitTime - left)
                }
            }
            @Suppress("UNREACHABLE_CODE")
            null
        }
    }

    actual override fun peek(): E? = lock.withLock { pq.peek() }

    actual override fun remainingCapacity(): Int = Int.MAX_VALUE

    actual override fun remove(element: E): Boolean = lock.withLock { pq.remove(element) }

    actual override fun contains(element: E): Boolean = lock.withLock { pq.contains(element) }

    actual override fun clear() = lock.withLock { pq.clear() }

    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)

    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            var n = 0
            while (n < maxElements) {
                val first = pq.peek()
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L) break
                c.add(pq.poll()!!)
                n++
            }
            if (n > 0 && pq.isNotEmpty()) {
                available.signal()
            }
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(pq) }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                this@DelayQueue.remove(e)
            }
        }
    }
}

actual open class ConcurrentSkipListMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    private val lock = ReentrantLock()
    private val map: TreeMap<K, V>

    actual constructor() : super() {
        map = TreeMap()
    }

    actual constructor(comparator: Comparator<in K>?) : super() {
        map = TreeMap(comparator)
    }

    actual constructor(m: Map<out K, V>) : super() {
        map = TreeMap()
        putAll(m)
    }

    private fun checkNotNullKey(key: Any?) {
        if (key == null) throw NullPointerException("key is null")
    }

    private fun checkNotNullValue(value: Any?) {
        if (value == null) throw NullPointerException("value is null")
    }

    @Suppress("UNCHECKED_CAST")
    private fun compareKeys(k1: K, k2: K): Int {
        val cmp = map.comparator()
        return if (cmp != null) cmp.compare(k1, k2) else (k1 as Comparable<K>).compareTo(k2)
    }

    actual override val size: Int
        get() = lock.withLock { map.size }

    actual override fun isEmpty(): Boolean = lock.withLock { map.isEmpty() }

    actual override fun containsKey(key: K): Boolean {
        checkNotNullKey(key)
        return lock.withLock { map.containsKey(key) }
    }

    actual override fun containsValue(value: V): Boolean {
        checkNotNullValue(value)
        return lock.withLock { map.containsValue(value) }
    }

    actual override operator fun get(key: K): V? {
        checkNotNullKey(key)
        return lock.withLock { map[key] }
    }

    actual override fun put(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock { map.put(key, value) }
    }

    actual override fun remove(key: K): V? {
        checkNotNullKey(key)
        return lock.withLock { map.remove(key) }
    }

    actual override fun clear() = lock.withLock { map.clear() }

    actual open fun putIfAbsent(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock {
            val prev = map[key]
            if (prev == null) map.put(key, value) else prev
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun remove(key: Any?, value: Any?): Boolean {
        checkNotNullKey(key)
        if (value == null) return false
        return lock.withLock {
            val k = key as K
            if (map.containsKey(k) && map[k] == value) {
                map.remove(k)
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, oldValue: V, newValue: V): Boolean {
        checkNotNullKey(key)
        checkNotNullValue(oldValue)
        checkNotNullValue(newValue)
        return lock.withLock {
            if (map.containsKey(key) && map[key] == oldValue) {
                map.put(key, newValue)
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock {
            if (map.containsKey(key)) map.put(key, value) else null
        }
    }

    actual override fun comparator(): Comparator<in K>? = map.comparator()

    actual override fun firstKey(): K = lock.withLock { map.firstKey() }

    actual override fun lastKey(): K = lock.withLock { map.lastKey() }

    actual override fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.lowerEntry(key) }
    }

    actual override fun lowerKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.lowerKey(key) }
    }

    actual override fun floorEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.floorEntry(key) }
    }

    actual override fun floorKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.floorKey(key) }
    }

    actual override fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.ceilingEntry(key) }
    }

    actual override fun ceilingKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.ceilingKey(key) }
    }

    actual override fun higherEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.higherEntry(key) }
    }

    actual override fun higherKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.higherKey(key) }
    }

    actual override fun firstEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.firstEntry()
    }

    actual override fun lastEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.lastEntry()
    }

    actual override fun pollFirstEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.pollFirstEntry()
    }

    actual override fun pollLastEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.pollLastEntry()
    }

    actual open fun keySet(): MutableSet<K> = keys

    internal fun snapshotKeysDescending(): MutableList<K> = lock.withLock {
        val list = ArrayList(map.keys)
        list.reverse()
        list
    }

    actual override val keys: MutableSet<K>
        get() = object : AbstractMutableSet<K>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun contains(element: K): Boolean = this@ConcurrentSkipListMap.containsKey(element)
            override fun add(element: K): Boolean = throw UnsupportedOperationException()
            override fun remove(element: K): Boolean = this@ConcurrentSkipListMap.remove(element) != null
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun iterator(): MutableIterator<K> {
                val snapshot = lock.withLock { ArrayList(map.keys) }
                return object : MutableIterator<K> {
                    private val it = snapshot.iterator()
                    private var lastKey: K? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): K {
                        val k = it.next()
                        lastKey = k
                        return k
                    }
                    override fun remove() {
                        val k = lastKey ?: throw IllegalStateException()
                        lastKey = null
                        this@ConcurrentSkipListMap.remove(k)
                    }
                }
            }
        }

    actual override val values: MutableCollection<V>
        get() = object : AbstractMutableCollection<V>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun contains(element: V): Boolean = this@ConcurrentSkipListMap.containsValue(element)
            override fun add(element: V): Boolean = throw UnsupportedOperationException()
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun iterator(): MutableIterator<V> {
                val snapshot = lock.withLock { ArrayList(map.entries.map { SimpleMutableEntry(it.key, it.value) }) }
                return object : MutableIterator<V> {
                    private val it = snapshot.iterator()
                    private var lastEntry: SimpleMutableEntry<K, V>? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): V {
                        val e = it.next()
                        lastEntry = e
                        return e.value
                    }
                    override fun remove() {
                        val e = lastEntry ?: throw IllegalStateException()
                        lastEntry = null
                        this@ConcurrentSkipListMap.remove(e.key, e.value)
                    }
                }
            }
        }

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean =
                this@ConcurrentSkipListMap[element.key] == element.value
            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean =
                this@ConcurrentSkipListMap.remove(element.key, element.value)
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                val snapshot = lock.withLock { ArrayList<MutableMap.MutableEntry<K, V>>(map.entries.map { SimpleMutableEntry(it.key, it.value) }) }
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private val it = snapshot.iterator()
                    private var lastEntry: MutableMap.MutableEntry<K, V>? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): MutableMap.MutableEntry<K, V> {
                        val e = it.next()
                        lastEntry = e
                        return e
                    }
                    override fun remove() {
                        val e = lastEntry ?: throw IllegalStateException()
                        lastEntry = null
                        this@ConcurrentSkipListMap.remove(e.key)
                    }
                }
            }
        }

    private class SimpleMutableEntry<K, V>(
        override val key: K,
        override var value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }
        override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
        override fun toString(): String = "$key=$value"
    }
}

actual open class ConcurrentSkipListSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    private val m: ConcurrentSkipListMap<E, Boolean>

    actual constructor() : super() {
        m = ConcurrentSkipListMap()
    }

    actual constructor(comparator: Comparator<in E>?) : super() {
        m = ConcurrentSkipListMap(comparator)
    }

    actual constructor(c: Collection<E>) : super() {
        m = ConcurrentSkipListMap()
        addAll(c)
    }

    actual override val size: Int get() = m.size
    actual override fun isEmpty(): Boolean = m.isEmpty()
    actual override fun contains(element: E): Boolean = m.containsKey(element)
    actual override fun add(element: E): Boolean = m.putIfAbsent(element, true) == null
    actual override fun remove(element: E): Boolean = m.remove(element, true)
    actual override fun clear() = m.clear()
    actual override fun iterator(): MutableIterator<E> = m.keys.iterator()

    actual override fun descendingIterator(): MutableIterator<E> {
        val snapshot = m.snapshotKeysDescending()
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastKey: E? = null
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val k = it.next()
                lastKey = k
                return k
            }
            override fun remove() {
                val k = lastKey ?: throw IllegalStateException()
                lastKey = null
                m.remove(k)
            }
        }
    }

    actual override fun comparator(): Comparator<in E>? = m.comparator()
    actual override fun first(): E = m.firstKey()
    actual override fun last(): E = m.lastKey()
    actual open fun lower(e: E): E? = m.lowerKey(e)
    actual open fun floor(e: E): E? = m.floorKey(e)
    actual open fun ceiling(e: E): E? = m.ceilingKey(e)
    actual open fun higher(e: E): E? = m.higherKey(e)
    actual open fun pollFirst(): E? = m.pollFirstEntry()?.key
    actual open fun pollLast(): E? = m.pollLastEntry()?.key
}

actual open class Phaser actual constructor(
    private val parent: Phaser?,
    parties: Int
) {
    private val root: Phaser = parent?.getRoot() ?: this
    private val lock = ReentrantLock()
    private val phaseCond = lock.newCondition()

    private var phaseNum: Int = 0
    private var registered: Int = 0
    private var arrived: Int = 0
    private var terminated: Boolean = false

    actual constructor() : this(null, 0)
    actual constructor(parties: Int) : this(null, parties)
    actual constructor(parent: Phaser?) : this(parent, 0)

    init {
        if (parties < 0 || parties > 0xFFFF) {
            throw IllegalArgumentException("Illegal number of parties: $parties")
        }
        registered = parties
        if (parent != null && parties > 0) {
            parent.register()
        }
    }

    actual open fun register(): Int = bulkRegister(1)

    actual open fun bulkRegister(parties: Int): Int {
        if (parties < 0) throw IllegalArgumentException("parties < 0")
        if (parties == 0) return getPhase()
        return lock.withLock {
            if (terminated) return@withLock phaseNum
            val wasZero = registered == 0
            registered += parties
            if (parent != null && wasZero) {
                parent.register()
            }
            phaseNum
        }
    }

    actual open fun arrive(): Int = doArrive(deregister = false)

    actual open fun arriveAndDeregister(): Int = doArrive(deregister = true)

    private fun doArrive(deregister: Boolean): Int {
        return lock.withLock {
            if (terminated) return@withLock phaseNum
            val unarrived = registered - arrived
            if (unarrived <= 0) {
                throw IllegalStateException("Attempted arrival of unregistered party")
            }
            val currentPhase = phaseNum
            arrived++
            if (deregister) {
                registered--
                arrived--
            }
            if (arrived == registered) {
                val nextPhase = (currentPhase + 1) and Int.MAX_VALUE
                val shouldTerminate = onAdvance(currentPhase, registered) || registered == 0
                arrived = 0
                if (shouldTerminate) {
                    terminated = true
                    phaseNum = nextPhase or Int.MIN_VALUE
                    if (parent != null) {
                        parent.arriveAndDeregister()
                    }
                } else {
                    phaseNum = nextPhase
                }
                phaseCond.signalAll()
            }
            currentPhase
        }
    }

    actual open fun arriveAndAwaitAdvance(): Int {
        lock.withLock {
            if (terminated) return phaseNum
            val unarrived = registered - arrived
            if (unarrived <= 0) {
                throw IllegalStateException("Attempted arrival of unregistered party")
            }
            val currentPhase = phaseNum
            arrived++
            if (arrived == registered) {
                val nextPhase = (currentPhase + 1) and Int.MAX_VALUE
                val shouldTerminate = onAdvance(currentPhase, registered) || registered == 0
                arrived = 0
                if (shouldTerminate) {
                    terminated = true
                    phaseNum = nextPhase or Int.MIN_VALUE
                    if (parent != null) {
                        parent.arriveAndDeregister()
                    }
                } else {
                    phaseNum = nextPhase
                }
                phaseCond.signalAll()
                return phaseNum
            }
            while (!terminated && phaseNum == currentPhase) {
                phaseCond.await()
            }
            return phaseNum
        }
    }

    actual open fun awaitAdvance(phase: Int): Int {
        if (phase < 0) return phase
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                phaseCond.await()
            }
            phaseNum
        }
    }

    actual open fun awaitAdvanceInterruptibly(phase: Int): Int {
        if (phase < 0) return phase
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                if (Thread.currentThread().isInterrupted()) {
                    throw InterruptedException()
                }
                phaseCond.await()
            }
            phaseNum
        }
    }

    actual open fun awaitAdvanceInterruptibly(phase: Int, timeout: Long, unit: TimeUnit): Int {
        if (phase < 0) return phase
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                if (Thread.currentThread().isInterrupted()) {
                    throw InterruptedException()
                }
                if (nanos <= 0L) {
                    throw TimeoutException()
                }
                nanos = phaseCond.awaitNanos(nanos)
            }
            phaseNum
        }
    }

    actual open fun forceTermination() {
        lock.withLock {
            if (!terminated) {
                terminated = true
                if (phaseNum >= 0) {
                    phaseNum = phaseNum or Int.MIN_VALUE
                }
                phaseCond.signalAll()
            }
        }
    }

    actual open fun getPhase(): Int = lock.withLock { phaseNum }

    actual open fun getRegisteredParties(): Int = lock.withLock { registered }

    actual open fun getArrivedParties(): Int = lock.withLock { if (terminated) 0 else arrived }

    actual open fun getUnarrivedParties(): Int = lock.withLock { if (terminated) 0 else registered - arrived }

    actual open fun getParent(): Phaser? = parent

    actual open fun getRoot(): Phaser = root

    actual open fun isTerminated(): Boolean = lock.withLock { terminated }

    protected actual open fun onAdvance(phase: Int, registeredParties: Int): Boolean =
        registeredParties == 0
}

actual open class Exchanger<V> actual constructor() {
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()

    private class Slot<V>(val item: V) {
        var matched: Boolean = false
        var response: V? = null
    }

    private var waitingSlot: Slot<V>? = null

    @Suppress("UNCHECKED_CAST")
    actual open fun exchange(x: V): V {
        lock.withLock {
            val existing = waitingSlot
            if (existing != null) {
                waitingSlot = null
                existing.response = x
                existing.matched = true
                cond.signalAll()
                return existing.item
            }
            val mySlot = Slot(x)
            waitingSlot = mySlot
            while (!mySlot.matched) {
                if (Thread.currentThread().isInterrupted()) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw InterruptedException()
                }
                cond.await()
            }
            return mySlot.response as V
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun exchange(x: V, timeout: Long, unit: TimeUnit): V {
        var nanos = unit.toNanos(timeout)
        lock.withLock {
            val existing = waitingSlot
            if (existing != null) {
                waitingSlot = null
                existing.response = x
                existing.matched = true
                cond.signalAll()
                return existing.item
            }
            val mySlot = Slot(x)
            waitingSlot = mySlot
            while (!mySlot.matched) {
                if (Thread.currentThread().isInterrupted()) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw InterruptedException()
                }
                if (nanos <= 0L) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw TimeoutException()
                }
                nanos = cond.awaitNanos(nanos)
            }
            return mySlot.response as V
        }
    }
}

actual open class ConcurrentLinkedDeque<E> : AbstractMutableCollection<E>, Deque<E>, Serializable {
    private val lock = ReentrantLock()
    private val items = ArrayList<E>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    private fun checkNotNullElement(e: Any?) {
        if (e == null) throw NullPointerException()
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun isEmpty(): Boolean = lock.withLock { items.isEmpty() }

    actual override fun contains(element: E): Boolean {
        if (element == null) return false
        return lock.withLock { items.contains(element) }
    }

    actual override fun add(element: E): Boolean = offerLast(element)

    actual override fun remove(element: E): Boolean = removeFirstOccurrence(element)

    actual override fun clear() = lock.withLock { items.clear() }

    actual override fun addFirst(e: E) {
        checkNotNullElement(e)
        lock.withLock { items.add(0, e) }
    }

    actual override fun addLast(e: E) {
        checkNotNullElement(e)
        lock.withLock { items.add(e) }
    }

    actual override fun offerFirst(e: E): Boolean {
        addFirst(e)
        return true
    }

    actual override fun offerLast(e: E): Boolean {
        addLast(e)
        return true
    }

    actual override fun removeFirst(): E = pollFirst() ?: throw NoSuchElementException()

    actual override fun removeLast(): E = pollLast() ?: throw NoSuchElementException()

    actual override fun pollFirst(): E? = lock.withLock {
        if (items.isEmpty()) null else items.removeAt(0)
    }

    actual override fun pollLast(): E? = lock.withLock {
        if (items.isEmpty()) null else items.removeAt(items.size - 1)
    }

    actual override fun getFirst(): E = peekFirst() ?: throw NoSuchElementException()

    actual override fun getLast(): E = peekLast() ?: throw NoSuchElementException()

    actual override fun peekFirst(): E? = lock.withLock {
        if (items.isEmpty()) null else items[0]
    }

    actual override fun peekLast(): E? = lock.withLock {
        if (items.isEmpty()) null else items[items.size - 1]
    }

    actual open fun removeFirstOccurrence(o: Any?): Boolean {
        if (o == null) return false
        return lock.withLock {
            val idx = items.indexOfFirst { it == o }
            if (idx >= 0) {
                items.removeAt(idx)
                true
            } else {
                false
            }
        }
    }

    actual open fun removeLastOccurrence(o: Any?): Boolean {
        if (o == null) return false
        return lock.withLock {
            val idx = items.indexOfLast { it == o }
            if (idx >= 0) {
                items.removeAt(idx)
                true
            } else {
                false
            }
        }
    }

    actual override fun offer(e: E): Boolean = offerLast(e)

    actual open fun remove(): E = removeFirst()

    actual override fun poll(): E? = pollFirst()

    actual open fun element(): E = getFirst()

    actual override fun peek(): E? = peekFirst()

    actual override fun push(e: E) = addFirst(e)

    actual override fun pop(): E = removeFirst()

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(items) }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                removeFirstOccurrence(e)
            }
        }
    }

    actual override fun descendingIterator(): MutableIterator<E> {
        val snapshot = lock.withLock {
            val list = ArrayList(items)
            list.reverse()
            list
        }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                removeLastOccurrence(e)
            }
        }
    }
}


