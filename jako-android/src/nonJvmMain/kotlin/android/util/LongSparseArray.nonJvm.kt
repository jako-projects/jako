package android.util

import java.lang.Cloneable

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
