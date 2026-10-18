package jako.test.util.concurrent.atomic

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*

class AtomicsTest {

    @Test
    fun testAtomicInteger() {
        val ai = AtomicInteger(10)
        assertEquals(10, ai.get())

        ai.set(20)
        assertEquals(20, ai.get())

        assertEquals(20, ai.getAndSet(30))
        assertEquals(30, ai.get())

        assertTrue(ai.compareAndSet(30, 40))
        assertEquals(40, ai.get())

        assertFalse(ai.compareAndSet(30, 50))
        assertEquals(40, ai.get())

        assertEquals(40, ai.getAndIncrement())
        assertEquals(41, ai.get())

        assertEquals(41, ai.getAndDecrement())
        assertEquals(40, ai.get())

        assertEquals(40, ai.getAndAdd(5))
        assertEquals(45, ai.get())

        assertEquals(46, ai.incrementAndGet())
        assertEquals(45, ai.decrementAndGet())
        assertEquals(50, ai.addAndGet(5))
    }

    @Test
    fun testAtomicLong() {
        val al = AtomicLong(100L)
        assertEquals(100L, al.get())

        al.set(200L)
        assertEquals(200L, al.get())

        assertEquals(200L, al.getAndSet(300L))
        assertEquals(300L, al.get())

        assertTrue(al.compareAndSet(300L, 400L))
        assertEquals(400L, al.get())

        assertEquals(400L, al.getAndIncrement())
        assertEquals(401L, al.get())

        assertEquals(401L, al.getAndDecrement())
        assertEquals(400L, al.get())

        assertEquals(400L, al.getAndAdd(10L))
        assertEquals(410L, al.get())

        assertEquals(411L, al.incrementAndGet())
        assertEquals(410L, al.decrementAndGet())
        assertEquals(420L, al.addAndGet(10L))
    }

    @Test
    fun testAtomicBoolean() {
        val ab = AtomicBoolean(false)
        assertFalse(ab.get())

        ab.set(true)
        assertTrue(ab.get())

        assertTrue(ab.getAndSet(false))
        assertFalse(ab.get())

        assertTrue(ab.compareAndSet(false, true))
        assertTrue(ab.get())
    }

    @Test
    fun testAtomicReference() {
        val ar = AtomicReference("hello")
        assertEquals("hello", ar.get())

        ar.set("world")
        assertEquals("world", ar.get())

        assertEquals("world", ar.getAndSet("kotlin"))
        assertEquals("kotlin", ar.get())

        assertTrue(ar.compareAndSet("kotlin", "native"))
        assertEquals("native", ar.get())

        assertFalse(ar.compareAndSet("kotlin", "java"))
        assertEquals("native", ar.get())

        assertEquals("native", ar.getAndUpdate { s -> "$s-updated" })
        assertEquals("native-updated", ar.get())
        assertEquals("NATIVE-UPDATED", ar.updateAndGet { s -> s?.uppercase() })
        assertEquals("NATIVE-UPDATED!", ar.accumulateAndGet("!") { a, b -> "$a$b" })
    }

    @Test
    fun testAtomicFunctionalMethodsAndArrays() {
        val ai = AtomicInteger(5)
        assertEquals(5, ai.getAndUpdate { it * 2 })
        assertEquals(10, ai.get())
        assertEquals(13, ai.updateAndGet { it + 3 })
        assertEquals(13, ai.getAndAccumulate(7) { a, b -> a + b })
        assertEquals(20, ai.get())
        assertEquals(40, ai.accumulateAndGet(2) { a, b -> a * b })

        val al = AtomicLong(10L)
        assertEquals(10L, al.getAndUpdate { it + 5L })
        assertEquals(30L, al.updateAndGet { it * 2L })
        assertEquals(30L, al.getAndAccumulate(12L) { a, b -> a + b })
        assertEquals(50L, al.accumulateAndGet(8L) { a, b -> a + b })

        val intArr = java.util.concurrent.atomic.AtomicIntegerArray(intArrayOf(1, 2, 3))
        assertEquals(3, intArr.length())
        assertEquals("[1, 2, 3]", intArr.toString())
        assertEquals(2, intArr.getAndAdd(1, 5))
        assertEquals(7, intArr.get(1))
        assertEquals(14, intArr.updateAndGet(1) { it * 2 })
        assertTrue(intArr.compareAndSet(0, 1, 10))
        assertEquals(10, intArr.get(0))
        assertFailsWith<IndexOutOfBoundsException> { intArr.get(-1) }
        assertFailsWith<IndexOutOfBoundsException> { intArr.get(3) }

        val longArr = java.util.concurrent.atomic.AtomicLongArray(2)
        longArr.set(0, 100L)
        assertEquals(101L, longArr.incrementAndGet(0))
        assertEquals(202L, longArr.accumulateAndGet(0, 2L) { a, b -> a * b })
        assertEquals("[202, 0]", longArr.toString())

        val refArr = java.util.concurrent.atomic.AtomicReferenceArray(arrayOf<String?>("a", "b"))
        assertEquals(2, refArr.length())
        assertEquals("a", refArr.getAndSet(0, "x"))
        assertEquals("x-y", refArr.accumulateAndGet(0, "y") { a, b -> "$a-$b" })
        assertEquals("[x-y, b]", refArr.toString())
    }

    @Test
    fun testAtomicStampedAndMarkableReference() {
        val v1 = "first"
        val v2 = "second"
        val stamped = java.util.concurrent.atomic.AtomicStampedReference(v1, 1)
        assertEquals(v1, stamped.reference)
        assertEquals(1, stamped.stamp)

        val holder = IntArray(1)
        assertEquals(v1, stamped.get(holder))
        assertEquals(1, holder[0])

        assertFalse(stamped.compareAndSet(v1, v2, 99, 2))
        assertTrue(stamped.compareAndSet(v1, v2, 1, 2))
        assertEquals(v2, stamped.get(holder))
        assertEquals(2, holder[0])
        assertTrue(stamped.attemptStamp(v2, 3))
        assertEquals(3, stamped.stamp)

        val markable = java.util.concurrent.atomic.AtomicMarkableReference(v1, false)
        assertEquals(v1, markable.reference)
        assertFalse(markable.isMarked)

        val markHolder = BooleanArray(1)
        assertEquals(v1, markable.get(markHolder))
        assertFalse(markHolder[0])

        assertTrue(markable.compareAndSet(v1, v2, false, true))
        assertEquals(v2, markable.get(markHolder))
        assertTrue(markHolder[0])
        assertTrue(markable.attemptMark(v2, false))
        assertFalse(markable.isMarked)
    }
}

