package jako.test.net

import java.net.*

import kotlin.test.*

class InetAddressTest {

    @Test
    fun testParseLoopbackAddress() {
        val loopback = InetAddress.getLoopbackAddress()
        assertNotNull(loopback)
        assertTrue(loopback.isLoopbackAddress())
        val addrBytes = loopback.getAddress()
        assertEquals(4, addrBytes.size)
        assertEquals(127.toByte(), addrBytes[0])
        assertEquals(0.toByte(), addrBytes[1])
        assertEquals(0.toByte(), addrBytes[2])
        assertEquals(1.toByte(), addrBytes[3])
    }

    @Test
    fun testGetByAddressIPv4() {
        val bytes = byteArrayOf(192.toByte(), 168.toByte(), 1.toByte(), 100.toByte())
        val inetAddr = InetAddress.getByAddress("myhost", bytes)
        assertEquals("myhost", inetAddr.getHostName())
        assertEquals("192.168.1.100", inetAddr.getHostAddress())
        assertFalse(inetAddr.isLoopbackAddress())
        assertFalse(inetAddr.isAnyLocalAddress())
    }

    @Test
    fun testGetByNameIPv4String() {
        val inetAddr = InetAddress.getByName("127.0.0.1")
        assertEquals("127.0.0.1", inetAddr.getHostAddress())
        assertTrue(inetAddr.isLoopbackAddress())
    }

    @Test
    fun testGetByNameLocalhost() {
        val inetAddr = InetAddress.getByName("localhost")
        assertNotNull(inetAddr)
        assertTrue(inetAddr.isLoopbackAddress())
    }

    @Test
    fun testEqualsAndHashCode() {
        val a1 = InetAddress.getByAddress(byteArrayOf(10, 0, 0, 1))
        val a2 = InetAddress.getByAddress(byteArrayOf(10, 0, 0, 1))
        val a3 = InetAddress.getByAddress(byteArrayOf(10, 0, 0, 2))

        assertEquals(a1, a2)
        assertEquals(a1.hashCode(), a2.hashCode())
        assertNotEquals(a1, a3)
    }

    @Test
    fun testInet4AddressGetByName() {
        val addr = Inet4Address.getByName("10.0.0.6")
        assertTrue(addr is Inet4Address)
        assertEquals("10.0.0.6", addr.getHostAddress())
        assertEquals(4, addr.getAddress().size)
    }

    @Test
    fun testInet6AddressGetByName() {
        val addr = Inet6Address.getByName("::ac")
        assertTrue(addr is Inet6Address)
        val bytes = addr.getAddress()
        assertEquals(16, bytes.size)
        assertEquals(0xac.toByte(), bytes[15])
        assertEquals(0.toByte(), bytes[14])
    }

    @Test
    fun testInetAddressGetByNameIPv6() {
        val addr = InetAddress.getByName("::1")
        assertTrue(addr is Inet6Address)
        assertTrue(addr.isLoopbackAddress())
    }
}
