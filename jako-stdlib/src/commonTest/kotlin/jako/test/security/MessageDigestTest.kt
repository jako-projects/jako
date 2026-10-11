package jako.test.security

import java.security.*

import kotlin.test.*

class MessageDigestTest {

    private fun ByteArray.toHexString(): String {
        return joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }

    @Test
    fun testSha256() {
        val md = MessageDigest.getInstance("SHA-256")
        assertEquals("SHA-256", md.getAlgorithm())
        assertEquals(32, md.getDigestLength())

        val digest = md.digest("abc".encodeToByteArray())
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", digest.toHexString())
    }

    @Test
    fun testSha1() {
        val md = MessageDigest.getInstance("SHA-1")
        assertEquals("SHA-1", md.getAlgorithm())
        assertEquals(20, md.getDigestLength())

        val digest = md.digest("abc".encodeToByteArray())
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", digest.toHexString())
    }

    @Test
    fun testMd5() {
        val md = MessageDigest.getInstance("MD5")
        assertEquals("MD5", md.getAlgorithm())
        assertEquals(16, md.getDigestLength())

        val digest = md.digest("abc".encodeToByteArray())
        assertEquals("900150983cd24fb0d6963f7d28e17f72", digest.toHexString())
    }

    @Test
    fun testIsEqual() {
        val a = byteArrayOf(1, 2, 3)
        val b = byteArrayOf(1, 2, 3)
        val c = byteArrayOf(1, 2, 4)

        assertTrue(MessageDigest.isEqual(a, b))
        assertFalse(MessageDigest.isEqual(a, c))
    }
}
