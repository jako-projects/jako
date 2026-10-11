package jako.test.security

import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.security.auth.x500.X500Principal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SecurityTest {

    @Test
    fun testKeyStoreInstance() {
        val ks = KeyStore.getInstance("PKCS12")
        assertNotNull(ks)
        assertEquals("PKCS12", ks.type)
    }

    @Test
    fun testTrustManagerFactory() {
        val tmf = TrustManagerFactory.getInstance("PKIX")
        assertNotNull(tmf)
        tmf.init(null as KeyStore?)
        val tms = tmf.trustManagers
        assertNotNull(tms)
    }

    @Test
    fun testX500Principal() {
        val p = X500Principal("CN=example.com")
        assertEquals("CN=example.com", p.name)
        assertEquals("CN=example.com", p.getName())
    }
}
