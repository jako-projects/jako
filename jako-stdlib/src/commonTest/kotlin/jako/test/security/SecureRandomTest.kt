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
}
