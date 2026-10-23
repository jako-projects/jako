@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface SortedMap<K, V> : MutableMap<K, V> {
    fun comparator(): Comparator<in K>?
    fun firstKey(): K
    fun lastKey(): K
}
