package jako.test.util

import java.util.Arrays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArraysTest {

    @Test
    fun testSortAndBinarySearch() {
        val ints = intArrayOf(5, 2, 8, 1, 9)
        Arrays.sort(ints)
        assertEquals(1, ints[0])
        assertEquals(9, ints[4])

        val idx = Arrays.binarySearch(ints, 5)
        assertEquals(2, idx)
    }

    @Test
    fun testFillAndEquals() {
        val a1 = intArrayOf(0, 0, 0)
        Arrays.fill(a1, 7)
        val a2 = intArrayOf(7, 7, 7)
        assertTrue(Arrays.equals(a1, a2))
    }

    @Test
    fun testCopyOfAndRange() {
        val orig = byteArrayOf(1, 2, 3, 4, 5)
        val copy = Arrays.copyOf(orig, 3)
        assertEquals(3, copy.size)
        assertEquals(1, copy[0])
        assertEquals(3, copy[2])

        val range = Arrays.copyOfRange(orig, 1, 4)
        assertEquals(3, range.size)
        assertEquals(2, range[0])
        assertEquals(4, range[2])
    }

    @Test
    fun testAsList() {
        val list = Arrays.asList("hello", "world")
        assertEquals(2, list.size)
        assertEquals("hello", list[0])
        assertEquals("world", list[1])
    }
}
