@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

/**
 * [Nullability Decision] Navigation methods (lower*, floor*, ceiling*, higher*, firstEntry, lastEntry, poll*)
 * return null when no matching key/entry exists in the map.
 */
expect interface NavigableMap<K, V> : SortedMap<K, V> {
    fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun lowerKey(key: K): K?
    fun floorEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun floorKey(key: K): K?
    fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun ceilingKey(key: K): K?
    fun higherEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun higherKey(key: K): K?
    fun firstEntry(): MutableMap.MutableEntry<K, V>?
    fun lastEntry(): MutableMap.MutableEntry<K, V>?
    fun pollFirstEntry(): MutableMap.MutableEntry<K, V>?
    fun pollLastEntry(): MutableMap.MutableEntry<K, V>?
}
