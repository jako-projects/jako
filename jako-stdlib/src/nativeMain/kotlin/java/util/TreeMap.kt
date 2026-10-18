package java.util

import java.io.Serializable
import java.lang.Cloneable

actual interface SortedMap<K, V> : MutableMap<K, V> {
    actual fun comparator(): Comparator<in K>?
    actual fun firstKey(): K
    actual fun lastKey(): K
}

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

actual open class TreeMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    private var root: Entry<K, V>? = null
    private var _size: Int = 0
    private val _comparator: Comparator<in K>?

    actual constructor() : super() {
        _comparator = null
    }

    actual constructor(comparator: Comparator<in K>?) : super() {
        _comparator = comparator
    }

    actual constructor(m: Map<out K, V>) : super() {
        _comparator = null
        putAll(m)
    }

    actual constructor(m: SortedMap<K, out V>) : super() {
        _comparator = m.comparator()
        putAll(m)
    }

    actual override fun comparator(): Comparator<in K>? = _comparator

    override val size: Int get() = _size
    override fun isEmpty(): Boolean = _size == 0

    actual override fun firstKey(): K = (getFirstEntry() ?: throw NoSuchElementException()).key
    actual override fun lastKey(): K = (getLastEntry() ?: throw NoSuchElementException()).key

    @Suppress("UNCHECKED_CAST")
    private fun compare(k1: K, k2: K): Int {
        val c = _comparator
        if (c != null) return c.compare(k1, k2)
        if (k1 == null) throw NullPointerException("key is null")
        return (k1 as Comparable<K>).compareTo(k2)
    }

    private fun exportEntry(e: Entry<K, V>?): MutableMap.MutableEntry<K, V>? {
        if (e == null) return null
        return ImmutableEntry(e.key, e.value)
    }

    private fun getLowerEntry(key: K): Entry<K, V>? {
        var p = root
        if (p == null) {
            compare(key, key)
            return null
        }
        while (p != null) {
            val cmp = compare(key, p.key)
            if (cmp > 0) {
                if (p.right != null) {
                    p = p.right
                } else {
                    return p
                }
            } else {
                if (p.left != null) {
                    p = p.left
                } else {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.left) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
            }
        }
        return null
    }

    private fun getFloorEntry(key: K): Entry<K, V>? {
        var p = root
        if (p == null) {
            compare(key, key)
            return null
        }
        while (p != null) {
            val cmp = compare(key, p.key)
            if (cmp > 0) {
                if (p.right != null) {
                    p = p.right
                } else {
                    return p
                }
            } else if (cmp < 0) {
                if (p.left != null) {
                    p = p.left
                } else {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.left) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
            } else {
                return p
            }
        }
        return null
    }

    private fun getCeilingEntry(key: K): Entry<K, V>? {
        var p = root
        if (p == null) {
            compare(key, key)
            return null
        }
        while (p != null) {
            val cmp = compare(key, p.key)
            if (cmp < 0) {
                if (p.left != null) {
                    p = p.left
                } else {
                    return p
                }
            } else if (cmp > 0) {
                if (p.right != null) {
                    p = p.right
                } else {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.right) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
            } else {
                return p
            }
        }
        return null
    }

    private fun getHigherEntry(key: K): Entry<K, V>? {
        var p = root
        if (p == null) {
            compare(key, key)
            return null
        }
        while (p != null) {
            val cmp = compare(key, p.key)
            if (cmp < 0) {
                if (p.left != null) {
                    p = p.left
                } else {
                    return p
                }
            } else {
                if (p.right != null) {
                    p = p.right
                } else {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.right) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
            }
        }
        return null
    }

    actual override fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>? = exportEntry(getLowerEntry(key))
    actual override fun lowerKey(key: K): K? = getLowerEntry(key)?.key
    actual override fun floorEntry(key: K): MutableMap.MutableEntry<K, V>? = exportEntry(getFloorEntry(key))
    actual override fun floorKey(key: K): K? = getFloorEntry(key)?.key
    actual override fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>? = exportEntry(getCeilingEntry(key))
    actual override fun ceilingKey(key: K): K? = getCeilingEntry(key)?.key
    actual override fun higherEntry(key: K): MutableMap.MutableEntry<K, V>? = exportEntry(getHigherEntry(key))
    actual override fun higherKey(key: K): K? = getHigherEntry(key)?.key
    actual override fun firstEntry(): MutableMap.MutableEntry<K, V>? = exportEntry(getFirstEntry())
    actual override fun lastEntry(): MutableMap.MutableEntry<K, V>? = exportEntry(getLastEntry())

    actual override fun pollFirstEntry(): MutableMap.MutableEntry<K, V>? {
        val p = getFirstEntry() ?: return null
        val result = exportEntry(p)
        deleteEntry(p)
        return result
    }

    actual override fun pollLastEntry(): MutableMap.MutableEntry<K, V>? {
        val p = getLastEntry() ?: return null
        val result = exportEntry(p)
        deleteEntry(p)
        return result
    }

    override fun containsKey(key: K): Boolean = getEntry(key) != null

    override fun containsValue(value: V): Boolean {
        var e = getFirstEntry()
        while (e != null) {
            if (e.value == value) return true
            e = successor(e)
        }
        return false
    }

    override operator fun get(key: K): V? = getEntry(key)?.value

    private fun getEntry(key: Any?): Entry<K, V>? {
        if (_comparator != null) {
            return getEntryUsingComparator(key)
        }
        if (key == null) throw NullPointerException("key is null")
        @Suppress("UNCHECKED_CAST")
        val k = key as Comparable<K>
        var p = root
        while (p != null) {
            val cmp = k.compareTo(p.key)
            p = when {
                cmp < 0 -> p.left
                cmp > 0 -> p.right
                else -> return p
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getEntryUsingComparator(key: Any?): Entry<K, V>? {
        val c = _comparator ?: return null
        var p = root
        while (p != null) {
            val cmp = c.compare(key as K, p.key)
            p = when {
                cmp < 0 -> p.left
                cmp > 0 -> p.right
                else -> return p
            }
        }
        return null
    }

    actual override fun put(key: K, value: V): V? {
        var t = root
        if (t == null) {
            if (_comparator != null) {
                _comparator.compare(key, key)
            } else {
                if (key == null) throw NullPointerException("key is null")
                @Suppress("UNCHECKED_CAST")
                (key as Comparable<K>).compareTo(key)
            }
            root = Entry(key, value, null)
            _size = 1
            return null
        }
        var cmp: Int
        var parent: Entry<K, V>
        val c = _comparator
        if (c != null) {
            do {
                parent = t!!
                cmp = c.compare(key, t.key)
                t = when {
                    cmp < 0 -> t.left
                    cmp > 0 -> t.right
                    else -> return t.setValue(value)
                }
            } while (t != null)
        } else {
            if (key == null) throw NullPointerException("key is null")
            @Suppress("UNCHECKED_CAST")
            val k = key as Comparable<K>
            do {
                parent = t!!
                cmp = k.compareTo(t.key)
                t = when {
                    cmp < 0 -> t.left
                    cmp > 0 -> t.right
                    else -> return t.setValue(value)
                }
            } while (t != null)
        }
        val e = Entry(key, value, parent)
        if (cmp < 0) {
            parent.left = e
        } else {
            parent.right = e
        }
        fixAfterInsertion(e)
        _size++
        return null
    }

    override fun remove(key: K): V? {
        val p = getEntry(key) ?: return null
        val oldValue = p.value
        deleteEntry(p)
        return oldValue
    }

    override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }

    override fun clear() {
        _size = 0
        root = null
    }

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet()

    override val keys: MutableSet<K>
        get() = KeySet()

    override val values: MutableCollection<V>
        get() = ValuesCollection()

    private inner class EntryIterator : MutableIterator<MutableMap.MutableEntry<K, V>> {
        private var next: Entry<K, V>? = getFirstEntry()
        private var lastReturned: Entry<K, V>? = null

        override fun hasNext(): Boolean = next != null

        override fun next(): MutableMap.MutableEntry<K, V> {
            val e = next ?: throw NoSuchElementException()
            next = successor(e)
            lastReturned = e
            return e
        }

        override fun remove() {
            val last = lastReturned ?: throw IllegalStateException()
            if (last.left != null && last.right != null) {
                next = last
            }
            deleteEntry(last)
            lastReturned = null
        }
    }

    private inner class KeyIterator : MutableIterator<K> {
        private val entryIterator = EntryIterator()
        override fun hasNext(): Boolean = entryIterator.hasNext()
        override fun next(): K = entryIterator.next().key
        override fun remove() = entryIterator.remove()
    }

    private inner class ValueIterator : MutableIterator<V> {
        private val entryIterator = EntryIterator()
        override fun hasNext(): Boolean = entryIterator.hasNext()
        override fun next(): V = entryIterator.next().value
        override fun remove() = entryIterator.remove()
    }

    private inner class EntrySet : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
        override val size: Int get() = this@TreeMap._size
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntryIterator()
        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
        override fun clear() = this@TreeMap.clear()
        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
            val entry = getEntry(element.key) ?: return false
            return entry.value == element.value
        }
        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
            val entry = getEntry(element.key) ?: return false
            if (entry.value == element.value) {
                deleteEntry(entry)
                return true
            }
            return false
        }
    }

    private inner class KeySet : AbstractMutableSet<K>() {
        override val size: Int get() = this@TreeMap._size
        override fun iterator(): MutableIterator<K> = KeyIterator()
        override fun add(element: K): Boolean = throw UnsupportedOperationException()
        override fun clear() = this@TreeMap.clear()
        override fun contains(element: K): Boolean = containsKey(element)
        override fun remove(element: K): Boolean {
            val prevSize = _size
            this@TreeMap.remove(element)
            return _size < prevSize
        }
    }

    private inner class ValuesCollection : AbstractMutableCollection<V>() {
        override val size: Int get() = this@TreeMap._size
        override fun iterator(): MutableIterator<V> = ValueIterator()
        override fun add(element: V): Boolean = throw UnsupportedOperationException()
        override fun clear() = this@TreeMap.clear()
        override fun contains(element: V): Boolean = containsValue(element)
    }

    companion object {
        private const val RED = false
        private const val BLACK = true
    }

    private fun colorOf(p: Entry<K, V>?): Boolean = p?.color ?: BLACK
    private fun parentOf(p: Entry<K, V>?): Entry<K, V>? = p?.parent
    private fun setColor(p: Entry<K, V>?, c: Boolean) {
        if (p != null) p.color = c
    }
    private fun leftOf(p: Entry<K, V>?): Entry<K, V>? = p?.left
    private fun rightOf(p: Entry<K, V>?): Entry<K, V>? = p?.right

    private fun rotateLeft(p: Entry<K, V>?) {
        if (p != null) {
            val r = p.right
            p.right = r?.left
            if (r?.left != null) {
                r.left!!.parent = p
            }
            r?.parent = p.parent
            if (p.parent == null) {
                root = r
            } else if (p.parent!!.left == p) {
                p.parent!!.left = r
            } else {
                p.parent!!.right = r
            }
            r?.left = p
            p.parent = r
        }
    }

    private fun rotateRight(p: Entry<K, V>?) {
        if (p != null) {
            val l = p.left
            p.left = l?.right
            if (l?.right != null) {
                l.right!!.parent = p
            }
            l?.parent = p.parent
            if (p.parent == null) {
                root = l
            } else if (p.parent!!.right == p) {
                p.parent!!.right = l
            } else {
                p.parent!!.left = l
            }
            l?.right = p
            p.parent = l
        }
    }

    private fun fixAfterInsertion(xEntry: Entry<K, V>) {
        var x: Entry<K, V>? = xEntry
        x!!.color = RED
        while (x != null && x != root && x.parent?.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                val y = rightOf(parentOf(parentOf(x)))
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK)
                    setColor(y, BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    x = parentOf(parentOf(x))
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x)
                        rotateLeft(x)
                    }
                    setColor(parentOf(x), BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    rotateRight(parentOf(parentOf(x)))
                }
            } else {
                val y = leftOf(parentOf(parentOf(x)))
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK)
                    setColor(y, BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    x = parentOf(parentOf(x))
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x)
                        rotateRight(x)
                    }
                    setColor(parentOf(x), BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    rotateLeft(parentOf(parentOf(x)))
                }
            }
        }
        root?.color = BLACK
    }

    private fun deleteEntry(pEntry: Entry<K, V>) {
        var p = pEntry
        _size--

        if (p.left != null && p.right != null) {
            val s = successor(p)!!
            p.key = s.key
            p.value = s.value
            p = s
        }

        val replacement = if (p.left != null) p.left else p.right

        if (replacement != null) {
            replacement.parent = p.parent
            if (p.parent == null) {
                root = replacement
            } else if (p == p.parent!!.left) {
                p.parent!!.left = replacement
            } else {
                p.parent!!.right = replacement
            }

            p.left = null
            p.right = null
            p.parent = null

            if (p.color == BLACK) {
                fixAfterDeletion(replacement)
            }
        } else if (p.parent == null) {
            root = null
        } else {
            if (p.color == BLACK) {
                fixAfterDeletion(p)
            }

            if (p.parent != null) {
                if (p == p.parent!!.left) {
                    p.parent!!.left = null
                } else if (p == p.parent!!.right) {
                    p.parent!!.right = null
                }
                p.parent = null
            }
        }
    }

    private fun fixAfterDeletion(xEntry: Entry<K, V>?) {
        var x = xEntry
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                var sib = rightOf(parentOf(x))
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK)
                    setColor(parentOf(x), RED)
                    rotateLeft(parentOf(x))
                    sib = rightOf(parentOf(x))
                }
                if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED)
                    x = parentOf(x)
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK)
                        setColor(sib, RED)
                        rotateRight(sib)
                        sib = rightOf(parentOf(x))
                    }
                    setColor(sib, colorOf(parentOf(x)))
                    setColor(parentOf(x), BLACK)
                    setColor(rightOf(sib), BLACK)
                    rotateLeft(parentOf(x))
                    x = root
                }
            } else {
                var sib = leftOf(parentOf(x))
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK)
                    setColor(parentOf(x), RED)
                    rotateRight(parentOf(x))
                    sib = leftOf(parentOf(x))
                }
                if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED)
                    x = parentOf(x)
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK)
                        setColor(sib, RED)
                        rotateLeft(sib)
                        sib = leftOf(parentOf(x))
                    }
                    setColor(sib, colorOf(parentOf(x)))
                    setColor(parentOf(x), BLACK)
                    setColor(leftOf(sib), BLACK)
                    rotateRight(parentOf(x))
                    x = root
                }
            }
        }
        setColor(x, BLACK)
    }

    private fun successor(t: Entry<K, V>?): Entry<K, V>? {
        if (t == null) return null
        if (t.right != null) {
            var p = t.right
            while (p?.left != null) {
                p = p.left
            }
            return p
        } else {
            var p = t.parent
            var ch = t
            while (p != null && ch == p.right) {
                ch = p
                p = p.parent
            }
            return p
        }
    }

    private fun getFirstEntry(): Entry<K, V>? {
        var p = root
        if (p != null) {
            while (p?.left != null) {
                p = p.left
            }
        }
        return p
    }

    private fun getLastEntry(): Entry<K, V>? {
        var p = root
        if (p != null) {
            while (p?.right != null) {
                p = p.right
            }
        }
        return p
    }

    private class Entry<K, V>(
        override var key: K,
        override var value: V,
        var parent: Entry<K, V>?
    ) : MutableMap.MutableEntry<K, V> {
        var left: Entry<K, V>? = null
        var right: Entry<K, V>? = null
        var color: Boolean = BLACK

        override fun setValue(newValue: V): V {
            val oldValue = value
            value = newValue
            return oldValue
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }

        override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)

        override fun toString(): String = "$key=$value"
    }

    private class ImmutableEntry<K, V>(
        override val key: K,
        override val value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V = throw UnsupportedOperationException()

        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }

        override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)

        override fun toString(): String = "$key=$value"
    }
}
