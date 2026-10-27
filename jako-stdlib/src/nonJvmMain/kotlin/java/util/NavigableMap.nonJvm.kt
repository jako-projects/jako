package java.util

actual interface NavigableMap<K, V> : SortedMap<K, V> {
    actual fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>?
    actual fun lowerKey(key: K): K?
    actual fun floorEntry(key: K): MutableMap.MutableEntry<K, V>?
    actual fun floorKey(key: K): K?
    actual fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>?
    actual fun ceilingKey(key: K): K?
    actual fun higherEntry(key: K): MutableMap.MutableEntry<K, V>?
    actual fun higherKey(key: K): K?
    actual fun firstEntry(): MutableMap.MutableEntry<K, V>?
    actual fun lastEntry(): MutableMap.MutableEntry<K, V>?
    actual fun pollFirstEntry(): MutableMap.MutableEntry<K, V>?
    actual fun pollLastEntry(): MutableMap.MutableEntry<K, V>?
}
