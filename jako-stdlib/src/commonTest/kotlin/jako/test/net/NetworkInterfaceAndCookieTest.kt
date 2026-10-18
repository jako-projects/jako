package jako.test.net

import java.net.BindException
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpCookie
import java.net.HttpRetryException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import java.net.SocketOptions
import java.net.StandardSocketOptions
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NetworkInterfaceAndCookieTest {

    @Test
    fun testNetworkExceptionsAndSocketOptions() {
        val bindEx = BindException("Address already in use")
        assertEquals("Address already in use", bindEx.message)

        val noRouteEx = NoRouteToHostException("No route")
        assertEquals("No route", noRouteEx.message)

        val portEx = PortUnreachableException("Port unreachable")
        assertEquals("Port unreachable", portEx.message)

        val retryEx = HttpRetryException("Moved", 302, "https://example.com/login")
        assertEquals(302, retryEx.responseCode())
        assertEquals("Moved", retryEx.reason)
        assertEquals("https://example.com/login", retryEx.location)

        assertEquals("SO_KEEPALIVE", StandardSocketOptions.SO_KEEPALIVE.name())
        assertEquals("TCP_NODELAY", StandardSocketOptions.TCP_NODELAY.name())
        assertEquals("SO_RCVBUF", StandardSocketOptions.SO_RCVBUF.name())
        assertEquals(0x0001, SocketOptions.TCP_NODELAY)
        assertEquals(0x0008, SocketOptions.SO_KEEPALIVE)
    }

    @Test
    fun testNetworkInterface() {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        assertNotNull(interfaces)
        assertTrue(interfaces.hasMoreElements())
        val first = interfaces.nextElement()
        assertNotNull(first.name)

        val loopbackAddr = InetAddress.getByName("127.0.0.1")
        val loIf = NetworkInterface.getByInetAddress(loopbackAddr)
        assertNotNull(loIf)
        assertTrue(loIf.isLoopback())
        assertTrue(loIf.isUp())
        assertTrue(loIf.interfaceAddresses.isNotEmpty())
        val byName = NetworkInterface.getByName(loIf.name)
        assertNotNull(byName)
        assertEquals(loIf.name, byName.name)
    }

    @Test
    fun testHttpCookieAndCookieManager() {
        val parsed = HttpCookie.parse("Set-Cookie: session_id=xyz123; Path=/app; Domain=.example.com; Max-Age=3600; Secure; HttpOnly")
        assertEquals(1, parsed.size)
        val cookie = parsed[0]
        assertEquals("session_id", cookie.name)
        assertEquals("xyz123", cookie.value)
        assertEquals("/app", cookie.path)
        assertEquals(".example.com", cookie.domain)
        assertEquals(3600L, cookie.maxAge)
        assertTrue(cookie.secure)
        assertTrue(cookie.isHttpOnly)
        assertFalse(cookie.hasExpired())

        assertTrue(HttpCookie.domainMatches(".example.com", "www.example.com"))
        assertFalse(HttpCookie.domainMatches(".example.com", "sub.www.example.com"))

        val manager = CookieManager(null, CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(manager)
        assertSame(manager, CookieHandler.getDefault())

        val uri = URI("https://www.example.com/app/dashboard")
        manager.put(
            uri,
            mapOf("Set-Cookie" to listOf("theme=dark; Path=/app; Domain=.example.com"))
        )

        val store = manager.cookieStore
        assertEquals(1, store.getCookies().size)
        assertEquals("theme", store.getCookies()[0].name)

        val reqHeaders = manager.get(uri, emptyMap())
        val cookieHeaders = reqHeaders["Cookie"]
        assertNotNull(cookieHeaders)
        assertTrue(cookieHeaders.any { it.contains("theme=dark") })

        assertTrue(store.removeAll())
        assertEquals(0, store.getCookies().size)
        CookieHandler.setDefault(null)
        assertNull(CookieHandler.getDefault())
    }
}
