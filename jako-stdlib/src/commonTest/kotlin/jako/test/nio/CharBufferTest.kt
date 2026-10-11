package jako.test.nio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.ReadOnlyBufferException
import java.nio.ShortBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharBufferTest {

    @Test
    fun testWrapFlipGet() {
        val wrapped = CharBuffer.wrap("abcd")
        assertEquals(4, wrapped.capacity())
        assertEquals(4, wrapped.remaining())
        assertEquals('a', wrapped.get())
        assertEquals(3, wrapped.remaining())

        wrapped.flip()
        assertEquals(0, wrapped.position())
        assertEquals(1, wrapped.limit())
        assertEquals('a', wrapped.get())
        assertEquals(0, wrapped.remaining())
    }

    @Test
    fun testAllocatePutFlipGet() {
        val buf = CharBuffer.allocate(8)
        assertEquals(8, buf.capacity())
        assertEquals(8, buf.remaining())
        assertEquals(0, buf.position())

        buf.put('你')
        buf.put('好')
        assertEquals(2, buf.position())

        buf.flip()
        assertEquals(0, buf.position())
        assertEquals(2, buf.limit())
        assertEquals('你', buf.get())
        assertEquals('好', buf.get())

        buf.clear()
        assertEquals(0, buf.position())
        assertEquals(8, buf.limit())
    }

    @Test
    fun testDuplicateSliceReadOnly() {
        val buf = CharBuffer.wrap(charArrayOf('w', 'x', 'y', 'z'))
        buf.position(1)
        val sliced = buf.slice()
        assertEquals(3, sliced.capacity())
        assertEquals('x', sliced.get())

        val dup = buf.duplicate()
        assertEquals(1, dup.position())
        assertEquals('x', dup.get())

        val ro = buf.asReadOnlyBuffer()
        assertTrue(ro.isReadOnly)
        assertFailsWith<ReadOnlyBufferException> { ro.put('!') }
        assertFalse(buf.isReadOnly)
    }

    @Test
    fun testIntBufferAndLongBuffer() {
        val ints = IntBuffer.allocate(4)
        ints.put(11)
        ints.put(22)
        ints.flip()
        assertEquals(11, ints.get())
        assertEquals(22, ints.get())

        val wrappedInts = IntBuffer.wrap(intArrayOf(7, 8, 9))
        wrappedInts.position(1)
        val intSlice = wrappedInts.slice()
        assertEquals(2, intSlice.capacity())
        assertEquals(8, intSlice.get())

        val longs = LongBuffer.allocate(3)
        longs.put(100L)
        longs.put(200L)
        longs.flip()
        assertEquals(100L, longs.get())
        assertEquals(200L, longs.get())

        val wrappedLongs = LongBuffer.wrap(longArrayOf(1L, 2L, 3L))
        assertEquals(1L, wrappedLongs.get(0))
        wrappedLongs.put(1, 99L)
        assertEquals(99L, wrappedLongs.get(1))
    }

    @Test
    fun testByteBufferAsTypedViews() {
        val bb = ByteBuffer.allocate(8)
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.put(0x4F.toByte())
        bb.put(0x60.toByte())
        bb.put(0x59.toByte())
        bb.put(0x7D.toByte())
        bb.put(0x00.toByte())
        bb.put(0x00.toByte())
        bb.put(0x00.toByte())
        bb.put(0x01.toByte())
        bb.flip()

        val chars = bb.asCharBuffer()
        assertEquals(4, chars.remaining())
        assertEquals('\u4F60', chars.get())
        assertEquals('\u597D', chars.get())

        bb.position(4)
        val ints = bb.asIntBuffer()
        assertEquals(1, ints.remaining())
        assertEquals(1, ints.get())

        bb.clear()
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.put(0x01.toByte())
        bb.put(0x02.toByte())
        bb.put(0x03.toByte())
        bb.put(0x04.toByte())
        bb.put(0x05.toByte())
        bb.put(0x06.toByte())
        bb.put(0x07.toByte())
        bb.put(0x08.toByte())
        bb.flip()
        val longs = bb.asLongBuffer()
        assertEquals(0x0102030405060708L, longs.get())
    }

    @Test
    fun testShortFloatDoubleBuffersAndViews() {
        val shorts = ShortBuffer.allocate(3)
        shorts.put((-2).toShort())
        shorts.put(7)
        shorts.flip()
        assertEquals((-2).toShort(), shorts.get())
        assertEquals(7.toShort(), shorts.get())

        val wrappedShorts = ShortBuffer.wrap(shortArrayOf(1, 2, 3))
        wrappedShorts.position(1)
        assertEquals(2, wrappedShorts.slice().capacity())

        val floats = FloatBuffer.wrap(floatArrayOf(1.5f, 2.5f))
        assertEquals(1.5f, floats.get())
        floats.put(0, 9.5f)
        assertEquals(9.5f, floats.get(0))

        val doubles = DoubleBuffer.allocate(2)
        doubles.put(3.25)
        doubles.flip()
        assertEquals(3.25, doubles.get())

        val bb = ByteBuffer.allocate(8)
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putShort((-1).toShort())
        bb.putFloat(1.0f)
        bb.put((0).toByte())
        bb.put((0).toByte())
        bb.flip()
        assertEquals((-1).toShort(), bb.asShortBuffer().get())
        bb.position(2)
        assertEquals(1.0f, bb.asFloatBuffer().get())

        val db = ByteBuffer.allocate(8)
        db.order(ByteOrder.BIG_ENDIAN)
        db.putDouble(2.5)
        db.flip()
        assertEquals(2.5, db.asDoubleBuffer().get())
    }
}
