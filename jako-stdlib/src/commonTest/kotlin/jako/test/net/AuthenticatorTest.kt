package jako.test.net

import java.net.*

import kotlin.test.*

class AuthenticatorTest {

    @Test
    fun testPasswordAuthentication() {
        val auth = PasswordAuthentication("user1", "pass123".toCharArray())
        assertEquals("user1", auth.getUserName())
        assertEquals("pass123", auth.getPassword().concatToString())
    }

    @Test
    fun testCustomAuthenticator() {
        var observedType: Authenticator.RequestorType? = null
        var observedHost: String? = null
        var observedPort: Int = -1
        var observedUrl: URL? = null
        val customAuth = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                observedType = getRequestorType()
                observedHost = getRequestingHost()
                observedPort = getRequestingPort()
                observedUrl = getRequestingURL()
                return PasswordAuthentication("admin", "secret".toCharArray())
            }
        }

        try {
            Authenticator.setDefault(customAuth)
            val result = Authenticator.requestPasswordAuthentication(
                "example.com",
                null,
                80,
                "http",
                "realm",
                "basic"
            )
            assertNotNull(result)
            assertEquals("admin", result.getUserName())
            assertEquals("secret", result.getPassword().concatToString())
            assertEquals(Authenticator.RequestorType.SERVER, observedType)
            assertEquals("example.com", observedHost)
            assertEquals(80, observedPort)

            val proxyUrl = URL("http://proxy.example.com:8080/path")
            val proxyResult = Authenticator.requestPasswordAuthentication(
                "proxy.example.com",
                InetAddress.getByName("127.0.0.1"),
                8080,
                "http",
                "proxy-realm",
                "basic",
                proxyUrl,
                Authenticator.RequestorType.PROXY
            )
            assertNotNull(proxyResult)
            assertEquals(Authenticator.RequestorType.PROXY, observedType)
            assertEquals(proxyUrl, observedUrl)
        } finally {
            Authenticator.setDefault(null)
        }
    }
}
