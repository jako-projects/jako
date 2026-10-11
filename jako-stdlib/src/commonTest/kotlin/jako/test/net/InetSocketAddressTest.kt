package jako.test.net

import java.net.*

import kotlin.test.*

class InetSocketAddressTest {

    @Test
    fun testConstructorWithPortOnly() {
        val sa = InetSocketAddress(8080)
        assertEquals(8080, sa.getPort())
        assertNotNull(sa.getAddress())
        assertFalse(sa.isUnresolved())
    }

    @Test
    fun testConstructorWithHostAndPort() {
        val sa = InetSocketAddress("127.0.0.1", 9090)
        assertEquals(9090, sa.getPort())
        assertEquals("127.0.0.1", sa.getHostString())
        assertNotNull(sa.getHostName())

        assertNotNull(sa.getAddress())
        assertFalse(sa.isUnresolved())
    }

    @Test
    fun testCreateUnresolved() {
        val sa = InetSocketAddress.createUnresolved("nonexistent.domain.example", 443)
        assertEquals(443, sa.getPort())
        assertEquals("nonexistent.domain.example", sa.getHostName())
        assertTrue(sa.isUnresolved())
        assertNull(sa.getAddress())
    }

    @Test
    fun testEqualsAndHashCode() {
        val sa1 = InetSocketAddress("127.0.0.1", 8080)
        val sa2 = InetSocketAddress("127.0.0.1", 8080)
        val sa3 = InetSocketAddress("127.0.0.1", 8081)

        assertEquals(sa1, sa2)
        assertEquals(sa1.hashCode(), sa2.hashCode())
        assertNotEquals(sa1, sa3)
    }
}
