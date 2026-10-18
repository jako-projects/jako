package jako.test.util

import java.util.TreeMap
import kotlin.test.*

class TreeMapTest {
    @Test
    fun testNaturalOrder() {
        val map = TreeMap<Int, String>()
        map[3] = "three"
        map[1] = "one"
        map[4] = "four"
        map[2] = "two"

        assertEquals(4, map.size)
        assertEquals(listOf(1, 2, 3, 4), map.keys.toList())
        assertEquals(listOf("one", "two", "three", "four"), map.values.toList())
        assertEquals(1, map.firstKey())
        assertEquals(4, map.lastKey())

        assertEquals("two", map[2])
        assertEquals("two", map.remove(2))
        assertEquals(3, map.size)
        assertEquals(listOf(1, 3, 4), map.keys.toList())
    }

    @Test
    fun testCaseInsensitiveComparator() {
        val map = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
        map["cache-control"] = "no-cache"
        map["User-Agent"] = "OkHttp"

        assertEquals(2, map.size)
        assertTrue(map.containsKey("Cache-Control"))
        assertTrue(map.containsKey("CACHE-CONTROL"))
        assertEquals("no-cache", map["Cache-Control"])
        assertEquals("no-cache", map["CACHE-CONTROL"])

        map["Cache-Control"] = "no-store"
        assertEquals(2, map.size)
        assertEquals("no-store", map["cache-control"])

        assertEquals("no-store", map.remove("CACHE-CONTROL"))
        assertEquals(1, map.size)
        assertFalse(map.containsKey("cache-control"))
    }

    @Test
    fun testIteratorRemove() {
        val map = TreeMap<String, Int>()
        map["a"] = 1
        map["b"] = 2
        map["c"] = 3

        val it = map.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.key == "b") {
                it.remove()
            }
        }

        assertEquals(2, map.size)
        assertEquals(listOf("a", "c"), map.keys.toList())
    }

    @Test
    fun testBalancingWithManyElements() {
        val map = TreeMap<Int, Int>()
        for (i in 1..100) {
            map[i] = i * 10
        }
        assertEquals(100, map.size)
        assertEquals(1, map.firstKey())
        assertEquals(100, map.lastKey())

        for (i in 1..100 step 2) {
            assertEquals(i * 10, map.remove(i))
        }
        assertEquals(50, map.size)
        assertEquals(2, map.firstKey())
        assertEquals(100, map.lastKey())
    }

    @Test
    fun testNavigableMapOperations() {
        val map = TreeMap<Int, String>()
        map[10] = "ten"
        map[20] = "twenty"
        map[30] = "thirty"
        map[40] = "forty"

        assertEquals(10, map.lowerKey(20))
        assertNull(map.lowerKey(10))
        assertEquals("ten", map.lowerEntry(20)?.value)

        assertEquals(20, map.floorKey(20))
        assertEquals(20, map.floorKey(25))
        assertNull(map.floorKey(5))
        assertEquals("twenty", map.floorEntry(25)?.value)

        assertEquals(20, map.ceilingKey(20))
        assertEquals(30, map.ceilingKey(25))
        assertNull(map.ceilingKey(45))
        assertEquals("thirty", map.ceilingEntry(25)?.value)

        assertEquals(30, map.higherKey(20))
        assertNull(map.higherKey(40))
        assertEquals("thirty", map.higherEntry(20)?.value)

        assertEquals(10, map.firstEntry()?.key)
        assertEquals(40, map.lastEntry()?.key)

        val copy = TreeMap(map as java.util.SortedMap<Int, String>)
        assertEquals(4, copy.size)
        assertEquals(10, copy.pollFirstEntry()?.key)
        assertEquals(40, copy.pollLastEntry()?.key)
        assertEquals(listOf(20, 30), copy.keys.toList())
    }

    @Test
    fun testEventObjectAndObservable() {
        assertFailsWith<IllegalArgumentException> {
            java.util.EventObject(null)
        }
        val ev = java.util.EventObject("btn1")
        assertEquals("btn1", ev.source)
        assertTrue(ev.toString().contains("[source=btn1]"))

        val received = ArrayList<Any?>()
        val subject = object : java.util.Observable() {
            fun publish(msg: Any?) {
                setChanged()
                notifyObservers(msg)
            }
        }
        val obs = java.util.Observer { _, arg -> received.add(arg) }
        subject.addObserver(obs)
        assertEquals(1, subject.countObservers())
        subject.notifyObservers("ignored-not-changed")
        assertTrue(received.isEmpty())

        subject.publish("event-1")
        assertFalse(subject.hasChanged())
        assertEquals(listOf<Any?>("event-1"), received)

        subject.deleteObserver(obs)
        assertEquals(0, subject.countObservers())
    }
}

