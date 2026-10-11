package jako.test.util

import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionsTest {

    @Test
    fun testSortAndReverse() {
        val list = mutableListOf("banana", "apple", "cherry")
        Collections.sort(list)
        assertEquals("apple", list[0])
        assertEquals("cherry", list[2])

        Collections.reverse(list)
        assertEquals("cherry", list[0])
        assertEquals("apple", list[2])
    }

    @Test
    fun testMinMaxFrequency() {
        val list = listOf(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5)
        assertEquals(1, Collections.min(list))
        assertEquals(9, Collections.max(list))
        assertEquals(3, Collections.frequency(list, 5))
    }

    @Test
    fun testDisjointAndSwap() {
        val l1 = listOf(1, 2, 3)
        val l2 = listOf(4, 5, 6)
        val l3 = listOf(3, 4, 5)

        assertTrue(Collections.disjoint(l1, l2))
        assertFalse(Collections.disjoint(l1, l3))

        val mut = mutableListOf("A", "B", "C")
        Collections.swap(mut, 0, 2)
        assertEquals(listOf("C", "B", "A"), mut)
    }

    @Test
    fun testNCopiesAndSingleton() {
        val copies = Collections.nCopies(3, "test")
        assertEquals(3, copies.size)
        assertEquals("test", copies[0])

        val single = Collections.singletonList(42)
        assertEquals(1, single.size)
        assertEquals(42, single[0])
    }

    @Test
    fun testNewSetFromMap() {
        val set = Collections.newSetFromMap(linkedMapOf<String, Boolean>())
        assertTrue(set.add("a"))
        assertFalse(set.add("a"))
        assertTrue(set.contains("a"))
        assertEquals(1, set.size)
        assertTrue(set.remove("a"))
        assertTrue(set.isEmpty())
    }
}
