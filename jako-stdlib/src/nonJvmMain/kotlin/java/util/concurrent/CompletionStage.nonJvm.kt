package java.util.concurrent

import java.lang.Runnable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

actual interface CompletionStage<T> {
    actual fun <U> thenApply(fn: Function<in T, out U>): CompletionStage<U>
    actual fun thenAccept(action: Consumer<in T>): CompletionStage<Unit>
    actual fun thenRun(action: Runnable): CompletionStage<Unit>
    actual fun <U, V> thenCombine(other: CompletionStage<out U>, fn: BiFunction<in T, in U, out V>): CompletionStage<V>
    actual fun <U> thenCompose(fn: Function<in T, out CompletionStage<U>>): CompletionStage<U>
    actual fun exceptionally(fn: Function<Throwable, out T>): CompletionStage<T>
    actual fun <U> handle(fn: BiFunction<in T?, Throwable?, out U>): CompletionStage<U>
    actual fun whenComplete(action: BiConsumer<in T?, in Throwable?>): CompletionStage<T>
    actual fun toCompletableFuture(): CompletableFuture<T>
}
