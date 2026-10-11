@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

/**
 * Java `Collections` is a non-instantiable utility class. Declaring it as an `object`
 * lets Kotlin Native resolve static imports such as `import java.util.Collections.singletonMap`.
 */
expect object Collections {
    fun <T> emptyList(): List<T>
    fun <K, V> emptyMap(): Map<K, V>
    fun <T> emptySet(): Set<T>
    fun <T> singletonList(item: T): List<T>
    fun <T> singleton(item: T): Set<T>
    fun <K, V> singletonMap(key: K, value: V): Map<K, V>
    fun <T> unmodifiableList(list: List<T>): List<T>
    fun <T> unmodifiableSet(set: Set<T>): Set<T>
    fun <K, V> unmodifiableMap(map: Map<K, V>): Map<K, V>
    fun <T> unmodifiableCollection(c: Collection<T>): Collection<T>
    fun <T> synchronizedList(list: MutableList<T>): MutableList<T>
    fun <T> synchronizedSet(s: MutableSet<T>): MutableSet<T>
    fun <K, V> synchronizedMap(m: MutableMap<K, V>): MutableMap<K, V>
    fun <T> synchronizedCollection(c: MutableCollection<T>): MutableCollection<T>
    fun <E> newSetFromMap(map: MutableMap<E, Boolean>): MutableSet<E>
    fun <T : Comparable<T>> sort(list: MutableList<T>)
    fun <T> sort(list: MutableList<T>, c: Comparator<in T>?)
    fun <T> reverse(list: MutableList<T>)
    fun <T> shuffle(list: MutableList<T>)
    fun <T> swap(list: MutableList<T>, i: Int, j: Int)
    fun <T> fill(list: MutableList<in T>, obj: T)
    fun <T : Comparable<T>> min(coll: Collection<T>): T
    fun <T> min(coll: Collection<T>, comp: Comparator<in T>): T
    fun <T : Comparable<T>> max(coll: Collection<T>): T
    fun <T> max(coll: Collection<T>, comp: Comparator<in T>): T
    fun <T> nCopies(n: Int, o: T): List<T>
    fun <T> reverseOrder(): Comparator<T>
    fun <T> reverseOrder(cmp: Comparator<T>): Comparator<T>
    fun <T> enumeration(c: Collection<T>): Enumeration<T>
    fun <T> list(e: Enumeration<T>): ArrayList<T>
    fun frequency(c: Collection<*>, o: Any?): Int
    fun disjoint(c1: Collection<*>, c2: Collection<*>): Boolean
    fun <T> addAll(c: MutableCollection<in T>, vararg elements: T): Boolean
}
