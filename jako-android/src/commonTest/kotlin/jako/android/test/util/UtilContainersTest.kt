package jako.android.test.util

import android.util.ArrayMap
import android.util.ArraySet
import android.util.LongSparseArray
import android.util.LruCache
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.util.SparseLongArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UtilContainersTest {

    @Test
    fun testLruCache() {
        val evictedKeys = mutableListOf<String>()
        val cache = object : LruCache<String, Int>(3) {
            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int, newValue: Int?) {
                if (evicted) evictedKeys.add(key)
            }

            override fun create(key: String): Int? {
                return if (key.startsWith("auto_")) key.removePrefix("auto_").toInt() else null
            }
        }

        assertNull(cache["missing"])
        assertEquals(1, cache.missCount())

        assertEquals(99, cache["auto_99"])
        assertEquals(1, cache.createCount())
        assertEquals(1, cache.size())

        cache.put("a", 1)
        cache.put("b", 2)
        // Cache now has [auto_99, a, b] (size 3)
        assertEquals(1, cache["a"]) // Access 'a' -> order becomes [auto_99, b, a]
        assertEquals(1, cache.hitCount())

        cache.put("c", 3) // Evicts 'auto_99'
        assertEquals(listOf("auto_99"), evictedKeys)
        assertFalse(cache.snapshot().containsKey("auto_99"))
        assertEquals(listOf("b", "a", "c"), cache.snapshot().keys.toList())

        assertEquals(2, cache.remove("b"))
        assertEquals(2, cache.size())

        cache.evictAll()
        assertEquals(0, cache.size())
    }

    @Test
    fun testSparseArrays() {
        val sa = SparseArray<String>()
        sa.put(10, "ten")
        sa.put(5, "five")
        sa.append(20, "twenty")
        assertEquals(3, sa.size())
        assertEquals("five", sa[5])
        assertEquals("default", sa.get(99, "default"))
        assertEquals(0, sa.indexOfKey(5))
        assertTrue(sa.indexOfKey(7) < 0)
        assertEquals("{5=five, 10=ten, 20=twenty}", sa.toString())

        val cloned = sa.clone()
        sa.delete(10)
        assertEquals(2, sa.size())
        assertEquals(3, cloned.size())
        assertEquals("ten", cloned[10])

        val sba = SparseBooleanArray()
        sba.put(2, true)
        sba.put(1, false)
        assertTrue(sba[2])
        assertFalse(sba[99])
        assertTrue(sba.get(99, true))
        assertEquals("{1=false, 2=true}", sba.toString())
        assertEquals(sba, sba.clone())

        val sia = SparseIntArray()
        sia.put(100, 1)
        sia.put(50, 2)
        assertEquals(2, sia[50])
        assertEquals(-1, sia.get(10, -1))
        assertContentEquals(intArrayOf(50, 100), sia.copyKeys())
        assertEquals("{50=2, 100=1}", sia.toString())

        val sla = SparseLongArray()
        sla.put(3, 300L)
        sla.put(1, 100L)
        assertEquals(100L, sla[1])
        assertEquals(0L, sla[2])
        assertEquals("{1=100, 3=300}", sla.toString())

        val lsa = LongSparseArray<String>()
        lsa.put(10000000000L, "big")
        lsa.put(1L, "one")
        assertEquals("big", lsa[10000000000L])
        assertEquals("{1=one, 10000000000=big}", lsa.toString())
    }

    @Test
    fun testArrayMapAndArraySet() {
        val map = ArrayMap<String?, Int>()
        map["b"] = 2
        map["a"] = 1
        map[null] = 0
        assertEquals(3, map.size)
        assertEquals(0, map[null])
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
        assertTrue(map.containsKey(null))
        assertTrue(map.containsValue(2))

        val keyIdx = map.indexOfKey("a")
        assertTrue(keyIdx >= 0)
        assertEquals("a", map.keyAt(keyIdx))
        assertEquals(1, map.valueAt(keyIdx))
        assertEquals(1, map.setValueAt(keyIdx, 10))
        assertEquals(10, map["a"])

        val copyMap = ArrayMap(map)
        assertEquals(map, copyMap)
        assertEquals(map.hashCode(), copyMap.hashCode())

        map.removeAt(keyIdx)
        assertFalse(map.containsKey("a"))
        assertEquals(2, map.size)

        val set = ArraySet<String?>()
        assertTrue(set.add("x"))
        assertTrue(set.add("y"))
        assertTrue(set.add(null))
        assertFalse(set.add("x"))
        assertEquals(3, set.size)
        assertTrue(set.contains(null))
        assertTrue(set.contains("x"))

        val idx = set.indexOf("y")
        assertTrue(idx >= 0)
        assertEquals("y", set.valueAt(idx))
        assertEquals("y", set.removeAt(idx))
        assertEquals(2, set.size)
    }
}
