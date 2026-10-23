package android.util

@Suppress("UNCHECKED_CAST")
actual final class ArraySet<E> : MutableCollection<E>, MutableSet<E> {
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
            array = arrayOfNulls(capacity)
        }
    }

    actual constructor(set: ArraySet<E>?) : this(0) {
        if (set != null) {
            addAll(set)
        }
    }

    actual constructor(set: Collection<E>?) : this(0) {
        if (set != null) {
            addAll(set)
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
        if (key == array[index]) return index
        var end = index + 1
        while (end < N && hashes[end] == hash) {
            if (key == array[end]) return end
            end++
        }
        var i = index - 1
        while (i >= 0 && hashes[i] == hash) {
            if (key == array[i]) return i
            i--
        }
        return end.inv()
    }

    private fun indexOfNull(): Int {
        val N = _size
        if (N == 0) return 0.inv()
        val index = ContainerHelpers.binarySearch(hashes, N, 0)
        if (index < 0) return index
        if (array[index] == null) return index
        var end = index + 1
        while (end < N && hashes[end] == 0) {
            if (array[end] == null) return end
            end++
        }
        var i = index - 1
        while (i >= 0 && hashes[i] == 0) {
            if (array[i] == null) return i
            i--
        }
        return end.inv()
    }

    actual fun indexOf(key: Any?): Int {
        return if (key == null) indexOfNull() else indexOf(key, key.hashCode())
    }

    actual fun valueAt(index: Int): E = array[index] as E

    actual override fun contains(element: E): Boolean = indexOf(element) >= 0

    actual fun ensureCapacity(minimumCapacity: Int) {
        if (hashes.size < minimumCapacity) {
            hashes = hashes.copyOf(minimumCapacity)
            array = array.copyOf(minimumCapacity)
        }
    }

    actual override fun add(element: E): Boolean {
        val hash: Int
        var index: Int
        if (element == null) {
            hash = 0
            index = indexOfNull()
        } else {
            hash = element.hashCode()
            index = indexOf(element, hash)
        }
        if (index >= 0) {
            return false
        }
        index = index.inv()
        if (_size >= hashes.size) {
            val n = when {
                _size >= 8 -> _size + (_size shr 1)
                _size >= 4 -> 8
                else -> 4
            }
            hashes = hashes.copyOf(n)
            array = array.copyOf(n)
        }
        if (index < _size) {
            hashes.copyInto(hashes, index + 1, index, _size)
            array.copyInto(array, index + 1, index, _size)
        }
        hashes[index] = hash
        array[index] = element
        _size++
        return true
    }

    actual fun addAll(array: ArraySet<out E>) {
        val N = array._size
        ensureCapacity(_size + N)
        if (_size == 0) {
            if (N > 0) {
                array.hashes.copyInto(hashes, 0, 0, N)
                array.array.copyInto(this.array, 0, 0, N)
                _size = N
            }
        } else {
            for (i in 0 until N) {
                add(array.valueAt(i))
            }
        }
    }

    actual override fun addAll(elements: Collection<E>): Boolean {
        ensureCapacity(_size + elements.size)
        var added = false
        for (value in elements) {
            added = added or add(value)
        }
        return added
    }

    actual override fun remove(element: E): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    actual fun removeAt(index: Int): E {
        val old = array[index] as E
        val nsize = _size - 1
        if (index < nsize) {
            hashes.copyInto(hashes, index, index + 1, _size)
            array.copyInto(array, index, index + 1, _size)
        }
        array[nsize] = null
        _size = nsize
        return old
    }

    actual fun removeAll(array: ArraySet<out E>): Boolean {
        val N = array._size
        val originalSize = _size
        for (i in 0 until N) {
            remove(array.valueAt(i) as E)
        }
        return originalSize != _size
    }

    actual override fun removeAll(elements: Collection<E>): Boolean {
        var removed = false
        for (value in elements) {
            removed = removed or remove(value)
        }
        return removed
    }

    actual override fun retainAll(elements: Collection<E>): Boolean {
        var removed = false
        for (i in _size - 1 downTo 0) {
            if (!elements.contains(array[i] as E)) {
                removeAt(i)
                removed = true
            }
        }
        return removed
    }

    actual override fun containsAll(elements: Collection<E>): Boolean {
        for (e in elements) {
            if (!contains(e)) return false
        }
        return true
    }

    actual override fun clear() {
        if (_size != 0) {
            hashes.fill(0, 0, _size)
            array.fill(null, 0, _size)
            _size = 0
        }
    }

    actual override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
        var index = 0
        var canRemove = false
        override fun hasNext(): Boolean = index < _size
        override fun next(): E {
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

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Set<*>) {
            if (size != other.size) return false
            try {
                for (i in 0 until _size) {
                    val mine = valueAt(i)
                    if (!other.contains(mine)) return false
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
        var result = 0
        for (i in 0 until _size) {
            result += h[i]
        }
        return result
    }

    actual override fun toString(): String {
        if (isEmpty()) return "{}"
        val buffer = StringBuilder(_size * 14)
        buffer.append('{')
        for (i in 0 until _size) {
            if (i > 0) buffer.append(", ")
            val value = valueAt(i)
            if (value !== this) buffer.append(value) else buffer.append("(this Set)")
        }
        buffer.append('}')
        return buffer.toString()
    }
}
