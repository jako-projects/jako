package jako.test.net

import java.net.URI
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.InetSocketAddress
import java.io.IOException
import kotlin.test.*

class ProxySelectorTest {

    @Test
    fun testDefaultProxySelector() {
        val selector = ProxySelector.getDefault()
        assertNotNull(selector)

        val proxies = selector.select(URI("http://example.com"))
        assertNotNull(proxies)
        assertTrue(proxies.isNotEmpty())
        assertEquals(Proxy.Type.DIRECT, proxies[0].type())
    }

    @Test
    fun testCustomProxySelector() {
        val original = ProxySelector.getDefault()
        val customProxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("myproxy", 8080))
        val custom = object : ProxySelector() {
            override fun select(uri: URI?): List<Proxy> = listOf(customProxy)
            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {}
        }

        try {
            ProxySelector.setDefault(custom)
            val selected = ProxySelector.getDefault()?.select(URI("http://test.org"))
            assertNotNull(selected)
            assertEquals(1, selected.size)
            assertEquals(customProxy, selected[0])
        } finally {
            ProxySelector.setDefault(original)
        }
    }
}
