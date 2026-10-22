@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class Exchanger<V> {
    constructor()
    open fun exchange(x: V): V
    open fun exchange(x: V, timeout: Long, unit: TimeUnit): V
}
