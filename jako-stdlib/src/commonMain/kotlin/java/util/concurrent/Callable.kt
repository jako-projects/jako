@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect fun interface Callable<V> {
    fun call(): V
}
