package android.util

import java.lang.Cloneable

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
