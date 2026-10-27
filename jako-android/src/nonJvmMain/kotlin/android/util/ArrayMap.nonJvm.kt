package android.util

@Suppress("UNCHECKED_CAST")
actual final class ArrayMap<K, V> : MutableMap<K, V> {
    private var hashes: IntArray
    private var array: Array<Any?>
    private var _size: Int = 0

    actual constructor() : this(0)

    actual constructor(capacity: Int) {
        if (capacity <= 0) {
            hashes = IntArray(0)
            array = emptyArray()
        } else {
            hashes = IntArray(capacity)
            array = arrayOfNulls(capacity shl 1)
        }
    }

    actual constructor(map: ArrayMap<K, V>?) : this(0) {
        if (map != null) {
            putAll(map)
        }
    }

    actual override val size: Int
        get() = _size

    actual override fun isEmpty(): Boolean = _size <= 0

    private fun indexOf(key: Any, hash: Int): Int {
        val N = _size
        if (N == 0) return 0.inv()
        val index = ContainerHelpers.binarySearch(hashes, N, hash)
        if (index < 0) return index
        if (key == array[index shl 1]) return index
        var end = index + 1
        while (end < N && hashes[end] == hash) {
            if (key == array[end shl 1]) return end
            end++
        }
        var i = index - 1
        while (i >= 0 && hashes[i] == hash) {
            if (key == array[i shl 1]) return i
            i--
        }
        return end.inv()
    }

    private fun indexOfNull(): Int {
        val N = _size
        if (N == 0) return 0.inv()
        val index = ContainerHelpers.binarySearch(hashes, N, 0)
        if (index < 0) return index
        if (array[index shl 1] == null) return index
        var end = index + 1
        while (end < N && hashes[end] == 0) {
            if (array[end shl 1] == null) return end
            end++
        }
        var i = index - 1
        while (i >= 0 && hashes[i] == 0) {
            if (array[i shl 1] == null) return i
            i--
        }
        return end.inv()
    }

    actual fun indexOfKey(key: Any?): Int {
        return if (key == null) indexOfNull() else indexOf(key, key.hashCode())
    }

    actual fun indexOfValue(value: Any?): Int {
        val N = _size * 2
        val a = array
        if (value == null) {
            for (i in 1 until N step 2) {
                if (a[i] == null) return i shr 1
            }
        } else {
            for (i in 1 until N step 2) {
                if (value == a[i]) return i shr 1
            }
        }
        return -1
    }

    actual override fun containsKey(key: K): Boolean = indexOfKey(key) >= 0

    actual override fun containsValue(value: V): Boolean = indexOfValue(value) >= 0

    actual override fun get(key: K): V? {
        val index = indexOfKey(key)
        return if (index >= 0) array[(index shl 1) + 1] as V else null
    }

    actual fun keyAt(index: Int): K = array[index shl 1] as K

    actual fun valueAt(index: Int): V = array[(index shl 1) + 1] as V

    actual fun setValueAt(index: Int, value: V): V {
        val pos = (index shl 1) + 1
        val old = array[pos] as V
        array[pos] = value
        return old
    }

    actual fun ensureCapacity(minimumCapacity: Int) {
        if (hashes.size < minimumCapacity) {
            hashes = hashes.copyOf(minimumCapacity)
            array = array.copyOf(minimumCapacity shl 1)
        }
    }

    actual override fun put(key: K, value: V): V? {
        val hash: Int
        var index: Int
        if (key == null) {
            hash = 0
            index = indexOfNull()
        } else {
            hash = key.hashCode()
            index = indexOf(key, hash)
        }
        if (index >= 0) {
            val pos = (index shl 1) + 1
            val old = array[pos] as V
            array[pos] = value
            return old
        }
        index = index.inv()
        if (_size >= hashes.size) {
            val n = when {
                _size >= 8 -> _size + (_size shr 1)
                _size >= 4 -> 8
                else -> 4
            }
            hashes = hashes.copyOf(n)
            array = array.copyOf(n shl 1)
        }
        if (index < _size) {
            hashes.copyInto(hashes, index + 1, index, _size)
            array.copyInto(array, (index + 1) shl 1, index shl 1, _size shl 1)
        }
        hashes[index] = hash
        array[index shl 1] = key
        array[(index shl 1) + 1] = value
        _size++
        return null
    }

    actual fun putAll(array: ArrayMap<out K, out V>) {
        val N = array._size
        ensureCapacity(_size + N)
        if (_size == 0) {
            if (N > 0) {
                array.hashes.copyInto(hashes, 0, 0, N)
                array.array.copyInto(this.array, 0, 0, N shl 1)
                _size = N
            }
        } else {
            for (i in 0 until N) {
                put(array.keyAt(i), array.valueAt(i))
            }
        }
    }

    actual override fun putAll(from: Map<out K, V>) {
        ensureCapacity(_size + from.size)
        for ((k, v) in from) {
            put(k, v)
        }
    }

    actual override fun remove(key: K): V? {
        val index = indexOfKey(key)
        return if (index >= 0) removeAt(index) else null
    }

    actual fun removeAt(index: Int): V {
        val old = array[(index shl 1) + 1] as V
        val nsize = _size - 1
        if (index < nsize) {
            hashes.copyInto(hashes, index, index + 1, _size)
            array.copyInto(array, index shl 1, (index + 1) shl 1, _size shl 1)
        }
        array[nsize shl 1] = null
        array[(nsize shl 1) + 1] = null
        _size = nsize
        return old
    }

    actual override fun clear() {
        if (_size > 0) {
            hashes.fill(0, 0, _size)
            array.fill(null, 0, _size shl 1)
            _size = 0
        }
    }

    actual fun containsAll(collection: Collection<*>): Boolean {
        for (item in collection) {
            if (!containsKey(item as K)) return false
        }
        return true
    }

    actual fun removeAll(collection: Collection<*>): Boolean {
        val oldSize = _size
        for (item in collection) {
            remove(item as K)
        }
        return oldSize != _size
    }

    actual fun retainAll(collection: Collection<*>): Boolean {
        val oldSize = _size
        for (i in _size - 1 downTo 0) {
            if (!collection.contains(keyAt(i))) {
                removeAt(i)
            }
        }
        return oldSize != _size
    }

    actual override val keys: MutableSet<K>
        get() = object : AbstractMutableSet<K>() {
            override val size: Int get() = _size
            override fun add(element: K): Boolean = throw UnsupportedOperationException()
            override fun contains(element: K): Boolean = containsKey(element)
            override fun clear() = this@ArrayMap.clear()
            override fun remove(element: K): Boolean {
                val idx = indexOfKey(element)
                if (idx >= 0) {
                    removeAt(idx)
                    return true
                }
                return false
            }
            override fun iterator(): MutableIterator<K> = object : MutableIterator<K> {
                var index = 0
                var canRemove = false
                override fun hasNext(): Boolean = index < _size
                override fun next(): K {
                    if (!hasNext()) throw NoSuchElementException()
                    val res = keyAt(index)
                    index++
                    canRemove = true
                    return res
                }
                override fun remove() {
                    check(canRemove)
                    index--
                    removeAt(index)
                    canRemove = false
                }
            }
        }

    actual override val values: MutableCollection<V>
        get() = object : AbstractMutableCollection<V>() {
            override val size: Int get() = _size
            override fun add(element: V): Boolean = throw UnsupportedOperationException()
            override fun contains(element: V): Boolean = containsValue(element)
            override fun clear() = this@ArrayMap.clear()
            override fun iterator(): MutableIterator<V> = object : MutableIterator<V> {
                var index = 0
                var canRemove = false
                override fun hasNext(): Boolean = index < _size
                override fun next(): V {
                    if (!hasNext()) throw NoSuchElementException()
                    val res = valueAt(index)
                    index++
                    canRemove = true
                    return res
                }
                override fun remove() {
                    check(canRemove)
                    index--
                    removeAt(index)
                    canRemove = false
                }
            }
        }

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
            override val size: Int get() = _size
            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
            override fun clear() = this@ArrayMap.clear()
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                var index = -1
                var entryValid = false
                override fun hasNext(): Boolean = index + 1 < _size
                override fun next(): MutableMap.MutableEntry<K, V> {
                    if (!hasNext()) throw NoSuchElementException()
                    index++
                    entryValid = true
                    return object : MutableMap.MutableEntry<K, V> {
                        override val key: K get() = keyAt(index)
                        override val value: V get() = valueAt(index)
                        override fun setValue(newValue: V): V = setValueAt(index, newValue)
                        override fun equals(other: Any?): Boolean {
                            if (other !is Map.Entry<*, *>) return false
                            return other.key == key && other.value == value
                        }
                        override fun hashCode(): Int {
                            return (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
                        }
                        override fun toString(): String = "$key=$value"
                    }
                }
                override fun remove() {
                    check(entryValid)
                    removeAt(index)
                    index--
                    entryValid = false
                }
            }
        }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Map<*, *>) {
            if (size != other.size) return false
            try {
                for (i in 0 until _size) {
                    val key = keyAt(i)
                    val mine = valueAt(i)
                    val theirs = (other as Map<Any?, Any?>)[key]
                    if (mine == null) {
                        if (theirs != null || !other.containsKey(key)) return false
                    } else if (mine != theirs) {
                        return false
                    }
                }
            } catch (_: NullPointerException) {
                return false
            } catch (_: ClassCastException) {
                return false
            }
            return true
        }
        return false
    }

    actual override fun hashCode(): Int {
        val h = hashes
        val a = array
        var result = 0
        var i = 0
        var v = 1
        val s = _size
        while (i < s) {
            val value = a[v]
            result += h[i] xor (value?.hashCode() ?: 0)
            i++
            v += 2
        }
        return result
    }

    actual override fun toString(): String {
        if (isEmpty()) return "{}"
        val buffer = StringBuilder(_size * 28)
        buffer.append('{')
        for (i in 0 until _size) {
            if (i > 0) buffer.append(", ")
            val key = keyAt(i)
            if (key !== this) buffer.append(key) else buffer.append("(this Map)")
            buffer.append('=')
            val value = valueAt(i)
            if (value !== this) buffer.append(value) else buffer.append("(this Map)")
        }
        buffer.append('}')
        return buffer.toString()
    }
}
