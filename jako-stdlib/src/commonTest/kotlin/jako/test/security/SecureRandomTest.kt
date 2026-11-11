package jako.test.security

import java.security.*

import kotlin.test.*

class SecureRandomTest {

    @Test
    fun testNextBytes() {
        val sr = SecureRandom()
        val b1 = ByteArray(16)
        val b2 = ByteArray(16)

        sr.nextBytes(b1)
        sr.nextBytes(b2)

        assertFalse(b1.contentEquals(b2), "Random bytes should not be identical")
    }

    @Test
    fun testNextIntBounded() {
        val sr = SecureRandom()
        for (i in 0 until 50) {
            val v = sr.nextInt(10)
            assertTrue(v in 0 until 10, "Value $v out of bound [0, 10)")
        }
    }

    @Test
    fun testPrimitives() {
        val sr = SecureRandom()
        val longVal = sr.nextLong()
        val boolVal = sr.nextBoolean()
        // Ensure execution without throwing
        assertTrue(boolVal || !boolVal)
    }

    @Test
    fun testLargeBufferExceeding64KiBQuota() {
        val sr = SecureRandom()
        val large = ByteArray(100_000)
        sr.nextBytes(large)
        val firstSlice = large.copyOfRange(0, 64)
        val lastSlice = large.copyOfRange(large.size - 64, large.size)
        assertTrue(firstSlice.any { it.toInt() != 0 }, "First 64 bytes should not be all zeros")
        assertTrue(lastSlice.any { it.toInt() != 0 }, "Last 64 bytes past 65536 boundary should not be all zeros")
        assertFalse(firstSlice.contentEquals(lastSlice), "Slices across 64KiB boundary must not repeat")
    }
}
