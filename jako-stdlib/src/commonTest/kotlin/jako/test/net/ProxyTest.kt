package jako.test.net

import java.net.*

import kotlin.test.*

class ProxyTest {

    @Test
    fun testNoProxy() {
        val noProxy = Proxy.NO_PROXY
        assertEquals(Proxy.Type.DIRECT, noProxy.type())
        assertNull(noProxy.address())
        assertEquals("DIRECT", noProxy.toString())
    }

    @Test
    fun testHttpProxy() {
        val sa = InetSocketAddress("proxy.example.com", 8080)
        val proxy = Proxy(Proxy.Type.HTTP, sa)
        assertEquals(Proxy.Type.HTTP, proxy.type())
        assertEquals(sa, proxy.address())
    }
}
