package android.util

import java.lang.Cloneable
import java.util.LinkedHashMap

actual open class LruCache<K, V> actual constructor(maxSize: Int) {
    private val map: LinkedHashMap<K, V>
    private var size: Int = 0
    private var maxSize: Int = maxSize
    private var putCount: Int = 0
    private var createCount: Int = 0
    private var evictionCount: Int = 0
    private var hitCount: Int = 0
    private var missCount: Int = 0

    init {
        require(maxSize > 0) { "maxSize <= 0" }
        this.map = LinkedHashMap(0, 0.75f, true)
    }

    actual open fun resize(maxSize: Int) {
        require(maxSize > 0) { "maxSize <= 0" }
        synchronized(this) {
            this.maxSize = maxSize
        }
        trimToSize(maxSize)
    }

    actual operator fun get(key: K): V? {
        if (key == null) {
            throw NullPointerException("key == null")
        }
        synchronized(this) {
            val v = map[key]
            if (v != null) {
                hitCount++
                return v
            }
            missCount++
        }

        val createdValue = create(key) ?: return null

        val mapValue: V? = synchronized(this) {
            createCount++
            val prev = map.put(key, createdValue)
            if (prev != null) {
                map.put(key, prev)
            } else {
                size += safeSizeOf(key, createdValue)
            }
            prev
        }

        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue)
            return mapValue
        } else {
            trimToSize(maxSize)
            return createdValue
        }
    }

    actual fun put(key: K, value: V): V? {
        if (key == null || value == null) {
            throw NullPointerException("key == null || value == null")
        }
        val previous: V? = synchronized(this) {
            putCount++
            size += safeSizeOf(key, value)
            val prev = map.put(key, value)
            if (prev != null) {
                size -= safeSizeOf(key, prev)
            }
            prev
        }
        if (previous != null) {
            entryRemoved(false, key, previous, value)
        }
        trimToSize(maxSize)
        return previous
    }

    actual open fun trimToSize(maxSize: Int) {
        while (true) {
            val evictedPair = synchronized(this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw IllegalStateException("${this::class.simpleName}.sizeOf() is reporting inconsistent results!")
                }
                if (size <= maxSize || map.isEmpty()) {
                    null
                } else {
                    val toEvict = map.entries.firstOrNull()
                    if (toEvict == null) {
                        null
                    } else {
                        val k = toEvict.key
                        val v = toEvict.value
                        map.remove(k)
                        size -= safeSizeOf(k, v)
                        evictionCount++
                        Pair(k, v)
                    }
                }
            } ?: break
            entryRemoved(true, evictedPair.first, evictedPair.second, null)
        }
    }

    actual fun remove(key: K): V? {
        if (key == null) {
            throw NullPointerException("key == null")
        }
        val previous: V? = synchronized(this) {
            val prev = map.remove(key)
            if (prev != null) {
                size -= safeSizeOf(key, prev)
            }
            prev
        }
        if (previous != null) {
            entryRemoved(false, key, previous, null)
        }
        return previous
    }

    protected actual open fun entryRemoved(evicted: Boolean, key: K, oldValue: V, newValue: V?) {}

    protected actual open fun create(key: K): V? = null

    private fun safeSizeOf(key: K, value: V): Int {
        val result = sizeOf(key, value)
        if (result < 0) {
            throw IllegalStateException("Negative size: $key=$value")
        }
        return result
    }

    protected actual open fun sizeOf(key: K, value: V): Int = 1

    actual fun evictAll() {
        trimToSize(-1)
    }

    actual fun size(): Int = synchronized(this) { size }

    actual fun maxSize(): Int = synchronized(this) { maxSize }

    actual fun hitCount(): Int = synchronized(this) { hitCount }

    actual fun missCount(): Int = synchronized(this) { missCount }

    actual fun createCount(): Int = synchronized(this) { createCount }

    actual fun putCount(): Int = synchronized(this) { putCount }

    actual fun evictionCount(): Int = synchronized(this) { evictionCount }

    actual fun snapshot(): MutableMap<K, V> = synchronized(this) { LinkedHashMap(map) }

    actual override fun toString(): String = synchronized(this) {
        val accesses = hitCount + missCount
        val hitPercent = if (accesses != 0) (100 * hitCount / accesses) else 0
        "LruCache[maxSize=$maxSize,hits=$hitCount,misses=$missCount,hitRate=$hitPercent%]"
    }
}

private object ContainerHelpers {
    val DELETED = Any()

    fun binarySearch(array: IntArray, size: Int, value: Int): Int {
        var lo = 0
        var hi = size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val midVal = array[mid]
            if (midVal < value) {
                lo = mid + 1
            } else if (midVal > value) {
                hi = mid - 1
            } else {
                return mid
            }
        }
        return lo.inv()
    }

    fun binarySearch(array: LongArray, size: Int, value: Long): Int {
        var lo = 0
        var hi = size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val midVal = array[mid]
            if (midVal < value) {
                lo = mid + 1
            } else if (midVal > value) {
                hi = mid - 1
            } else {
                return mid
            }
        }
        return lo.inv()
    }
}

@Suppress("UNCHECKED_CAST")
actual open class SparseArray<E> actual constructor(initialCapacity: Int) : Cloneable {
    private var garbage = false
    private var keys: IntArray
    private var values: Array<Any?>
    private var size: Int = 0

    actual constructor() : this(10)

    init {
        if (initialCapacity == 0) {
            keys = IntArray(0)
            values = emptyArray()
        } else {
            keys = IntArray(initialCapacity)
            values = arrayOfNulls(initialCapacity)
        }
    }

    actual open fun clone(): SparseArray<E> {
        val clone = SparseArray<E>(0)
        clone.garbage = garbage
        clone.keys = keys.copyOf()
        clone.values = values.copyOf()
        clone.size = size
        return clone
    }

    actual open operator fun get(key: Int): E? = get(key, null as E)

    actual open fun get(key: Int, valueIfKeyNotFound: E): E {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        return if (i < 0 || values[i] === ContainerHelpers.DELETED) {
            valueIfKeyNotFound
        } else {
            values[i] as E
        }
    }

    actual open fun delete(key: Int) {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0 && values[i] !== ContainerHelpers.DELETED) {
            values[i] = ContainerHelpers.DELETED
            garbage = true
        }
    }

    actual open fun remove(key: Int) {
        delete(key)
    }

    actual open fun removeAt(index: Int) {
        if (index >= size) {
            throw IndexOutOfBoundsException("Index: $index")
        }
        if (values[index] !== ContainerHelpers.DELETED) {
            values[index] = ContainerHelpers.DELETED
            garbage = true
        }
    }

    actual open fun removeAtRange(index: Int, size: Int) {
        val end = minOf(this.size, index + size)
        for (i in index until end) {
            removeAt(i)
        }
    }

    private fun gc() {
        val n = size
        var o = 0
        val k = keys
        val v = values
        for (i in 0 until n) {
            val value = v[i]
            if (value !== ContainerHelpers.DELETED) {
                if (i != o) {
                    k[o] = k[i]
                    v[o] = value
                    v[i] = null
                }
                o++
            }
        }
        garbage = false
        size = o
    }

    actual open fun put(key: Int, value: E) {
        var i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            values[i] = value
        } else {
            i = i.inv()
            if (i < size && values[i] === ContainerHelpers.DELETED) {
                keys[i] = key
                values[i] = value
                return
            }
            if (garbage && size >= keys.size) {
                gc()
                i = ContainerHelpers.binarySearch(keys, size, key).inv()
            }
            if (size >= keys.size) {
                val newCap = maxOf(4, if (size < 6) size * 2 else size + (size shr 1))
                keys = keys.copyOf(newCap)
                values = values.copyOf(newCap)
            }
            if (size - i != 0) {
                keys.copyInto(keys, i + 1, i, size)
                values.copyInto(values, i + 1, i, size)
            }
            keys[i] = key
            values[i] = value
            size++
        }
    }

    actual open fun size(): Int {
        if (garbage) gc()
        return size
    }

    actual open fun keyAt(index: Int): Int {
        if (garbage) gc()
        return keys[index]
    }

    actual open fun valueAt(index: Int): E {
        if (garbage) gc()
        return values[index] as E
    }

    actual open fun setValueAt(index: Int, value: E) {
        if (garbage) gc()
        values[index] = value
    }

    actual open fun indexOfKey(key: Int): Int {
        if (garbage) gc()
        return ContainerHelpers.binarySearch(keys, size, key)
    }

    actual open fun indexOfValue(value: E): Int {
        if (garbage) gc()
        for (i in 0 until size) {
            if (values[i] === value) return i
        }
        return -1
    }

    actual open fun clear() {
        val n = size
        for (i in 0 until n) {
            values[i] = null
        }
        size = 0
        garbage = false
    }

    actual open fun append(key: Int, value: E) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value)
            return
        }
        if (garbage && size >= keys.size) {
            gc()
        }
        val pos = size
        if (pos >= keys.size) {
            val newCap = maxOf(4, if (pos < 6) pos * 2 else pos + (pos shr 1))
            keys = keys.copyOf(newCap)
            values = values.copyOf(newCap)
        }
        keys[pos] = key
        values[pos] = value
        size = pos + 1
    }

    actual override fun toString(): String {
        if (size() <= 0) return "{}"
        val buffer = StringBuilder(size * 28)
        buffer.append('{')
        for (i in 0 until size) {
            if (i > 0) buffer.append(", ")
            val key = keyAt(i)
            buffer.append(key)
            buffer.append('=')
            val value = valueAt(i)
            if (value !== this) {
                buffer.append(value)
            } else {
                buffer.append("(this Map)")
            }
        }
        buffer.append('}')
        return buffer.toString()
    }
}

actual open class SparseBooleanArray actual constructor(initialCapacity: Int) : Cloneable {
    private var keys: IntArray = if (initialCapacity == 0) IntArray(0) else IntArray(initialCapacity)
    private var values: BooleanArray = if (initialCapacity == 0) BooleanArray(0) else BooleanArray(initialCapacity)
    private var size: Int = 0

    actual constructor() : this(10)

    actual open fun clone(): SparseBooleanArray {
        val clone = SparseBooleanArray(0)
        clone.keys = keys.copyOf()
        clone.values = values.copyOf()
        clone.size = size
        return clone
    }

    actual open operator fun get(key: Int): Boolean = get(key, false)

    actual open fun get(key: Int, valueIfKeyNotFound: Boolean): Boolean {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        return if (i < 0) valueIfKeyNotFound else values[i]
    }

    actual open fun delete(key: Int) {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            keys.copyInto(keys, i, i + 1, size)
            values.copyInto(values, i, i + 1, size)
            size--
        }
    }

    actual open fun removeAt(index: Int) {
        keys.copyInto(keys, index, index + 1, size)
        values.copyInto(values, index, index + 1, size)
        size--
    }

    actual open fun put(key: Int, value: Boolean) {
        var i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            values[i] = value
        } else {
            i = i.inv()
            if (size >= keys.size) {
                val newCap = maxOf(4, if (size < 6) size * 2 else size + (size shr 1))
                keys = keys.copyOf(newCap)
                values = values.copyOf(newCap)
            }
            if (size - i != 0) {
                keys.copyInto(keys, i + 1, i, size)
                values.copyInto(values, i + 1, i, size)
            }
            keys[i] = key
            values[i] = value
            size++
        }
    }

    actual open fun size(): Int = size

    actual open fun keyAt(index: Int): Int = keys[index]

    actual open fun valueAt(index: Int): Boolean = values[index]

    actual open fun setValueAt(index: Int, value: Boolean) {
        values[index] = value
    }

    actual open fun indexOfKey(key: Int): Int = ContainerHelpers.binarySearch(keys, size, key)

    actual open fun indexOfValue(value: Boolean): Int {
        for (i in 0 until size) {
            if (values[i] == value) return i
        }
        return -1
    }

    actual open fun clear() {
        size = 0
    }

    actual open fun append(key: Int, value: Boolean) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value)
            return
        }
        val pos = size
        if (pos >= keys.size) {
            val newCap = maxOf(4, if (pos < 6) pos * 2 else pos + (pos shr 1))
            keys = keys.copyOf(newCap)
            values = values.copyOf(newCap)
        }
        keys[pos] = key
        values[pos] = value
        size = pos + 1
    }

    actual override fun hashCode(): Int {
        var hashCode = size
        for (i in 0 until size) {
            hashCode = 31 * hashCode + keys[i] xor if (values[i]) 1 else 0
        }
        return hashCode
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SparseBooleanArray) return false
        if (size != other.size) return false
        for (i in 0 until size) {
            if (keys[i] != other.keys[i]) return false
            if (values[i] != other.values[i]) return false
        }
        return true
    }

    actual override fun toString(): String {
        if (size() <= 0) return "{}"
        val buffer = StringBuilder(size * 28)
        buffer.append('{')
        for (i in 0 until size) {
            if (i > 0) buffer.append(", ")
            buffer.append(keyAt(i))
            buffer.append('=')
            buffer.append(valueAt(i))
        }
        buffer.append('}')
        return buffer.toString()
    }
}

actual open class SparseIntArray actual constructor(initialCapacity: Int) : Cloneable {
    private var keys: IntArray = if (initialCapacity == 0) IntArray(0) else IntArray(initialCapacity)
    private var values: IntArray = if (initialCapacity == 0) IntArray(0) else IntArray(initialCapacity)
    private var size: Int = 0

    actual constructor() : this(10)

    actual open fun clone(): SparseIntArray {
        val clone = SparseIntArray(0)
        clone.keys = keys.copyOf()
        clone.values = values.copyOf()
        clone.size = size
        return clone
    }

    actual open operator fun get(key: Int): Int = get(key, 0)

    actual open fun get(key: Int, valueIfKeyNotFound: Int): Int {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        return if (i < 0) valueIfKeyNotFound else values[i]
    }

    actual open fun delete(key: Int) {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) removeAt(i)
    }

    actual open fun removeAt(index: Int) {
        keys.copyInto(keys, index, index + 1, size)
        values.copyInto(values, index, index + 1, size)
        size--
    }

    actual open fun put(key: Int, value: Int) {
        var i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            values[i] = value
        } else {
            i = i.inv()
            if (size >= keys.size) {
                val newCap = maxOf(4, if (size < 6) size * 2 else size + (size shr 1))
                keys = keys.copyOf(newCap)
                values = values.copyOf(newCap)
            }
            if (size - i != 0) {
                keys.copyInto(keys, i + 1, i, size)
                values.copyInto(values, i + 1, i, size)
            }
            keys[i] = key
            values[i] = value
            size++
        }
    }

    actual open fun size(): Int = size

    actual open fun keyAt(index: Int): Int = keys[index]

    actual open fun valueAt(index: Int): Int = values[index]

    actual open fun setValueAt(index: Int, value: Int) {
        values[index] = value
    }

    actual open fun indexOfKey(key: Int): Int = ContainerHelpers.binarySearch(keys, size, key)

    actual open fun indexOfValue(value: Int): Int {
        for (i in 0 until size) {
            if (values[i] == value) return i
        }
        return -1
    }

    actual open fun clear() {
        size = 0
    }

    actual open fun append(key: Int, value: Int) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value)
            return
        }
        val pos = size
        if (pos >= keys.size) {
            val newCap = maxOf(4, if (pos < 6) pos * 2 else pos + (pos shr 1))
            keys = keys.copyOf(newCap)
            values = values.copyOf(newCap)
        }
        keys[pos] = key
        values[pos] = value
        size = pos + 1
    }

    actual open fun copyKeys(): IntArray? {
        if (size() == 0) return null
        return keys.copyOf(size)
    }

    actual override fun toString(): String {
        if (size() <= 0) return "{}"
        val buffer = StringBuilder(size * 28)
        buffer.append('{')
        for (i in 0 until size) {
            if (i > 0) buffer.append(", ")
            buffer.append(keyAt(i))
            buffer.append('=')
            buffer.append(valueAt(i))
        }
        buffer.append('}')
        return buffer.toString()
    }
}

actual open class SparseLongArray actual constructor(initialCapacity: Int) : Cloneable {
    private var keys: IntArray = if (initialCapacity == 0) IntArray(0) else IntArray(initialCapacity)
    private var values: LongArray = if (initialCapacity == 0) LongArray(0) else LongArray(initialCapacity)
    private var size: Int = 0

    actual constructor() : this(10)

    actual open fun clone(): SparseLongArray {
        val clone = SparseLongArray(0)
        clone.keys = keys.copyOf()
        clone.values = values.copyOf()
        clone.size = size
        return clone
    }

    actual open operator fun get(key: Int): Long = get(key, 0L)

    actual open fun get(key: Int, valueIfKeyNotFound: Long): Long {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        return if (i < 0) valueIfKeyNotFound else values[i]
    }

    actual open fun delete(key: Int) {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) removeAt(i)
    }

    actual open fun removeAt(index: Int) {
        keys.copyInto(keys, index, index + 1, size)
        values.copyInto(values, index, index + 1, size)
        size--
    }

    actual open fun put(key: Int, value: Long) {
        var i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            values[i] = value
        } else {
            i = i.inv()
            if (size >= keys.size) {
                val newCap = maxOf(4, if (size < 6) size * 2 else size + (size shr 1))
                keys = keys.copyOf(newCap)
                values = values.copyOf(newCap)
            }
            if (size - i != 0) {
                keys.copyInto(keys, i + 1, i, size)
                values.copyInto(values, i + 1, i, size)
            }
            keys[i] = key
            values[i] = value
            size++
        }
    }

    actual open fun size(): Int = size

    actual open fun keyAt(index: Int): Int = keys[index]

    actual open fun valueAt(index: Int): Long = values[index]

    actual open fun indexOfKey(key: Int): Int = ContainerHelpers.binarySearch(keys, size, key)

    actual open fun indexOfValue(value: Long): Int {
        for (i in 0 until size) {
            if (values[i] == value) return i
        }
        return -1
    }

    actual open fun clear() {
        size = 0
    }

    actual open fun append(key: Int, value: Long) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value)
            return
        }
        val pos = size
        if (pos >= keys.size) {
            val newCap = maxOf(4, if (pos < 6) pos * 2 else pos + (pos shr 1))
            keys = keys.copyOf(newCap)
            values = values.copyOf(newCap)
        }
        keys[pos] = key
        values[pos] = value
        size = pos + 1
    }

    actual override fun toString(): String {
        if (size() <= 0) return "{}"
        val buffer = StringBuilder(size * 28)
        buffer.append('{')
        for (i in 0 until size) {
            if (i > 0) buffer.append(", ")
            buffer.append(keyAt(i))
            buffer.append('=')
            buffer.append(valueAt(i))
        }
        buffer.append('}')
        return buffer.toString()
    }
}

@Suppress("UNCHECKED_CAST")
actual open class LongSparseArray<E> actual constructor(initialCapacity: Int) : Cloneable {
    private var garbage = false
    private var keys: LongArray = if (initialCapacity == 0) LongArray(0) else LongArray(initialCapacity)
    private var values: Array<Any?> = if (initialCapacity == 0) emptyArray() else arrayOfNulls(initialCapacity)
    private var size: Int = 0

    actual constructor() : this(10)

    actual open fun clone(): LongSparseArray<E> {
        val clone = LongSparseArray<E>(0)
        clone.garbage = garbage
        clone.keys = keys.copyOf()
        clone.values = values.copyOf()
        clone.size = size
        return clone
    }

    actual open operator fun get(key: Long): E? = get(key, null as E)

    actual open fun get(key: Long, valueIfKeyNotFound: E): E {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        return if (i < 0 || values[i] === ContainerHelpers.DELETED) {
            valueIfKeyNotFound
        } else {
            values[i] as E
        }
    }

    actual open fun delete(key: Long) {
        val i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0 && values[i] !== ContainerHelpers.DELETED) {
            values[i] = ContainerHelpers.DELETED
            garbage = true
        }
    }

    actual open fun remove(key: Long) {
        delete(key)
    }

    actual open fun removeAt(index: Int) {
        if (values[index] !== ContainerHelpers.DELETED) {
            values[index] = ContainerHelpers.DELETED
            garbage = true
        }
    }

    private fun gc() {
        val n = size
        var o = 0
        val k = keys
        val v = values
        for (i in 0 until n) {
            val value = v[i]
            if (value !== ContainerHelpers.DELETED) {
                if (i != o) {
                    k[o] = k[i]
                    v[o] = value
                    v[i] = null
                }
                o++
            }
        }
        garbage = false
        size = o
    }

    actual open fun put(key: Long, value: E) {
        var i = ContainerHelpers.binarySearch(keys, size, key)
        if (i >= 0) {
            values[i] = value
        } else {
            i = i.inv()
            if (i < size && values[i] === ContainerHelpers.DELETED) {
                keys[i] = key
                values[i] = value
                return
            }
            if (garbage && size >= keys.size) {
                gc()
                i = ContainerHelpers.binarySearch(keys, size, key).inv()
            }
            if (size >= keys.size) {
                val newCap = maxOf(4, if (size < 6) size * 2 else size + (size shr 1))
                keys = keys.copyOf(newCap)
                values = values.copyOf(newCap)
            }
            if (size - i != 0) {
                keys.copyInto(keys, i + 1, i, size)
                values.copyInto(values, i + 1, i, size)
            }
            keys[i] = key
            values[i] = value
            size++
        }
    }

    actual open fun size(): Int {
        if (garbage) gc()
        return size
    }

    actual open fun keyAt(index: Int): Long {
        if (garbage) gc()
        return keys[index]
    }

    actual open fun valueAt(index: Int): E {
        if (garbage) gc()
        return values[index] as E
    }

    actual open fun setValueAt(index: Int, value: E) {
        if (garbage) gc()
        values[index] = value
    }

    actual open fun indexOfKey(key: Long): Int {
        if (garbage) gc()
        return ContainerHelpers.binarySearch(keys, size, key)
    }

    actual open fun indexOfValue(value: E): Int {
        if (garbage) gc()
        for (i in 0 until size) {
            if (values[i] === value) return i
        }
        return -1
    }

    actual open fun clear() {
        val n = size
        for (i in 0 until n) {
            values[i] = null
        }
        size = 0
        garbage = false
    }

    actual open fun append(key: Long, value: E) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value)
            return
        }
        if (garbage && size >= keys.size) {
            gc()
        }
        val pos = size
        if (pos >= keys.size) {
            val newCap = maxOf(4, if (pos < 6) pos * 2 else pos + (pos shr 1))
            keys = keys.copyOf(newCap)
            values = values.copyOf(newCap)
        }
        keys[pos] = key
        values[pos] = value
        size = pos + 1
    }

    actual override fun toString(): String {
        if (size() <= 0) return "{}"
        val buffer = StringBuilder(size * 28)
        buffer.append('{')
        for (i in 0 until size) {
            if (i > 0) buffer.append(", ")
            val key = keyAt(i)
            buffer.append(key)
            buffer.append('=')
            val value = valueAt(i)
            if (value !== this) {
                buffer.append(value)
            } else {
                buffer.append("(this Map)")
            }
        }
        buffer.append('}')
        return buffer.toString()
    }
}

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
