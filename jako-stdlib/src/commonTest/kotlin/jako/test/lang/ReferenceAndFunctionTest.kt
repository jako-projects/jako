package jako.test.lang

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.*
import kotlin.test.*

class ReferenceAndFunctionTest {

    @Test
    fun testWeakSoftPhantomAndReferenceQueue() {
        val queue = ReferenceQueue<String>()
        assertNull(queue.poll())

        val weak = WeakReference("weak-val", queue)
        assertEquals("weak-val", weak.get())
        assertFalse(weak.isEnqueued())
        assertTrue(weak.enqueue())
        assertTrue(weak.isEnqueued())
        assertFalse(weak.enqueue()) // Cannot enqueue twice

        val polled = queue.poll()
        assertSame(weak, polled)
        assertFalse(weak.isEnqueued())
        assertFalse(weak.enqueue()) // Still false after poll
        weak.clear()
        assertNull(weak.get())

        val soft = SoftReference("soft-val", queue)
        assertEquals("soft-val", soft.get())
        assertTrue(soft.enqueue())
        assertSame(soft, queue.remove(50L))

        val phantom = PhantomReference("phantom-val", queue)
        assertNull(phantom.get()) // PhantomReference.get() always returns null
        assertTrue(phantom.enqueue())
        assertSame(phantom, queue.remove())

        assertNull(queue.remove(10L))
        assertFailsWith<IllegalArgumentException> {
            queue.remove(-1L)
        }
    }

    @Test
    fun testPrimitiveFunctionalInterfaces() {
        val bSup = BooleanSupplier { true }
        assertTrue(bSup.getAsBoolean())

        val iSup = IntSupplier { 42 }
        assertEquals(42, iSup.getAsInt())

        val lSup = LongSupplier { 100L }
        assertEquals(100L, lSup.getAsLong())

        val dSup = DoubleSupplier { 3.5 }
        assertEquals(3.5, dSup.getAsDouble())

        var acc = 0L
        val iCons = IntConsumer { acc += it }
        val lCons = LongConsumer { acc += it }
        val dCons = DoubleConsumer { acc += it.toLong() }
        iCons.accept(10)
        lCons.accept(20L)
        dCons.accept(5.0)
        assertEquals(35L, acc)

        val objInt = ObjIntConsumer<StringBuilder> { sb, v -> sb.append(v) }
        val objLong = ObjLongConsumer<StringBuilder> { sb, v -> sb.append(v) }
        val objDouble = ObjDoubleConsumer<StringBuilder> { sb, v -> sb.append(v.toInt()) }
        val sb = StringBuilder()
        objInt.accept(sb, 1)
        objLong.accept(sb, 2L)
        objDouble.accept(sb, 3.0)
        assertEquals("123", sb.toString())

        assertTrue(IntPredicate { it > 0 }.test(5))
        assertTrue(LongPredicate { it < 0L }.test(-1L))
        assertTrue(DoublePredicate { it == 2.5 }.test(2.5))

        assertEquals("v7", IntFunction { "v$it" }.apply(7))
        assertEquals("v8", LongFunction { "v$it" }.apply(8L))
        assertEquals(6, DoubleFunction { it.toInt() }.apply(6.9))

        assertEquals(3, ToIntFunction<String> { it.length }.applyAsInt("abc"))
        assertEquals(4L, ToLongFunction<String> { it.length.toLong() }.applyAsLong("abcd"))
        assertEquals(2.0, ToDoubleFunction<String> { it.length.toDouble() }.applyAsDouble("ab"))

        assertEquals(5, ToIntBiFunction<String, String> { a, b -> a.length + b.length }.applyAsInt("ab", "cde"))
        assertEquals(5L, ToLongBiFunction<String, String> { a, b -> (a.length + b.length).toLong() }.applyAsLong("ab", "cde"))
        assertEquals(5.0, ToDoubleBiFunction<String, String> { a, b -> (a.length + b.length).toDouble() }.applyAsDouble("ab", "cde"))

        assertEquals(20, IntUnaryOperator { it * 2 }.applyAsInt(10))
        assertEquals(30L, LongUnaryOperator { it * 3L }.applyAsLong(10L))
        assertEquals(5.0, DoubleUnaryOperator { it * 2.0 }.applyAsDouble(2.5))

        assertEquals(15, IntBinaryOperator { a, b -> a + b }.applyAsInt(7, 8))
        assertEquals(15L, LongBinaryOperator { a, b -> a + b }.applyAsLong(7L, 8L))
        assertEquals(15.0, DoubleBinaryOperator { a, b -> a + b }.applyAsDouble(7.5, 7.5))

        assertEquals(10L, IntToLongFunction { it.toLong() }.applyAsLong(10))
        assertEquals(10.5, IntToDoubleFunction { it + 0.5 }.applyAsDouble(10))
        assertEquals(10, LongToIntFunction { it.toInt() }.applyAsInt(10L))
        assertEquals(10.5, LongToDoubleFunction { it + 0.5 }.applyAsDouble(10L))
        assertEquals(10, DoubleToIntFunction { it.toInt() }.applyAsInt(10.9))
        assertEquals(10L, DoubleToLongFunction { it.toLong() }.applyAsLong(10.9))
    }

    @Test
    fun testConcurrentLinkedDequeAndSkipListMapNavigable() {
        val deque = ConcurrentLinkedDeque<String>()
        assertTrue(deque.isEmpty())
        deque.addFirst("b")
        deque.addFirst("a")
        deque.addLast("c")
        assertEquals(3, deque.size)
        assertEquals("a", deque.peekFirst())
        assertEquals("c", deque.peekLast())
        assertEquals(listOf("c", "b", "a"), deque.descendingIterator().asSequence().toList())

        assertEquals("a", deque.pollFirst())
        assertEquals("c", deque.pollLast())
        assertEquals("b", deque.pop())
        assertNull(deque.poll())

        val skipMap = ConcurrentSkipListMap<Int, String>()
        skipMap[10] = "ten"
        skipMap[20] = "twenty"
        skipMap[30] = "thirty"
        assertEquals("ten", skipMap.lowerEntry(20)?.value)
        assertEquals("twenty", skipMap.floorEntry(25)?.value)
        assertEquals("thirty", skipMap.ceilingEntry(25)?.value)
        assertEquals("thirty", skipMap.higherEntry(20)?.value)
    }
}
