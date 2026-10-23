@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect abstract class Dictionary<K, V>() {
    abstract fun isEmpty(): Boolean
    abstract fun keys(): Enumeration<K>
    abstract fun elements(): Enumeration<V>
    abstract operator fun get(key: K): V?
    abstract fun put(key: K, value: V): V?
    abstract fun remove(key: K): V?
}
