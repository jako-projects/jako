package java.util

actual interface SortedMap<K, V> : MutableMap<K, V> {
    actual fun comparator(): Comparator<in K>?
    actual fun firstKey(): K
    actual fun lastKey(): K
}
