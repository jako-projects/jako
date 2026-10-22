@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect interface RunnableFuture<V> : Runnable, Future<V> {
    override fun run()
}
