package java.util

actual abstract class Dictionary<K, V> actual constructor() {
    actual abstract fun isEmpty(): Boolean
    actual abstract fun keys(): Enumeration<K>
    actual abstract fun elements(): Enumeration<V>
    actual abstract operator fun get(key: K): V?
    actual abstract fun put(key: K, value: V): V?
    actual abstract fun remove(key: K): V?
}
