@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

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
