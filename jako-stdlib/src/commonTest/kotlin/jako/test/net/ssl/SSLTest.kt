package jako.test.net.ssl

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import kotlin.test.*

class SSLTest {

    @Test
    fun testSSLContext() {
        val context = SSLContext.getInstance("TLS")
        assertNotNull(context)
        context.init(null, null, null)
        val factory = context.getSocketFactory()
        assertNotNull(factory)
    }

    @Test
    fun testDefaultSSLSocketFactory() {
        val factory = SSLSocketFactory.getDefault()
        assertNotNull(factory)
    }
}
