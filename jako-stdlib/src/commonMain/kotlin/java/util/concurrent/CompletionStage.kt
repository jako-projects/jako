@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

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
