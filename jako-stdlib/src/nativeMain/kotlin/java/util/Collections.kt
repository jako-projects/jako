package java.util

actual object Collections {
    actual fun <T> emptyList(): List<T> = kotlin.collections.emptyList()
    actual fun <K, V> emptyMap(): Map<K, V> = kotlin.collections.emptyMap()
    actual fun <T> emptySet(): Set<T> = kotlin.collections.emptySet()
    actual fun <T> singletonList(item: T): List<T> = kotlin.collections.listOf(item)
    actual fun <T> singleton(item: T): Set<T> = kotlin.collections.setOf(item)
    actual fun <K, V> singletonMap(key: K, value: V): Map<K, V> = kotlin.collections.mapOf(key to value)
    actual fun <T> unmodifiableList(list: List<T>): List<T> = list
    actual fun <T> unmodifiableSet(set: Set<T>): Set<T> = set
    actual fun <K, V> unmodifiableMap(map: Map<K, V>): Map<K, V> = map
    actual fun <T> unmodifiableCollection(c: Collection<T>): Collection<T> = c

    actual fun <T> synchronizedList(list: MutableList<T>): MutableList<T> = list
    actual fun <T> synchronizedSet(s: MutableSet<T>): MutableSet<T> = s
    actual fun <K, V> synchronizedMap(m: MutableMap<K, V>): MutableMap<K, V> = m
    actual fun <T> synchronizedCollection(c: MutableCollection<T>): MutableCollection<T> = c

    actual fun <E> newSetFromMap(map: MutableMap<E, Boolean>): MutableSet<E> {
        if (map.isNotEmpty()) throw IllegalArgumentException("Map is non-empty")
        return object : AbstractMutableSet<E>() {
            override val size: Int get() = map.size
            override fun iterator(): MutableIterator<E> = map.keys.iterator()
            override fun add(element: E): Boolean = map.put(element, true) == null
            override fun remove(element: E): Boolean = map.remove(element) != null
            override fun contains(element: E): Boolean = map.containsKey(element)
            override fun clear() = map.clear()
        }
    }

    actual fun <T : Comparable<T>> sort(list: MutableList<T>) = list.sort()
    actual fun <T> sort(list: MutableList<T>, c: Comparator<in T>?) {
        if (c != null) list.sortWith(c) else (list as MutableList<Comparable<Any>>).sort()
    }

    actual fun <T> reverse(list: MutableList<T>) = list.reverse()
    actual fun <T> shuffle(list: MutableList<T>) = list.shuffle()
    actual fun <T> swap(list: MutableList<T>, i: Int, j: Int) {
        val tmp = list[i]
        list[i] = list[j]
        list[j] = tmp
    }

    actual fun <T> fill(list: MutableList<in T>, obj: T) {
        for (i in list.indices) {
            list[i] = obj
        }
    }

    actual fun <T : Comparable<T>> min(coll: Collection<T>): T = coll.minOrNull() ?: throw NoSuchElementException()
    actual fun <T> min(coll: Collection<T>, comp: Comparator<in T>): T = coll.minWithOrNull(comp) ?: throw NoSuchElementException()
    actual fun <T : Comparable<T>> max(coll: Collection<T>): T = coll.maxOrNull() ?: throw NoSuchElementException()
    actual fun <T> max(coll: Collection<T>, comp: Comparator<in T>): T = coll.maxWithOrNull(comp) ?: throw NoSuchElementException()

    actual fun <T> nCopies(n: Int, o: T): List<T> {
        if (n < 0) throw IllegalArgumentException("List length = $n < 0")
        val result = ArrayList<T>(n)
        for (i in 0 until n) {
            result.add(o)
        }
        return result
    }

    actual fun <T> reverseOrder(): Comparator<T> = Comparator { a, b ->
        (b as Comparable<Any>).compareTo(a as Comparable<Any>)
    }

    actual fun <T> reverseOrder(cmp: Comparator<T>): Comparator<T> = Comparator { a, b ->
        cmp.compare(b, a)
    }

    actual fun <T> enumeration(c: Collection<T>): Enumeration<T> {
        val iter = c.iterator()
        return object : Enumeration<T> {
            override fun hasMoreElements(): Boolean = iter.hasNext()
            override fun nextElement(): T = iter.next()
        }
    }

    actual fun <T> list(e: Enumeration<T>): ArrayList<T> {
        val result = ArrayList<T>()
        while (e.hasMoreElements()) {
            result.add(e.nextElement())
        }
        return result
    }

    actual fun frequency(c: Collection<*>, o: Any?): Int {
        var count = 0
        for (e in c) {
            if (e == o) count++
        }
        return count
    }

    actual fun disjoint(c1: Collection<*>, c2: Collection<*>): Boolean {
        val (smaller, larger) = if (c1.size <= c2.size) c1 to c2 else c2 to c1
        for (e in smaller) {
            if (larger.contains(e)) return false
        }
        return true
    }

    actual fun <T> addAll(c: MutableCollection<in T>, vararg elements: T): Boolean {
        var modified = false
        for (e in elements) {
            modified = modified or c.add(e)
        }
        return modified
    }
}
