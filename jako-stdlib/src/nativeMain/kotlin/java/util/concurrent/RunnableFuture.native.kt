package java.util.concurrent

import java.lang.Runnable

actual interface RunnableFuture<V> : Runnable, Future<V> {
    actual override fun run()
}
