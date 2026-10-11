package jako.test.net

import java.net.*

import kotlin.test.*

class AddressesTest {

    @Test
    fun testInetAddressLoopback() {
        val loopback = InetAddress.getLoopbackAddress()
        assertNotNull(loopback)
        assertTrue(loopback.isLoopbackAddress())
        assertEquals("127.0.0.1", loopback.getHostAddress())
    }

    @Test
    fun testInetSocketAddress() {
        val isa = InetSocketAddress(8080)
        assertEquals(8080, isa.getPort())
        assertFalse(isa.isUnresolved())
        assertNotNull(isa.getAddress())

        val unresolved = InetSocketAddress.createUnresolved("example.invalid", 80)
        assertEquals(80, unresolved.getPort())
        assertTrue(unresolved.isUnresolved())
    }

    @Test
    fun testProxy() {
        val noProxy = Proxy.NO_PROXY
        assertEquals(Proxy.Type.DIRECT, noProxy.type())
        assertNull(noProxy.address())

        val isa = InetSocketAddress("127.0.0.1", 8888)
        val httpProxy = Proxy(Proxy.Type.HTTP, isa)
        assertEquals(Proxy.Type.HTTP, httpProxy.type())
        assertEquals(isa, httpProxy.address())
    }
}
