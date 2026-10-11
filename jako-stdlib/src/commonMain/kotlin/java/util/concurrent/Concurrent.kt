@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.lang.Runnable
import java.lang.Thread
import java.util.AbstractQueue
import java.util.Deque
import java.util.Queue
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

expect enum class TimeUnit {
    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;

    fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long
    fun toNanos(duration: Long): Long
    fun toMicros(duration: Long): Long
    fun toMillis(duration: Long): Long
    fun toSeconds(duration: Long): Long
    fun toMinutes(duration: Long): Long
    fun toHours(duration: Long): Long
    fun toDays(duration: Long): Long
}

expect open class CountDownLatch {
    constructor(count: Int)
    open fun await()
    open fun await(timeout: Long, unit: TimeUnit): Boolean
    open fun countDown()
    open fun getCount(): Long
}

expect fun interface Callable<V> {
    fun call(): V
}

expect interface Future<V> {
    fun cancel(mayInterruptIfRunning: Boolean): Boolean
    fun isCancelled(): Boolean
    fun isDone(): Boolean
    val isCancelled: Boolean
    val isDone: Boolean
    fun get(): V
    fun get(timeout: Long, unit: TimeUnit): V
}

expect interface RunnableFuture<V> : Runnable, Future<V> {
    override fun run()
}

expect open class FutureTask<V> : RunnableFuture<V> {
    constructor(callable: Callable<V>)
    constructor(runnable: Runnable, result: V)

    override fun run()
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean
    override fun isCancelled(): Boolean
    override fun isDone(): Boolean
    override val isCancelled: Boolean
    override val isDone: Boolean
    override fun get(): V
    override fun get(timeout: Long, unit: TimeUnit): V
}

expect open class ExecutionException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class TimeoutException : Exception {
    constructor()
    constructor(message: String?)
}

expect open class CancellationException : IllegalStateException {
    constructor()
    constructor(message: String?)
}

expect interface CompletionStage<T> {
    fun <U> thenApply(fn: Function<in T, out U>): CompletionStage<U>
    fun thenAccept(action: Consumer<in T>): CompletionStage<Unit>
    fun thenRun(action: Runnable): CompletionStage<Unit>
    fun <U, V> thenCombine(other: CompletionStage<out U>, fn: BiFunction<in T, in U, out V>): CompletionStage<V>
    fun <U> thenCompose(fn: Function<in T, out CompletionStage<U>>): CompletionStage<U>
    fun exceptionally(fn: Function<Throwable, out T>): CompletionStage<T>
    fun <U> handle(fn: BiFunction<in T?, Throwable?, out U>): CompletionStage<U>
    fun whenComplete(action: BiConsumer<in T?, in Throwable?>): CompletionStage<T>
    fun toCompletableFuture(): CompletableFuture<T>
}

expect open class CompletableFuture<T>() : Future<T>, CompletionStage<T> {
    open fun complete(value: T): Boolean
    open fun completeExceptionally(ex: Throwable): Boolean
    open fun getNow(valueIfAbsent: T): T
    open fun join(): T
    open fun isCompletedExceptionally(): Boolean
    open fun obtrudeValue(value: T)
    open fun obtrudeException(ex: Throwable)

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean
    override fun isCancelled(): Boolean
    override fun isDone(): Boolean
    override val isCancelled: Boolean
    override val isDone: Boolean
    override fun get(): T
    override fun get(timeout: Long, unit: TimeUnit): T

    override fun <U> thenApply(fn: Function<in T, out U>): CompletableFuture<U>
    override fun thenAccept(action: Consumer<in T>): CompletableFuture<Unit>
    override fun thenRun(action: Runnable): CompletableFuture<Unit>
    override fun <U, V> thenCombine(other: CompletionStage<out U>, fn: BiFunction<in T, in U, out V>): CompletableFuture<V>
    override fun <U> thenCompose(fn: Function<in T, out CompletionStage<U>>): CompletableFuture<U>
    override fun exceptionally(fn: Function<Throwable, out T>): CompletableFuture<T>
    override fun <U> handle(fn: BiFunction<in T?, Throwable?, out U>): CompletableFuture<U>
    override fun whenComplete(action: BiConsumer<in T?, in Throwable?>): CompletableFuture<T>
    override fun toCompletableFuture(): CompletableFuture<T>

    companion object {
        fun <U> completedFuture(value: U): CompletableFuture<U>
        fun <U> supplyAsync(supplier: Supplier<U>): CompletableFuture<U>
        fun <U> supplyAsync(supplier: Supplier<U>, executor: Executor): CompletableFuture<U>
        fun runAsync(runnable: Runnable): CompletableFuture<Unit>
        fun runAsync(runnable: Runnable, executor: Executor): CompletableFuture<Unit>
        fun allOf(vararg cfs: CompletableFuture<*>): CompletableFuture<Unit>
        fun anyOf(vararg cfs: CompletableFuture<*>): CompletableFuture<Any?>
    }
}

expect open class CompletionException : RuntimeException {
    protected constructor()
    protected constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}


expect fun interface Executor {
    fun execute(command: Runnable)
}

expect interface ExecutorService : Executor {
    fun shutdown()
    fun shutdownNow(): List<Runnable>
    fun isShutdown(): Boolean
    fun isTerminated(): Boolean
    fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    fun <T> submit(task: Callable<T>): Future<T>
    fun <T> submit(task: Runnable, result: T): Future<T>
    fun submit(task: Runnable): Future<*>
}

expect fun interface ThreadFactory {
    fun newThread(r: Runnable): Thread
}

expect open class RejectedExecutionException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect interface BlockingQueue<E> : Queue<E> {
    fun put(e: E)
    fun take(): E
    fun poll(timeout: Long, unit: TimeUnit): E?
    fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    fun remainingCapacity(): Int
    fun drainTo(c: MutableCollection<in E>): Int
    fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

expect interface BlockingDeque<E> : BlockingQueue<E>, Deque<E>

expect open class LinkedBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor()
    constructor(capacity: Int)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun poll(): E?
    override fun peek(): E?
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun remainingCapacity(): Int
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

expect open class LinkedBlockingDeque<E> : AbstractQueue<E>, BlockingDeque<E>, Serializable {
    constructor()
    constructor(capacity: Int)
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun remainingCapacity(): Int
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun poll(): E?
    override fun peek(): E?
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
    override fun pop(): E
    override fun push(e: E)
    override fun descendingIterator(): MutableIterator<E>
}

expect open class SynchronousQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor()

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun poll(): E?
    override fun peek(): E?
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun remainingCapacity(): Int
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}

expect open class ThreadPoolExecutor : ExecutorService {
    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory
    )

    override fun execute(command: Runnable)
    override fun shutdown()
    override fun shutdownNow(): List<Runnable>
    override fun isShutdown(): Boolean
    override fun isTerminated(): Boolean
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    override fun <T> submit(task: Callable<T>): Future<T>
    override fun <T> submit(task: Runnable, result: T): Future<T>
    override fun submit(task: Runnable): Future<*>
}

expect open class ConcurrentHashMap<K, V>() : MutableMap<K, V>, Serializable {
    override fun put(key: K, value: V): V?
    override operator fun get(key: K): V?
    override fun remove(key: K): V?
    override fun clear()
    override val size: Int
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun putAll(from: Map<out K, V>)
    open fun keySet(): MutableSet<K>
}

expect open class CopyOnWriteArrayList<E> : MutableList<E>, RandomAccess, Cloneable, Serializable {
    constructor()
    constructor(c: Collection<E>)
    constructor(array: Array<E>)

    open fun addIfAbsent(element: E): Boolean
    open fun addAllAbsent(c: Collection<E>): Int

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun get(index: Int): E
    override fun indexOf(element: E): Int
    override fun lastIndexOf(element: E): Int
    override fun iterator(): MutableIterator<E>
    override fun listIterator(): MutableListIterator<E>
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
    override fun add(element: E): Boolean
    override fun add(index: Int, element: E)
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun clear()
    override fun remove(element: E): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun removeAt(index: Int): E
    override fun set(index: Int, element: E): E
}

expect open class ConcurrentLinkedQueue<E> : AbstractQueue<E>, Queue<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun poll(): E?
    override fun peek(): E?
}

expect object Executors {
    fun newSingleThreadExecutor(): ExecutorService
    fun newSingleThreadExecutor(threadFactory: ThreadFactory): ExecutorService
    fun newFixedThreadPool(nThreads: Int): ExecutorService
    fun newFixedThreadPool(nThreads: Int, threadFactory: ThreadFactory): ExecutorService
    fun newCachedThreadPool(): ExecutorService
    fun newCachedThreadPool(threadFactory: ThreadFactory): ExecutorService
}
