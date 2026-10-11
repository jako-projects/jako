package jako.test.util

import java.util.*

import kotlin.test.*

class UUIDTest {

    @Test
    fun testConstructorAndGetters() {
        val msb = 0x1234567890ABCDEFL
        val lsb = 0x1122334455667788L
        val uuid = UUID(msb, lsb)

        assertEquals(msb, uuid.getMostSignificantBits())
        assertEquals(lsb, uuid.getLeastSignificantBits())
    }

    @Test
    fun testRandomUUIDAndParsing() {
        val uuid = UUID.randomUUID()
        assertEquals(4, uuid.version())
        assertEquals(2, uuid.variant())

        val str = uuid.toString()
        assertEquals(36, str.length)

        val parsed = UUID.fromString(str)
        assertEquals(uuid, parsed)
        assertEquals(uuid.hashCode(), parsed.hashCode())
        assertEquals(uuid.getMostSignificantBits(), parsed.getMostSignificantBits())
        assertEquals(uuid.getLeastSignificantBits(), parsed.getLeastSignificantBits())
        assertEquals(0, uuid.compareTo(parsed))
    }

    @Test
    fun testNameUUIDFromBytes() {
        val name = "jako-multiplatform-uuid".encodeToByteArray()
        val u1 = UUID.nameUUIDFromBytes(name)
        val u2 = UUID.nameUUIDFromBytes(name)

        assertEquals(u1, u2)
        assertEquals(u1.hashCode(), u2.hashCode())
        assertEquals(u1.toString(), u2.toString())
        assertEquals(3, u1.version())
        assertEquals(2, u1.variant())
    }

    @Test
    fun testComparisonAndEquality() {
        val u1 = UUID(100L, 200L)
        val u2 = UUID(100L, 200L)
        val u3 = UUID(100L, 300L)
        val u4 = UUID(200L, 100L)

        assertEquals(u1, u2)
        assertTrue(u1 < u3)
        assertTrue(u1 < u4)
        assertFalse(u1 == u3)
    }

    @Test
    fun testInvalidUUIDStringThrows() {
        assertFailsWith<IllegalArgumentException> {
            UUID.fromString("invalid-uuid")
        }
        assertFailsWith<IllegalArgumentException> {
            UUID.fromString("12345678-1234-1234-1234")
        }
    }
}
