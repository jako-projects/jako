@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.lang.Runnable
import java.util.AbstractQueue
import java.util.Deque
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.Random

expect interface Delayed : Comparable<Delayed> {
    fun getDelay(unit: TimeUnit): Long
}

expect interface ScheduledFuture<V> : Delayed, Future<V>

expect interface ScheduledExecutorService : ExecutorService {
    fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}

expect open class ScheduledThreadPoolExecutor : ThreadPoolExecutor, ScheduledExecutorService {
    constructor(corePoolSize: Int)
    constructor(corePoolSize: Int, threadFactory: ThreadFactory)

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    override fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    override fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}

/**
 * [Nullability Decision] ThreadLocalRandom.current() returns non-null ThreadLocalRandom.
 */
expect class ThreadLocalRandom : Random {
    override fun setSeed(seed: Long)
    override fun nextBoolean(): Boolean
    override fun nextInt(): Int
    override fun nextInt(bound: Int): Int
    fun nextInt(origin: Int, bound: Int): Int
    override fun nextLong(): Long
    fun nextLong(bound: Long): Long
    fun nextLong(origin: Long, bound: Long): Long
    override fun nextFloat(): Float
    override fun nextDouble(): Double
    fun nextDouble(bound: Double): Double
    fun nextDouble(origin: Double, bound: Double): Double

    companion object {
        fun current(): ThreadLocalRandom
    }
}

/**
 * [Nullability Decision] ArrayBlockingQueue does not permit null elements.
 * poll() and peek() return E? (null when empty).
 */
expect open class ArrayBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor(capacity: Int)
    constructor(capacity: Int, fair: Boolean)
    constructor(capacity: Int, fair: Boolean, c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun poll(): E?
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun peek(): E?
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

/**
 * [Nullability Decision] PriorityBlockingQueue does not permit null elements.
 * poll() and peek() return E? (null when empty).
 */
expect open class PriorityBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor()
    constructor(initialCapacity: Int)
    constructor(initialCapacity: Int, comparator: Comparator<in E>?)
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun poll(): E?
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun peek(): E?
    open fun comparator(): Comparator<in E>?
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

/**
 * [Nullability Decision] DelayQueue does not permit null elements.
 * poll() returns E? (null when empty or head not yet expired); peek() returns E? (head even if not expired).
 */
expect open class DelayQueue<E : Delayed> : AbstractQueue<E>, BlockingQueue<E> {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun poll(): E?
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun peek(): E?
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

/**
 * [Nullability Decision] ConcurrentSkipListMap does not permit null keys or values.
 */
expect open class ConcurrentSkipListMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in K>?)
    constructor(m: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override operator fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>

    open fun putIfAbsent(key: K, value: V): V?
    open fun remove(key: Any?, value: Any?): Boolean
    open fun replace(key: K, oldValue: V, newValue: V): Boolean
    open fun replace(key: K, value: V): V?
    override fun comparator(): Comparator<in K>?
    override fun firstKey(): K
    override fun lastKey(): K
    override fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun lowerKey(key: K): K?
    override fun floorEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun floorKey(key: K): K?
    override fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun ceilingKey(key: K): K?
    override fun higherEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun higherKey(key: K): K?
    override fun firstEntry(): MutableMap.MutableEntry<K, V>?
    override fun lastEntry(): MutableMap.MutableEntry<K, V>?
    override fun pollFirstEntry(): MutableMap.MutableEntry<K, V>?
    override fun pollLastEntry(): MutableMap.MutableEntry<K, V>?
    open fun keySet(): MutableSet<K>
}

/**
 * [Nullability Decision] ConcurrentSkipListSet does not permit null elements.
 */
expect open class ConcurrentSkipListSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in E>?)
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun iterator(): MutableIterator<E>

    override fun descendingIterator(): MutableIterator<E>
    override fun comparator(): Comparator<in E>?
    override fun first(): E
    override fun last(): E
    open fun lower(e: E): E?
    open fun floor(e: E): E?
    open fun ceiling(e: E): E?
    open fun higher(e: E): E?
    open fun pollFirst(): E?
    open fun pollLast(): E?
}

/**
 * [Nullability Decision] ConcurrentLinkedDeque does not permit null elements.
 * poll*, peek* return E? (null when empty).
 */
expect open class ConcurrentLinkedDeque<E> : AbstractMutableCollection<E>, Deque<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun iterator(): MutableIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()

    override fun addFirst(e: E)
    override fun addLast(e: E)
    override fun offerFirst(e: E): Boolean
    override fun offerLast(e: E): Boolean
    override fun removeFirst(): E
    override fun removeLast(): E
    override fun pollFirst(): E?
    override fun pollLast(): E?
    override fun getFirst(): E
    override fun getLast(): E
    override fun peekFirst(): E?
    override fun peekLast(): E?
    open fun removeFirstOccurrence(o: Any?): Boolean
    open fun removeLastOccurrence(o: Any?): Boolean
    override fun offer(e: E): Boolean
    open fun remove(): E
    override fun poll(): E?
    open fun element(): E
    override fun peek(): E?
    override fun push(e: E)
    override fun pop(): E
}

expect open class Phaser {
    constructor()
    constructor(parties: Int)
    constructor(parent: Phaser?)
    constructor(parent: Phaser?, parties: Int)

    open fun register(): Int
    open fun bulkRegister(parties: Int): Int
    open fun arrive(): Int
    open fun arriveAndDeregister(): Int
    open fun arriveAndAwaitAdvance(): Int
    open fun awaitAdvance(phase: Int): Int
    open fun awaitAdvanceInterruptibly(phase: Int): Int
    open fun awaitAdvanceInterruptibly(phase: Int, timeout: Long, unit: TimeUnit): Int
    open fun forceTermination()
    open fun getPhase(): Int
    open fun getRegisteredParties(): Int
    open fun getArrivedParties(): Int
    open fun getUnarrivedParties(): Int
    open fun getParent(): Phaser?
    open fun getRoot(): Phaser
    open fun isTerminated(): Boolean
    protected open fun onAdvance(phase: Int, registeredParties: Int): Boolean
}

expect open class Exchanger<V> {
    constructor()
    open fun exchange(x: V): V
    open fun exchange(x: V, timeout: Long, unit: TimeUnit): V
}

