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
    }
}
