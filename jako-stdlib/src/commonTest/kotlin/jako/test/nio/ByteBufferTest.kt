package jako.test.nio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.*

class ByteBufferTest {

    @Test
    fun testByteBufferAllocateAndPutGet() {
        val buf = ByteBuffer.allocate(10)
        assertEquals(10, buf.capacity())
        assertEquals(10, buf.remaining())
        assertEquals(0, buf.position())

        buf.put(0x12.toByte())
        buf.put(0x34.toByte())
        assertEquals(2, buf.position())
        assertEquals(8, buf.remaining())

        buf.flip()
        assertEquals(0, buf.position())
        assertEquals(2, buf.limit())
        assertEquals(0x12.toByte(), buf.get())
        assertEquals(0x34.toByte(), buf.get())

        buf.clear()
        assertEquals(0, buf.position())
        assertEquals(10, buf.capacity())
    }

    @Test
    fun testByteBufferWrapAndSlice() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val buf = ByteBuffer.wrap(data)
        assertEquals(5, buf.capacity())
        assertTrue(buf.hasArray())

        buf.position(1)
        val sliced = buf.slice()
        assertEquals(4, sliced.capacity())
        assertEquals(2.toByte(), sliced.get())
    }

    @Test
    fun testByteOrder() {
        val bo = ByteOrder.nativeOrder()
        assertNotNull(bo)
        assertEquals("LITTLE_ENDIAN", bo.toString())
        assertEquals("BIG_ENDIAN", ByteOrder.BIG_ENDIAN.toString())

        val buf = ByteBuffer.allocate(16)
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order())
        buf.putShort(0x1234.toShort())
        buf.putInt(0x12345678)
        buf.putLong(0x0102030405060708L)
        assertEquals(0x12.toByte(), buf.get(0))
        assertEquals(0x34.toByte(), buf.get(1))
        assertEquals(0x12.toByte(), buf.get(2))
        assertEquals(0x78.toByte(), buf.get(5))

        buf.clear()
        buf.order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(ByteOrder.LITTLE_ENDIAN, buf.order())
        buf.putShort(0x1234.toShort())
        buf.putInt(0x12345678)
        buf.putLong(0x0102030405060708L)
        assertEquals(0x34.toByte(), buf.get(0))
        assertEquals(0x12.toByte(), buf.get(1))
        assertEquals(0x78.toByte(), buf.get(2))
        assertEquals(0x12.toByte(), buf.get(5))

        buf.flip()
        assertEquals(0x1234.toShort(), buf.getShort())
        assertEquals(0x12345678, buf.getInt())
        assertEquals(0x0102030405060708L, buf.getLong())
    }

    @Test
    fun testReadOnlyDuplicateSliceAndTypedViews() {
        val buf = ByteBuffer.allocate(16)
        buf.putInt(100)
        buf.putInt(200)
        buf.flip()

        val dup = buf.duplicate()
        assertEquals(0, dup.position())
        assertEquals(8, dup.limit())
        assertEquals(100, dup.getInt())
        assertEquals(0, buf.position())

        val ro = buf.asReadOnlyBuffer()
        assertTrue(ro.isReadOnly())
        assertTrue(ro.isReadOnly)
        assertFalse(ro.hasArray())
        assertEquals(100, ro.getInt())
        assertFailsWith<java.nio.ReadOnlyBufferException> {
            ro.put(0, 1.toByte())
        }
        assertFailsWith<java.nio.ReadOnlyBufferException> {
            ro.putInt(300)
        }
        assertFailsWith<java.nio.ReadOnlyBufferException> {
            ro.array()
        }
        assertFailsWith<java.nio.ReadOnlyBufferException> {
            ro.arrayOffset()
        }

        val direct = ByteBuffer.allocateDirect(4)
        assertTrue(direct.isDirect())
        assertFalse(direct.hasArray())
        assertFailsWith<UnsupportedOperationException> {
            direct.array()
        }
        assertFailsWith<UnsupportedOperationException> {
            direct.arrayOffset()
        }
        assertFailsWith<IllegalArgumentException> {
            ByteBuffer.allocate(-1)
        }
        assertFailsWith<IllegalArgumentException> {
            ByteBuffer.allocateDirect(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            ByteBuffer.wrap(byteArrayOf(1, 2), 1, 5)
        }

        val emptyBuf = ByteBuffer.allocate(2).also { it.limit(0) }
        assertFailsWith<java.nio.BufferUnderflowException> {
            emptyBuf.get()
        }
        assertFailsWith<java.nio.BufferUnderflowException> {
            emptyBuf.getShort()
        }
        assertFailsWith<java.nio.BufferUnderflowException> {
            emptyBuf.getChar()
        }

        buf.position(4)
        val sliced = buf.slice()
        assertEquals(4, sliced.capacity())
        assertEquals(200, sliced.getInt(0))
        sliced.putInt(0, 999)
        assertEquals(999, buf.getInt(4))

        buf.clear()
        val intView = buf.asIntBuffer()
        assertEquals(4, intView.capacity())
        intView.put(0, 0x0A0B0C0D)
        assertEquals(0x0A.toByte(), buf.get(0))
        assertEquals(0x0D.toByte(), buf.get(3))

        val b1 = ByteBuffer.wrap(byteArrayOf(1, 2, 3))
        val b2 = ByteBuffer.wrap(byteArrayOf(1, 2, 4))
        assertTrue(b1.compareTo(b2) < 0)
        assertEquals(0, b1.compareTo(ByteBuffer.wrap(byteArrayOf(1, 2, 3))))
    }
}
