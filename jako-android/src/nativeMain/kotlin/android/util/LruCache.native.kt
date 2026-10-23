package android.util

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
