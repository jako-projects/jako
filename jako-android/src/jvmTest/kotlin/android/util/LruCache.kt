package android.util

import java.util.LinkedHashMap

/**
 * Desktop JVM shim for AOSP `android.util.LruCache`.
 * Standard AOSP `LruCache.trimToSize` in `android-all` invokes Android libcore's
 * non-standard `LinkedHashMap.eldest()`, which throws `NoSuchMethodError` on OpenJDK.
 * This class is identical to AOSP `LruCache` except using `map.entries.firstOrNull()`.
 */
open class LruCache<K, V>(maxSize: Int) {
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

    open fun resize(maxSize: Int) {
        require(maxSize > 0) { "maxSize <= 0" }
        synchronized(this) {
            this.maxSize = maxSize
        }
        trimToSize(maxSize)
    }

    operator fun get(key: K): V? {
        if (key == null) throw NullPointerException("key == null")
        var mapValue: V?
        synchronized(this) {
            mapValue = map[key]
            if (mapValue != null) {
                hitCount++
                return mapValue
            }
            missCount++
        }
        val createdValue = create(key) ?: return null
        synchronized(this) {
            createCount++
            mapValue = map.put(key, createdValue)
            if (mapValue != null) {
                map.put(key, mapValue!!)
            } else {
                size += safeSizeOf(key, createdValue)
            }
        }
        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue)
            return mapValue
        } else {
            trimToSize(maxSize)
            return createdValue
        }
    }

    fun put(key: K, value: V): V? {
        if (key == null || value == null) throw NullPointerException("key == null || value == null")
        val previous: V?
        synchronized(this) {
            putCount++
            size += safeSizeOf(key, value)
            previous = map.put(key, value)
            if (previous != null) {
                size -= safeSizeOf(key, previous)
            }
        }
        if (previous != null) {
            entryRemoved(false, key, previous, value)
        }
        trimToSize(maxSize)
        return previous
    }

    open fun trimToSize(maxSize: Int) {
        while (true) {
            val key: K
            val value: V
            synchronized(this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw IllegalStateException("${javaClass.name}.sizeOf() is reporting inconsistent results!")
                }
                if (size <= maxSize || map.isEmpty()) break
                val toEvict = map.entries.firstOrNull() ?: break
                key = toEvict.key
                value = toEvict.value
                map.remove(key)
                size -= safeSizeOf(key, value)
                evictionCount++
            }
            entryRemoved(true, key, value, null)
        }
    }

    fun remove(key: K): V? {
        if (key == null) throw NullPointerException("key == null")
        val previous: V?
        synchronized(this) {
            previous = map.remove(key)
            if (previous != null) {
                size -= safeSizeOf(key, previous)
            }
        }
        if (previous != null) {
            entryRemoved(false, key, previous, null)
        }
        return previous
    }

    protected open fun entryRemoved(evicted: Boolean, key: K, oldValue: V, newValue: V?) {}
    protected open fun create(key: K): V? = null

    private fun safeSizeOf(key: K, value: V): Int {
        val result = sizeOf(key, value)
        if (result < 0) throw IllegalStateException("Negative size: $key=$value")
        return result
    }

    protected open fun sizeOf(key: K, value: V): Int = 1

    fun evictAll() = trimToSize(-1)
    @Synchronized fun size(): Int = size
    @Synchronized fun maxSize(): Int = maxSize
    @Synchronized fun hitCount(): Int = hitCount
    @Synchronized fun missCount(): Int = missCount
    @Synchronized fun createCount(): Int = createCount
    @Synchronized fun putCount(): Int = putCount
    @Synchronized fun evictionCount(): Int = evictionCount
    @Synchronized fun snapshot(): MutableMap<K, V> = LinkedHashMap(map)

    @Synchronized
    override fun toString(): String {
        val accesses = hitCount + missCount
        val hitPercent = if (accesses != 0) (100 * hitCount / accesses) else 0
        return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", maxSize, hitCount, missCount, hitPercent)
    }
}
