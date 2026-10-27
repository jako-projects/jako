package jako.test.net.ssl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.security.*
import java.security.cert.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.*
import kotlin.test.*

class SSLTest {

    @Test
    fun testSSLContextAndDefaultFactory() {
        val context = SSLContext.getInstance("TLS")
        assertNotNull(context)
        assertEquals("TLS", context.getProtocol())
        context.init(null, null, null)
        val factory = context.getSocketFactory()
        assertNotNull(factory)
        assertTrue(factory.getDefaultCipherSuites().isNotEmpty())
        assertTrue(factory.getSupportedCipherSuites().isNotEmpty())

        val defaultFactory = SSLSocketFactory.getDefault()
        assertNotNull(defaultFactory)
    }

    @Test
    fun testX509CertificateDerAndPemParsingAndVerification() {
        val cf = CertificateFactory.getInstance("X.509")
        val derBytes = TestTlsCredentials.certificateDer
        val cert = cf.generateCertificate(ByteArrayInputStream(derBytes)) as X509Certificate

        assertEquals("X.509", cert.getType())
        assertEquals(3, cert.getVersion())
        assertEquals(BigInteger.valueOf(0x12345678L), cert.getSerialNumber())
        assertTrue(cert.getSubjectX500Principal().getName().contains("CN=localhost"))
        assertTrue(cert.getIssuerX500Principal().getName().contains("CN=localhost"))
        assertTrue(cert.getBasicConstraints() >= 0)

        // Validity check
        cert.checkValidity()
        assertFailsWith<CertificateNotYetValidException> {
            cert.checkValidity(Date(1000L)) // 1970
        }
        assertFailsWith<CertificateExpiredException> {
            cert.checkValidity(Date(2524608000000L)) // 2050
        }

        // SubjectAlternativeNames check
        val sans = cert.getSubjectAlternativeNames()
        assertNotNull(sans)
        val dnsNames = sans.filter { it[0] == 2 }.map { it[1] as String }
        val ipAddrs = sans.filter { it[0] == 7 }.map { it[1] as String }
        assertTrue(dnsNames.contains("localhost"), "Expected 'localhost' in DNS SANs: $dnsNames")
        assertTrue(ipAddrs.contains("127.0.0.1"), "Expected '127.0.0.1' in IP SANs: $ipAddrs")

        // Cryptographic signature verification
        cert.verify(cert.getPublicKey())

        // PEM parsing check
        val pemText = buildString {
            append("-----BEGIN CERTIFICATE-----\n")
            append(Base64.getMimeEncoder().encodeToString(derBytes))
            append("\n-----END CERTIFICATE-----\n")
        }
        val pemCert = cf.generateCertificate(ByteArrayInputStream(pemText.encodeToByteArray())) as X509Certificate
        assertEquals(cert.getSerialNumber(), pemCert.getSerialNumber())
        assertContentEquals(cert.getEncoded(), pemCert.getEncoded())
    }

    @Test
    fun testKeyStoreAndTrustManagerFactory() {
        val cf = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(ByteArrayInputStream(TestTlsCredentials.certificateDer)) as X509Certificate

        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        assertEquals(0, ks.size())
        ks.setCertificateEntry("test-ca", cert)
        assertEquals(1, ks.size())
        assertTrue(ks.containsAlias("test-ca"))
        assertTrue(ks.isCertificateEntry("test-ca"))
        assertEquals("test-ca", ks.getCertificateAlias(cert))
        assertNotNull(ks.getCertificate("test-ca"))

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        val tms = tmf.getTrustManagers()
        assertNotNull(tms)
        val x509Tm = tms.first { it is X509TrustManager } as X509TrustManager
        assertEquals(1, x509Tm.getAcceptedIssuers().size)
        x509Tm.checkServerTrusted(arrayOf(cert), "RSA")

        // Empty KeyStore should reject untrusted certificate
        val emptyKs = KeyStore.getInstance(KeyStore.getDefaultType())
        emptyKs.load(null, null)
        val emptyTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        emptyTmf.init(emptyKs)
        val emptyTm = emptyTmf.getTrustManagers()!!.first { it is X509TrustManager } as X509TrustManager
        assertFailsWith<Exception> {
            emptyTm.checkServerTrusted(arrayOf(cert), "RSA")
        }
    }

    @Test
    fun testRealTls12HandshakeAndEncryptedCommunication() {
        val server = LoopbackTls12Server { clientPlaintext ->
            val msg = clientPlaintext.decodeToString()
            "TLS-ACK:$msg".encodeToByteArray()
        }
        server.start()
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val serverCert = cf.generateCertificate(ByteArrayInputStream(TestTlsCredentials.certificateDer))

            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(null, null)
            ks.setCertificateEntry("loopback", serverCert)

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(ks)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), null)

            val sslSocket = wrapWithTlsRsaCipherSuite(sslContext.getSocketFactory())
                .createSocket("127.0.0.1", server.port) as SSLSocket
            sslSocket.setSoTimeout(5000)
            sslSocket.startHandshake()

            val session = sslSocket.getSession()
            assertTrue(session.isValid())
            assertEquals("TLSv1.2", session.getProtocol())
            assertTrue(session.getCipherSuite().isNotEmpty())
            val peerCerts = session.getPeerCertificates()
            assertTrue(peerCerts.isNotEmpty())
            val leaf = peerCerts[0] as X509Certificate
            assertEquals(BigInteger.valueOf(0x12345678L), leaf.getSerialNumber())

            val out = sslSocket.getOutputStream()
            val inp = sslSocket.getInputStream()
            val payload = "Hello Jako Cross-Platform TLS!".encodeToByteArray()
            out.write(payload)
            out.flush()

            val buf = ByteArray(128)
            val n = inp.read(buf)
            assertTrue(n > 0)
            assertEquals("TLS-ACK:Hello Jako Cross-Platform TLS!", buf.decodeToString(0, n))

            sslSocket.close()
            assertTrue(sslSocket.isClosed())
        } finally {
            server.close()
        }
    }

    @Test
    fun testUntrustedServerCertificateFailsHandshake() {
        val server = LoopbackTls12Server { "unreachable".encodeToByteArray() }
        server.start()
        try {
            val rejectingTm = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    throw CertificateException("Explicitly rejected untrusted server certificate")
                }
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(rejectingTm), null)

            val sslSocket = wrapWithTlsRsaCipherSuite(sslContext.getSocketFactory())
                .createSocket("127.0.0.1", server.port) as SSLSocket
            sslSocket.setSoTimeout(5000)
            assertFailsWith<SSLException> {
                sslSocket.startHandshake()
            }
            sslSocket.close()
        } finally {
            server.close()
        }
    }

    @Test
    fun testHttpsURLConnectionOverLoopbackTls() {
        val server = LoopbackTls12Server { requestBytes ->
            val reqText = requestBytes.decodeToString()
            val body = if (reqText.startsWith("POST")) "HTTPS-POST-OK" else "HTTPS-GET-OK"
            buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: text/plain\r\n")
                append("Content-Length: ${body.length}\r\n")
                append("Connection: close\r\n")
                append("\r\n")
                append(body)
            }.encodeToByteArray()
        }
        server.start()
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val serverCert = cf.generateCertificate(ByteArrayInputStream(TestTlsCredentials.certificateDer))
            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(null, null)
            ks.setCertificateEntry("loopback", serverCert)
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(ks)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), null)

            val url = URL("https://127.0.0.1:${server.port}/status")
            val conn = url.openConnection() as HttpsURLConnection
            conn.setSSLSocketFactory(wrapWithTlsRsaCipherSuite(sslContext.getSocketFactory()))
            conn.setHostnameVerifier(object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    return hostname == "127.0.0.1" && session.getPeerCertificates().isNotEmpty()
                }
            })
            conn.setConnectTimeout(5000)
            conn.setReadTimeout(5000)

            assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode())
            assertEquals("text/plain", conn.getContentType())
            val responseText = conn.getInputStream().readBytes().decodeToString()
            assertEquals("HTTPS-GET-OK", responseText)
            assertTrue(conn.getCipherSuite().isNotEmpty())
            assertTrue(conn.getServerCertificates().isNotEmpty())
            conn.disconnect()
        } finally {
            server.close()
        }
    }

    @Test
    fun testSNIHostNameValidationAndEquality() {
        val sni1 = SNIHostName("example.com")
        assertEquals("example.com", sni1.getAsciiName())
        assertEquals("example.com", sni1.asciiName)
        assertContentEquals("example.com".encodeToByteArray(), sni1.getEncoded())
        assertEquals("type=host_name (0), value=example.com", sni1.toString())

        // Case-insensitive equality and hashCode
        val sniUpper = SNIHostName("EXAMPLE.COM")
        assertEquals(sni1, sniUpper)
        assertEquals(sni1.hashCode(), sniUpper.hashCode())

        // Encoded byte[] constructor
        val sniFromBytes = SNIHostName("api.example.org".encodeToByteArray())
        assertEquals("api.example.org", sniFromBytes.asciiName)

        // IDN punycode conversion
        val sniIdn = SNIHostName("bücher.de")
        assertEquals("xn--bcher-kva.de", sniIdn.asciiName.lowercase())

        // Invalid hostnames rejected with IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            SNIHostName("")
        }
        assertFailsWith<IllegalArgumentException> {
            SNIHostName("example.com.")
        }
        assertFailsWith<IllegalArgumentException> {
            SNIHostName("-invalid.example.com")
        }
        assertFailsWith<IllegalArgumentException> {
            SNIHostName(ByteArray(0))
        }
    }

    @Test
    fun testExtendedSSLSessionContract() {
        val server = LoopbackTls12Server { "OK".encodeToByteArray() }
        server.start()
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val serverCert = cf.generateCertificate(ByteArrayInputStream(TestTlsCredentials.certificateDer))
            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(null, null)
            ks.setCertificateEntry("loopback", serverCert)
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(ks)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), null)

            val rawSocket = Socket("127.0.0.1", server.port)
            val sslSocket = wrapWithTlsRsaCipherSuite(sslContext.getSocketFactory())
                .createSocket(rawSocket, "secure.example.com", server.port, true) as SSLSocket
            sslSocket.setSoTimeout(5000)
            sslSocket.startHandshake()

            val session = sslSocket.getSession()
            assertTrue(session is ExtendedSSLSession)
            val extSession: ExtendedSSLSession = session
            assertEquals("secure.example.com", extSession.getPeerHost())
            assertEquals(server.port, extSession.getPeerPort())
            assertTrue(extSession.isValid())

            val requestedNames = extSession.getRequestedServerNames()
            assertNotNull(requestedNames)
            assertEquals(requestedNames, extSession.requestedServerNames)
            assertTrue(requestedNames.contains(SNIHostName("secure.example.com")))

            sslSocket.close()
        } finally {
            server.close()
        }
    }

    private fun wrapWithTlsRsaCipherSuite(delegate: SSLSocketFactory): SSLSocketFactory {
        return object : SSLSocketFactory() {
            private fun configure(s: Socket): Socket {
                if (s is SSLSocket) {
                    val target = "TLS_RSA_WITH_AES_128_CBC_SHA256"
                    val enabled = s.getEnabledCipherSuites()
                    if (!enabled.contains(target) && s.getSupportedCipherSuites().contains(target)) {
                        s.setEnabledCipherSuites(enabled + target)
                    }
                }
                return s
            }
            override fun getDefaultCipherSuites(): Array<String> = delegate.getDefaultCipherSuites()
            override fun getSupportedCipherSuites(): Array<String> = delegate.getSupportedCipherSuites()
            override fun createSocket(): Socket = configure(delegate.createSocket())
            override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
                configure(delegate.createSocket(s, host, port, autoClose))
            override fun createSocket(host: String, port: Int): Socket =
                configure(delegate.createSocket(host, port))
            override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
                configure(delegate.createSocket(host, port, localHost, localPort))
            override fun createSocket(host: InetAddress, port: Int): Socket =
                configure(delegate.createSocket(host, port))
            override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
                configure(delegate.createSocket(address, port, localAddress, localPort))
        }
    }
}

/**
 * Self-contained 2048-bit RSA keypair and ASN.1 DER X.509 v3 certificate generator for tests.
 */
private object TestTlsCredentials {
    private const val N_HEX =
        "ebf535ae67a7bbd6ec640d6f07989262caa1a3db93fb9c32487a6aa630d678ba1f20b1acb55f2dd8a4ca953cb05aad9304d7186d411b0a645c951c581f57cf65" +
        "777c3db87e5227aac393429c54cd9b9951c7addc4dd1a8bfd39b330cf26eeff55653b314393858adcf727b27e84e58ee010777ad682a8df566b2d8a4f2df5c8e" +
        "2f1829c0542c7bdfb65a2e8cd27d52cad3b39842e8031db5cc000fa66078ba410ab6ec6ce10229f67b33a62445fdd0eb225d2bcdfaae8ba3bf02c06bcf493695" +
        "74ca05f7ac01d947abfcb372d3c22711a3c885d0037e0efa34a228eb18c326127617c59188a958fea47cdf09348e21adb77c22715792d8afd91eb667cd0550f1"
    private const val D_HEX =
        "4a15d40d6564e0ebb5024ddcc29ee3e60bccf3fb43b8f13e02a8d2ea1aa6598253e5e0637ae1f5b94f930a70863bc9abfc539ce256b72ac9977b070514323fbc" +
        "7afe91f566c2b9e268b96b93f808151e1d7eefd6c17ca3da941dcfc9f59bed4533d83d50e83c387bba4373a4175cdc52e12c353d8d7111f800a6790ff7364f83" +
        "2ac36f611bd8ec34ccc2078d0e46d6e5ef24722c34580439f6f99a7fb6e1a914f380345cd09c59e6029ba86079292f1dd4813deeab68ae47ca1584a9e6489e2c" +
        "543ec8e28d970cdd5a77415a73597f4bce8e7e4463dbf6ca63c9a6be017d056e33a9505a3c1a485ea59503168ec45b65a38c53eae7521eee08f51a99da4303d"

    val publicKey: PublicKey
    val privateKey: PrivateKey
    val certificateDer: ByteArray

    init {
        val kf = KeyFactory.getInstance("RSA")
        val n = BigInteger(N_HEX, 16)
        val e = BigInteger.valueOf(65537L)
        val d = BigInteger(D_HEX, 16)
        publicKey = kf.generatePublic(RSAPublicKeySpec(n, e))
        privateKey = kf.generatePrivate(RSAPrivateKeySpec(n, d))
        certificateDer = buildSelfSignedX509v3Certificate(publicKey, privateKey)
    }

    private fun buildSelfSignedX509v3Certificate(pubKey: PublicKey, privKey: PrivateKey): ByteArray {
        // Version: [0] EXPLICIT INTEGER 2 (v3)
        val versionTlv = tlv(0xA0, tlv(0x02, byteArrayOf(0x02)))
        // SerialNumber: 0x12345678
        val serialTlv = tlv(0x02, BigInteger.valueOf(0x12345678L).toByteArray())
        // Signature Algorithm: sha256WithRSAEncryption (1.2.840.113549.1.1.11) + NULL
        val sha256RsaOid = byteArrayOf(0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01, 0x0B, 0x05, 0x00)
        val sigAlgTlv = tlv(0x30, sha256RsaOid)

        // Name: CN=localhost
        val cnOid = byteArrayOf(0x06, 0x03, 0x55, 0x04, 0x03)
        val cnVal = tlv(0x0C, "localhost".encodeToByteArray())
        val nameTlv = tlv(0x30, tlv(0x31, tlv(0x30, cnOid + cnVal)))

        // Validity: 20200101000000Z .. 20400101000000Z
        val notBefore = tlv(0x17, "200101000000Z".encodeToByteArray())
        val notAfter = tlv(0x17, "400101000000Z".encodeToByteArray())
        val validityTlv = tlv(0x30, notBefore + notAfter)

        // SubjectPublicKeyInfo
        val spkiTlv = pubKey.getEncoded()!!

        // Extensions: BasicConstraints (2.5.29.19) + SubjectAltName (2.5.29.17)
        val bcOid = byteArrayOf(0x06, 0x03, 0x55, 0x1D, 0x13)
        val bcVal = tlv(0x04, tlv(0x30, byteArrayOf(0x01, 0x01, 0xFF.toByte())))
        val bcExt = tlv(0x30, bcOid + bcVal)

        val sanOid = byteArrayOf(0x06, 0x03, 0x55, 0x1D, 0x11)
        val dnsSan = tlv(0x82, "localhost".encodeToByteArray())
        val ipSan = tlv(0x87, byteArrayOf(127, 0, 0, 1))
        val sanVal = tlv(0x04, tlv(0x30, dnsSan + ipSan))
        val sanExt = tlv(0x30, sanOid + sanVal)

        val extensionsTlv = tlv(0xA3, tlv(0x30, bcExt + sanExt))

        val tbsCertificate = tlv(
            0x30,
            versionTlv + serialTlv + sigAlgTlv + nameTlv + validityTlv + nameTlv + spkiTlv + extensionsTlv
        )

        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(privKey)
        signer.update(tbsCertificate)
        val sigBytes = signer.sign()
        val bitStringContent = ByteArray(1 + sigBytes.size)
        bitStringContent[0] = 0
        sigBytes.copyInto(bitStringContent, 1)
        val sigBitString = tlv(0x03, bitStringContent)

        return tlv(0x30, tbsCertificate + sigAlgTlv + sigBitString)
    }

    private fun tlv(tag: Int, content: ByteArray): ByteArray {
        val len = content.size
        val lenBytes = if (len < 128) {
            byteArrayOf(len.toByte())
        } else if (len < 256) {
            byteArrayOf(0x81.toByte(), len.toByte())
        } else {
            byteArrayOf(0x82.toByte(), ((len ushr 8) and 0xFF).toByte(), (len and 0xFF).toByte())
        }
        val out = ByteArray(1 + lenBytes.size + len)
        out[0] = tag.toByte()
        lenBytes.copyInto(out, 1)
        content.copyInto(out, 1 + lenBytes.size)
        return out
    }
}

/**
 * Minimal RFC 5246 TLS 1.2 (`TLS_RSA_WITH_AES_128_CBC_SHA256` `0x003C`) loopback server
 * that runs in-process across JVM, macOS, Linux, and Windows tests.
 */
private class LoopbackTls12Server(
    private val handler: (ByteArray) -> ByteArray
) {
    private val serverSocket = ServerSocket(0)
    val port: Int get() = serverSocket.getLocalPort()
    private var serverThread: Thread? = null
    private var closed = false

    private var unregisterPolling: (() -> Unit)? = null

    fun start() {
        val t = Thread {
            try {
                val client = serverSocket.accept()
                client.setSoTimeout(5000)
                handleClient(client)
            } catch (_: Exception) {
            }
        }
        serverThread = t
        try {
            t.start()
        } catch (_: UnsupportedOperationException) {
            startCooperativeLoopbackServer()
        }
    }

    fun close() {
        closed = true
        try {
            serverSocket.close()
        } catch (_: Exception) {
        }
    }

    private fun startCooperativeLoopbackServer() {
        var acceptedSocket: Socket? = null
        var stage = 0
        val transcript = ByteArrayOutputStream()
        val random = SecureRandom()
        var clientRandom = ByteArray(0)
        var serverRandom = ByteArray(0)
        var masterSecret = ByteArray(0)
        var clientWriteMacKey = ByteArray(0)
        var serverWriteMacKey = ByteArray(0)
        var clientWriteKey = ByteArray(0)
        var serverWriteKey = ByteArray(0)
        var clientSeqNum = 0L
        var serverSeqNum = 0L

        val poller = object : java.lang.Runnable {
            override fun run() {
                if (closed || stage >= 3) {
                    return
                }
                try {
                    if (acceptedSocket == null) {
                        serverSocket.setSoTimeout(1)
                        try {
                            acceptedSocket = serverSocket.accept()
                        } catch (_: Exception) {
                        }
                    }
                    val s = acceptedSocket
                    if (s != null && !s.isClosed()) {
                        val inp = s.getInputStream()
                        val out = s.getOutputStream()
                        if (stage == 0 && inp.available() >= 5) {
                            val (recType, chRecord) = readRecord(inp)
                            if (recType == 22) {
                                transcript.write(chRecord)
                                clientRandom = chRecord.copyOfRange(6, 38)
                                serverRandom = ByteArray(32).also { random.nextBytes(it) }
                                for (i in 24 until 32) serverRandom[i] = 0x00
                                val shBody = ByteArrayOutputStream().apply {
                                    write(0x03); write(0x03)
                                    write(serverRandom)
                                    write(0x00)
                                    write(0x00); write(0x3C)
                                    write(0x00)
                                    write(0x00); write(0x05)
                                    write(0xFF); write(0x01)
                                    write(0x00); write(0x01)
                                    write(0x00)
                                }.toByteArray()
                                val shMsg = wrapHandshake(2, shBody)
                                transcript.write(shMsg)
                                writeRecord(out, 22, shMsg)

                                val certDer = TestTlsCredentials.certificateDer
                                val certListLen = 3 + certDer.size
                                val certBody = ByteArrayOutputStream().apply {
                                    write((certListLen ushr 16) and 0xFF)
                                    write((certListLen ushr 8) and 0xFF)
                                    write(certListLen and 0xFF)
                                    write((certDer.size ushr 16) and 0xFF)
                                    write((certDer.size ushr 8) and 0xFF)
                                    write(certDer.size and 0xFF)
                                    write(certDer)
                                }.toByteArray()
                                val certMsg = wrapHandshake(11, certBody)
                                transcript.write(certMsg)
                                writeRecord(out, 22, certMsg)

                                val shdMsg = wrapHandshake(14, ByteArray(0))
                                transcript.write(shdMsg)
                                writeRecord(out, 22, shdMsg)
                                stage = 1
                            }
                        }
                        if (stage == 1 && inp.available() >= 5) {
                            val (ckeRecType, ckeRecord) = readRecord(inp)
                            if (ckeRecType == 22) {
                                transcript.write(ckeRecord)
                                val encPmsLen = ((ckeRecord[4].toInt() and 0xFF) shl 8) or (ckeRecord[5].toInt() and 0xFF)
                                val encPms = ckeRecord.copyOfRange(6, 6 + encPmsLen)
                                val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                                rsaCipher.init(Cipher.DECRYPT_MODE, TestTlsCredentials.privateKey)
                                val preMasterSecret = rsaCipher.doFinal(encPms)

                                masterSecret = prf(preMasterSecret, "master secret", clientRandom + serverRandom, 48)
                                val keyBlock = prf(masterSecret, "key expansion", serverRandom + clientRandom, 128)
                                clientWriteMacKey = keyBlock.copyOfRange(0, 32)
                                serverWriteMacKey = keyBlock.copyOfRange(32, 64)
                                clientWriteKey = keyBlock.copyOfRange(64, 80)
                                serverWriteKey = keyBlock.copyOfRange(80, 96)

                                val (ccsType, _) = readRecord(inp)
                                val (finType, encClientFin) = readRecord(inp)
                                if (ccsType == 20 && finType == 22) {
                                    val clientFinMsg = decryptCbcRecord(clientSeqNum++, 22, encClientFin, clientWriteKey, clientWriteMacKey)
                                    transcript.write(clientFinMsg)

                                    writeRecord(out, 20, byteArrayOf(0x01))
                                    val srvHash = MessageDigest.getInstance("SHA-256").digest(transcript.toByteArray())
                                    val srvVerify = prf(masterSecret, "server finished", srvHash, 12)
                                    val srvFinMsg = wrapHandshake(20, srvVerify)
                                    val encSrvFin = encryptCbcRecord(serverSeqNum++, 22, srvFinMsg, serverWriteKey, serverWriteMacKey, random)
                                    writeRecord(out, 22, encSrvFin)
                                    stage = 2
                                }
                            } else {
                                stage = 3
                                s.close()
                            }
                        }
                        if (stage == 2 && inp.available() >= 5) {
                            val (appType, encApp) = readRecord(inp)
                            val plain = decryptCbcRecord(clientSeqNum++, appType, encApp, clientWriteKey, clientWriteMacKey)
                            if (appType == 23 && plain.isNotEmpty()) {
                                val reply = handler(plain)
                                val encReply = encryptCbcRecord(serverSeqNum++, 23, reply, serverWriteKey, serverWriteMacKey, random)
                                writeRecord(out, 23, encReply)
                                val closeAlert = encryptCbcRecord(serverSeqNum++, 21, byteArrayOf(1, 0), serverWriteKey, serverWriteMacKey, random)
                                writeRecord(out, 21, closeAlert)
                            }
                            stage = 3
                            s.close()
                        }
                    }
                } catch (_: Throwable) {
                    stage = 3
                }
                if (!closed && stage < 3) {
                    java.util.concurrent.ForkJoinPool.commonPool().execute(this)
                }
            }
        }
        java.util.concurrent.ForkJoinPool.commonPool().execute(poller)
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            val inp = s.getInputStream()
            val out = s.getOutputStream()
            val transcript = ByteArrayOutputStream()
            val random = SecureRandom()

            // 1. Read ClientHello
            val (recType, chRecord) = readRecord(inp)
            if (recType != 22) return
            transcript.write(chRecord)
            // ClientHello body starts at chRecord[4]; clientRandom is at chRecord[6..38]
            val clientRandom = chRecord.copyOfRange(6, 38)

            // 2. Send ServerHello (TLS 1.2 0x0303 + TLS_RSA_WITH_AES_128_CBC_SHA256 0x003C)
            val serverRandom = ByteArray(32).also { random.nextBytes(it) }
            // Ensure last 8 bytes do not trigger TLS 1.3 downgrade sentinel
            for (i in 24 until 32) serverRandom[i] = 0x00
            val shBody = ByteArrayOutputStream().apply {
                write(0x03); write(0x03) // TLS 1.2
                write(serverRandom)
                write(0x00) // session_id len = 0
                write(0x00); write(0x3C) // TLS_RSA_WITH_AES_128_CBC_SHA256
                write(0x00) // compression = null
                // RFC 5746 renegotiation_info extension (0xff01, length 1, empty renegotiated_connection 0x00)
                write(0x00); write(0x05)
                write(0xFF); write(0x01)
                write(0x00); write(0x01)
                write(0x00)
            }.toByteArray()
            val shMsg = wrapHandshake(2, shBody)
            transcript.write(shMsg)
            writeRecord(out, 22, shMsg)

            // 3. Send Certificate
            val certDer = TestTlsCredentials.certificateDer
            val certListLen = 3 + certDer.size
            val certBody = ByteArrayOutputStream().apply {
                write((certListLen ushr 16) and 0xFF)
                write((certListLen ushr 8) and 0xFF)
                write(certListLen and 0xFF)
                write((certDer.size ushr 16) and 0xFF)
                write((certDer.size ushr 8) and 0xFF)
                write(certDer.size and 0xFF)
                write(certDer)
            }.toByteArray()
            val certMsg = wrapHandshake(11, certBody)
            transcript.write(certMsg)
            writeRecord(out, 22, certMsg)

            // 4. Send ServerHelloDone
            val shdMsg = wrapHandshake(14, ByteArray(0))
            transcript.write(shdMsg)
            writeRecord(out, 22, shdMsg)

            // 5. Read ClientKeyExchange
            val (ckeRecType, ckeRecord) = readRecord(inp)
            if (ckeRecType != 22) return
            transcript.write(ckeRecord)
            val encPmsLen = ((ckeRecord[4].toInt() and 0xFF) shl 8) or (ckeRecord[5].toInt() and 0xFF)
            val encPms = ckeRecord.copyOfRange(6, 6 + encPmsLen)

            val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            rsaCipher.init(Cipher.DECRYPT_MODE, TestTlsCredentials.privateKey)
            val preMasterSecret = rsaCipher.doFinal(encPms)

            val masterSecret = prf(preMasterSecret, "master secret", clientRandom + serverRandom, 48)
            val keyBlock = prf(masterSecret, "key expansion", serverRandom + clientRandom, 128)
            val clientWriteMacKey = keyBlock.copyOfRange(0, 32)
            val serverWriteMacKey = keyBlock.copyOfRange(32, 64)
            val clientWriteKey = keyBlock.copyOfRange(64, 80)
            val serverWriteKey = keyBlock.copyOfRange(80, 96)

            // 6. Read Client ChangeCipherSpec & Encrypted Finished
            val (ccsType, _) = readRecord(inp)
            if (ccsType != 20) return
            val (finType, encClientFin) = readRecord(inp)
            if (finType != 22) return
            var clientSeqNum = 0L
            val clientFinMsg = decryptCbcRecord(clientSeqNum++, 22, encClientFin, clientWriteKey, clientWriteMacKey)
            transcript.write(clientFinMsg)

            // 7. Send Server ChangeCipherSpec & Encrypted Finished
            writeRecord(out, 20, byteArrayOf(0x01))
            val srvHash = MessageDigest.getInstance("SHA-256").digest(transcript.toByteArray())
            val srvVerify = prf(masterSecret, "server finished", srvHash, 12)
            val srvFinMsg = wrapHandshake(20, srvVerify)
            var serverSeqNum = 0L
            val encSrvFin = encryptCbcRecord(serverSeqNum++, 22, srvFinMsg, serverWriteKey, serverWriteMacKey, random)
            writeRecord(out, 22, encSrvFin)

            // 8. Read Client ApplicationData and send response
            while (true) {
                val (appType, encApp) = try {
                    readRecord(inp)
                } catch (_: EOFException) {
                    break
                }
                val plain = decryptCbcRecord(clientSeqNum++, appType, encApp, clientWriteKey, clientWriteMacKey)
                if (appType == 21) break // alert
                if (appType == 23 && plain.isNotEmpty()) {
                    val reply = handler(plain)
                    val encReply = encryptCbcRecord(serverSeqNum++, 23, reply, serverWriteKey, serverWriteMacKey, random)
                    writeRecord(out, 23, encReply)
                    // Send close_notify
                    val closeAlert = encryptCbcRecord(serverSeqNum++, 21, byteArrayOf(1, 0), serverWriteKey, serverWriteMacKey, random)
                    writeRecord(out, 21, closeAlert)
                    break
                }
            }
        }
    }

    private fun encryptCbcRecord(
        seqNum: Long,
        contentType: Int,
        plaintext: ByteArray,
        encKey: ByteArray,
        macKey: ByteArray,
        random: SecureRandom
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(macKey, "HmacSHA256"))
        mac.update(recordHeaderAad(seqNum, contentType, plaintext.size))
        mac.update(plaintext)
        val macBytes = mac.doFinal()
        val dataWithMac = plaintext + macBytes
        val padLen = 16 - (dataWithMac.size % 16)
        val padded = ByteArray(dataWithMac.size + padLen)
        dataWithMac.copyInto(padded, 0)
        for (i in dataWithMac.size until padded.size) padded[i] = (padLen - 1).toByte()

        val iv = ByteArray(16).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encKey, "AES"), IvParameterSpec(iv))
        return iv + cipher.doFinal(padded)
    }

    private fun decryptCbcRecord(
        seqNum: Long,
        contentType: Int,
        fragment: ByteArray,
        decKey: ByteArray,
        macKey: ByteArray
    ): ByteArray {
        val iv = fragment.copyOfRange(0, 16)
        val ct = fragment.copyOfRange(16, fragment.size)
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(decKey, "AES"), IvParameterSpec(iv))
        val dec = cipher.doFinal(ct)
        val padTotal = (dec.last().toInt() and 0xFF) + 1
        val contentLen = dec.size - padTotal - 32
        return dec.copyOfRange(0, contentLen)
    }

    private fun recordHeaderAad(seqNum: Long, contentType: Int, length: Int): ByteArray {
        val aad = ByteArray(13)
        for (i in 0 until 8) aad[7 - i] = ((seqNum ushr (i * 8)) and 0xFF).toByte()
        aad[8] = contentType.toByte()
        aad[9] = 0x03
        aad[10] = 0x03
        aad[11] = ((length ushr 8) and 0xFF).toByte()
        aad[12] = (length and 0xFF).toByte()
        return aad
    }

    private fun prf(secret: ByteArray, label: String, seed: ByteArray, outLen: Int): ByteArray {
        val labelAndSeed = label.encodeToByteArray() + seed
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(secret, "HmacSHA256")
        mac.init(keySpec)
        val result = ByteArray(outLen)
        var a = mac.doFinal(labelAndSeed)
        var off = 0
        while (off < outLen) {
            mac.init(keySpec)
            mac.update(a)
            mac.update(labelAndSeed)
            val block = mac.doFinal()
            val copyLen = minOf(block.size, outLen - off)
            block.copyInto(result, off, 0, copyLen)
            off += copyLen
            if (off < outLen) {
                mac.init(keySpec)
                a = mac.doFinal(a)
            }
        }
        return result
    }

    private fun wrapHandshake(type: Int, body: ByteArray): ByteArray {
        val out = ByteArray(4 + body.size)
        out[0] = type.toByte()
        out[1] = ((body.size ushr 16) and 0xFF).toByte()
        out[2] = ((body.size ushr 8) and 0xFF).toByte()
        out[3] = (body.size and 0xFF).toByte()
        body.copyInto(out, 4)
        return out
    }

    private fun writeRecord(out: OutputStream, type: Int, payload: ByteArray) {
        out.write(
            byteArrayOf(
                type.toByte(),
                0x03,
                0x03,
                ((payload.size ushr 8) and 0xFF).toByte(),
                (payload.size and 0xFF).toByte()
            )
        )
        out.write(payload)
        out.flush()
    }

    private fun readRecord(inp: InputStream): Pair<Int, ByteArray> {
        val hdr = readExact(inp, 5)
        val type = hdr[0].toInt() and 0xFF
        val len = ((hdr[3].toInt() and 0xFF) shl 8) or (hdr[4].toInt() and 0xFF)
        return Pair(type, readExact(inp, len))
    }

    private fun readExact(inp: InputStream, len: Int): ByteArray {
        val buf = ByteArray(len)
        var off = 0
        while (off < len) {
            val n = inp.read(buf, off, len - off)
            if (n < 0) throw EOFException()
            off += n
        }
        return buf
    }
}
