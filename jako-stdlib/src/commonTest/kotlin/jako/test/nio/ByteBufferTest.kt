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
    }
}
