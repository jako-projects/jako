package jako.test.util

import java.util.BitSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitSetTest {

    @Test
    fun testBasicOperations() {
        val bs = BitSet()
        assertTrue(bs.isEmpty)
        assertEquals(0, bs.length())
        assertEquals(0, bs.cardinality())

        bs.set(10)
        bs.set(20)
        bs.set(30)

        assertFalse(bs.isEmpty)
        assertEquals(3, bs.cardinality())
        assertEquals(31, bs.length())
        assertTrue(bs.get(10))
        assertTrue(bs.get(20))
        assertTrue(bs.get(30))
        assertFalse(bs.get(0))
        assertFalse(bs.get(11))

        bs.clear(20)
        assertFalse(bs.get(20))
        assertEquals(2, bs.cardinality())

        bs.flip(10)
        assertFalse(bs.get(10))
        bs.flip(5)
        assertTrue(bs.get(5))
    }

    @Test
    fun testRangeOperations() {
        val bs = BitSet()
        bs.set(5, 10)
        for (i in 5 until 10) {
            assertTrue(bs.get(i))
        }
        assertFalse(bs.get(4))
        assertFalse(bs.get(10))
        assertEquals(5, bs.cardinality())

        val sub = bs.get(6, 9)
        assertEquals(3, sub.cardinality())
        assertTrue(sub.get(0))
        assertTrue(sub.get(1))
        assertTrue(sub.get(2))
        assertFalse(sub.get(3))
    }

    @Test
    fun testBitwiseOperations() {
        val a = BitSet()
        a.set(1)
        a.set(3)

        val b = BitSet()
        b.set(3)
        b.set(5)

        assertTrue(a.intersects(b))

        val orSet = BitSet().apply { or(a) }
        orSet.or(b)
        assertEquals(3, orSet.cardinality())
        assertTrue(orSet.get(1))
        assertTrue(orSet.get(3))
        assertTrue(orSet.get(5))

        val andSet = BitSet().apply { or(a) }
        andSet.and(b)
        assertEquals(1, andSet.cardinality())
        assertTrue(andSet.get(3))

        val xorSet = BitSet().apply { or(a) }
        xorSet.xor(b)
        assertEquals(2, xorSet.cardinality())
        assertTrue(xorSet.get(1))
        assertFalse(xorSet.get(3))
        assertTrue(xorSet.get(5))
    }

    @Test
    fun testNextPreviousBit() {
        val bs = BitSet()
        bs.set(10)
        bs.set(20)
        bs.set(30)

        assertEquals(10, bs.nextSetBit(0))
        assertEquals(10, bs.nextSetBit(10))
        assertEquals(20, bs.nextSetBit(11))
        assertEquals(30, bs.nextSetBit(25))
        assertEquals(-1, bs.nextSetBit(31))

        assertEquals(30, bs.previousSetBit(35))
        assertEquals(30, bs.previousSetBit(30))
        assertEquals(20, bs.previousSetBit(29))
        assertEquals(10, bs.previousSetBit(15))
        assertEquals(-1, bs.previousSetBit(5))
    }

    @Test
    fun testSerializationAndByteArray() {
        val bs = BitSet()
        bs.set(0)
        bs.set(8)
        bs.set(9)

        val bytes = bs.toByteArray()
        val restored = BitSet.valueOf(bytes)
        assertEquals(bs, restored)
        assertEquals(bs.hashCode(), restored.hashCode())
    }
}
