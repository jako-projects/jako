package java.util.concurrent

import java.lang.Runnable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

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

actual open class CompletableFuture<T> actual constructor() : Future<T>, CompletionStage<T> {
    private val latch = CountDownLatch(1)
    private var result: Any? = null
    private var exception: Throwable? = null
    private var isDoneFlag = false
    private var isCancelledFlag = false
    private val callbacks = mutableListOf<() -> Unit>()

    actual open fun complete(value: T): Boolean {
        if (isDoneFlag) return false
        result = value
        isDoneFlag = true
        latch.countDown()
        triggerCallbacks()
        return true
    }

    actual open fun completeExceptionally(ex: Throwable): Boolean {
        if (isDoneFlag) return false
        exception = ex
        isDoneFlag = true
        latch.countDown()
        triggerCallbacks()
        return true
    }

    private fun triggerCallbacks() {
        val list = ArrayList(callbacks)
        callbacks.clear()
        for (cb in list) {
            cb()
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun getNow(valueIfAbsent: T): T {
        if (isDoneFlag) {
            if (exception != null) throw CompletionException(exception)
            if (isCancelledFlag) throw CancellationException()
            return result as T
        }
        return valueIfAbsent
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun join(): T {
        latch.await()
        if (exception != null) throw CompletionException(exception)
        if (isCancelledFlag) throw CancellationException()
        return result as T
    }

    actual open fun isCompletedExceptionally(): Boolean = isDoneFlag && exception != null

    actual open fun obtrudeValue(value: T) {
        result = value
        exception = null
        isDoneFlag = true
        isCancelledFlag = false
        latch.countDown()
    }

    actual open fun obtrudeException(ex: Throwable) {
        exception = ex
        result = null
        isDoneFlag = true
        isCancelledFlag = false
        latch.countDown()
    }

    actual override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (isDoneFlag) return false
        isCancelledFlag = true
        isDoneFlag = true
        latch.countDown()
        triggerCallbacks()
        return true
    }

    actual override fun isCancelled(): Boolean = isCancelledFlag

    actual override fun isDone(): Boolean = isDoneFlag

    actual override val isCancelled: Boolean get() = isCancelled()

    actual override val isDone: Boolean get() = isDone()

    @Suppress("UNCHECKED_CAST")
    actual override fun get(): T {
        latch.await()
        if (isCancelledFlag) throw CancellationException("CompletableFuture was cancelled")
        if (exception != null) throw ExecutionException(exception?.message, exception)
        return result as T
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun get(timeout: Long, unit: TimeUnit): T {
        if (!latch.await(timeout, unit)) {
            throw TimeoutException("Timed out waiting for CompletableFuture")
        }
        if (isCancelledFlag) throw CancellationException("CompletableFuture was cancelled")
        if (exception != null) throw ExecutionException(exception?.message, exception)
        return result as T
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <U> thenApply(fn: Function<in T, out U>): CompletableFuture<U> {
        val next = CompletableFuture<U>()
        val action: () -> Unit = {
            if (exception != null) {
                next.completeExceptionally(exception!!)
            } else if (isCancelledFlag) {
                next.cancel(false)
            } else {
                try {
                    val u = fn.apply(result as T)
                    next.complete(u)
                } catch (t: Throwable) {
                    next.completeExceptionally(t)
                }
            }
        }
        if (isDoneFlag) action() else callbacks.add(action)
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun thenAccept(action: Consumer<in T>): CompletableFuture<Unit> {
        val next = CompletableFuture<Unit>()
        val act: () -> Unit = {
            if (exception != null) {
                next.completeExceptionally(exception!!)
            } else if (isCancelledFlag) {
                next.cancel(false)
            } else {
                try {
                    action.accept(result as T)
                    next.complete(Unit)
                } catch (t: Throwable) {
                    next.completeExceptionally(t)
                }
            }
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    actual override fun thenRun(action: Runnable): CompletableFuture<Unit> {
        val next = CompletableFuture<Unit>()
        val act: () -> Unit = {
            if (exception != null) {
                next.completeExceptionally(exception!!)
            } else if (isCancelledFlag) {
                next.cancel(false)
            } else {
                try {
                    action.run()
                    next.complete(Unit)
                } catch (t: Throwable) {
                    next.completeExceptionally(t)
                }
            }
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <U, V> thenCombine(
        other: CompletionStage<out U>,
        fn: BiFunction<in T, in U, out V>
    ): CompletableFuture<V> {
        val next = CompletableFuture<V>()
        val otherCf = other.toCompletableFuture()
        val checkBoth: () -> Unit = {
            if (this.isDoneFlag && otherCf.isDone()) {
                if (this.exception != null) {
                    next.completeExceptionally(this.exception!!)
                } else if (otherCf.exception != null) {
                    next.completeExceptionally(otherCf.exception!!)
                } else if (this.isCancelledFlag || otherCf.isCancelledFlag) {
                    next.cancel(false)
                } else {
                    try {
                        val res = fn.apply(this.result as T, otherCf.result as U)
                        next.complete(res)
                    } catch (t: Throwable) {
                        next.completeExceptionally(t)
                    }
                }
            }
        }
        this.whenComplete { _, _ -> checkBoth() }
        otherCf.whenComplete { _, _ -> checkBoth() }
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <U> thenCompose(fn: Function<in T, out CompletionStage<U>>): CompletableFuture<U> {
        val next = CompletableFuture<U>()
        val act: () -> Unit = {
            if (exception != null) {
                next.completeExceptionally(exception!!)
            } else if (isCancelledFlag) {
                next.cancel(false)
            } else {
                try {
                    val stage = fn.apply(result as T)
                    stage.toCompletableFuture().whenComplete { u, ex ->
                        if (ex != null) next.completeExceptionally(ex)
                        else if (u != null) next.complete(u)
                        else next.complete(null as U)
                    }
                } catch (t: Throwable) {
                    next.completeExceptionally(t)
                }
            }
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun exceptionally(fn: Function<Throwable, out T>): CompletableFuture<T> {
        val next = CompletableFuture<T>()
        val act: () -> Unit = {
            if (exception != null) {
                try {
                    val fallback = fn.apply(exception!!)
                    next.complete(fallback)
                } catch (t: Throwable) {
                    next.completeExceptionally(t)
                }
            } else if (isCancelledFlag) {
                next.cancel(false)
            } else {
                next.complete(result as T)
            }
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <U> handle(fn: BiFunction<in T?, Throwable?, out U>): CompletableFuture<U> {
        val next = CompletableFuture<U>()
        val act: () -> Unit = {
            try {
                val res = fn.apply(if (exception == null) result as T? else null, exception)
                next.complete(res)
            } catch (t: Throwable) {
                next.completeExceptionally(t)
            }
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun whenComplete(action: BiConsumer<in T?, in Throwable?>): CompletableFuture<T> {
        val next = CompletableFuture<T>()
        val act: () -> Unit = {
            try {
                action.accept(if (exception == null) result as T? else null, exception)
            } catch (_: Throwable) {}
            if (exception != null) next.completeExceptionally(exception!!)
            else if (isCancelledFlag) next.cancel(false)
            else next.complete(result as T)
        }
        if (isDoneFlag) act() else callbacks.add(act)
        return next
    }

    actual override fun toCompletableFuture(): CompletableFuture<T> = this

    actual companion object {
        actual fun <U> completedFuture(value: U): CompletableFuture<U> {
            val cf = CompletableFuture<U>()
            cf.complete(value)
            return cf
        }

        actual fun <U> supplyAsync(supplier: Supplier<U>): CompletableFuture<U> {
            return supplyAsync(supplier, Executors.newCachedThreadPool())
        }

        actual fun <U> supplyAsync(supplier: Supplier<U>, executor: Executor): CompletableFuture<U> {
            val cf = CompletableFuture<U>()
            executor.execute {
                try {
                    cf.complete(supplier.get())
                } catch (t: Throwable) {
                    cf.completeExceptionally(t)
                }
            }
            return cf
        }

        actual fun runAsync(runnable: Runnable): CompletableFuture<Unit> {
            return runAsync(runnable, Executors.newCachedThreadPool())
        }

        actual fun runAsync(runnable: Runnable, executor: Executor): CompletableFuture<Unit> {
            val cf = CompletableFuture<Unit>()
            executor.execute {
                try {
                    runnable.run()
                    cf.complete(Unit)
                } catch (t: Throwable) {
                    cf.completeExceptionally(t)
                }
            }
            return cf
        }

        actual fun allOf(vararg cfs: CompletableFuture<*>): CompletableFuture<Unit> {
            if (cfs.isEmpty()) return completedFuture(Unit)
            val result = CompletableFuture<Unit>()
            val count = CountDownLatch(cfs.size)
            for (cf in cfs) {
                cf.whenComplete { _, ex ->
                    if (ex != null) result.completeExceptionally(ex)
                    count.countDown()
                    if (count.getCount() == 0L && !result.isDone()) {
                        result.complete(Unit)
                    }
                }
            }
            return result
        }

        actual fun anyOf(vararg cfs: CompletableFuture<*>): CompletableFuture<Any?> {
            val result = CompletableFuture<Any?>()
            if (cfs.isEmpty()) return result
            for (cf in cfs) {
                cf.whenComplete { res, ex ->
                    if (ex != null) result.completeExceptionally(ex)
                    else result.complete(res)
                }
            }
            return result
        }
    }
}

actual open class CompletionException : RuntimeException {
    protected actual constructor() : super()
    protected actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

