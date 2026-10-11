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
        val customAuth = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
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
        } finally {
            Authenticator.setDefault(null)
        }
    }
}
