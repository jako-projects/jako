package javax.net.ssl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Pure-Kotlin RFC 5246 TLS 1.2 client socket implementation (`TLS_RSA_WITH_AES_128_GCM_SHA256` &
 * `TLS_RSA_WITH_AES_128_CBC_SHA256`) used as a zero-dependency fallback when native OpenSSL DLLs/SOs
 * are unavailable in minimal environments.
 */
internal class Tls12ClientSocket(
    underlyingSocket: Socket,
    host: String,
    peerPort: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager? = null
) : DelegatingSSLSocket(underlyingSocket, host, peerPort, autoClose, trustManager) {

    private var isHandshakeDone = false
    private var isClosedSocket = false
    private var peerCerts: Array<Certificate> = emptyArray()
    private var negotiatedCipherSuite = "TLS_RSA_WITH_AES_128_CBC_SHA256"
    private var negotiatedCipherCode = CIPHER_RSA_AES_128_CBC_SHA256

    private var clientWriteMacKey = ByteArray(0)
    private var serverWriteMacKey = ByteArray(0)
    private var clientWriteKey = ByteArray(0)
    private var serverWriteKey = ByteArray(0)
    private var clientWriteIv = ByteArray(0)
    private var serverWriteIv = ByteArray(0)

    private var clientSeqNum = 0L
    private var serverSeqNum = 0L

    private var decryptedBuffer = ByteArray(0)
    private var decryptedOffset = 0

    private val random = SecureRandom()

    private val sslInputStream: InputStream by lazy {
        object : InputStream() {
            override fun read(): Int {
                val b = ByteArray(1)
                val n = readAppData(b, 0, 1)
                return if (n <= 0) -1 else b[0].toInt() and 0xFF
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
                if (len == 0) return 0
                return readAppData(b, off, len)
            }

            override fun close() = this@Tls12ClientSocket.close()
        }
    }

    private val sslOutputStream: OutputStream by lazy {
        object : OutputStream() {
            override fun write(b: Int) {
                writeAppData(byteArrayOf(b.toByte()), 0, 1)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
                if (len == 0) return
                writeAppData(b, off, len)
            }

            override fun close() = this@Tls12ClientSocket.close()
        }
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        if (isClosedSocket) throw SocketException("Socket is closed")
        ensureUnderlyingConnected()

        try {
            val rawIn = underlyingSocket.getInputStream()
            val rawOut = underlyingSocket.getOutputStream()
            val handshakeTranscript = ByteArrayOutputStream()

            // 1. Send ClientHello
            val clientRandom = ByteArray(32).also { random.nextBytes(it) }
            val clientHelloBody = buildClientHelloBody(clientRandom, peerHostName)
            val clientHelloMsg = wrapHandshakeMessage(HANDSHAKE_CLIENT_HELLO, clientHelloBody)
            handshakeTranscript.write(clientHelloMsg)
            writeTlsRecord(rawOut, RECORD_HANDSHAKE, clientHelloMsg)

            // 2. Receive ServerHello, Certificate, ServerHelloDone
            var serverRandom: ByteArray? = null
            var serverDone = false
            val handshakeBuffer = ByteArrayOutputStream()

            while (!serverDone) {
                val (recType, payload) = readTlsRecord(rawIn)
                if (recType == RECORD_ALERT) {
                    throw SSLHandshakeException("Received TLS alert from server during handshake: ${payload.contentToString()}")
                }
                if (recType != RECORD_HANDSHAKE) {
                    throw SSLHandshakeException("Unexpected TLS record type during handshake: $recType")
                }
                handshakeBuffer.write(payload)
                val bytes = handshakeBuffer.toByteArray()
                var pos = 0
                while (pos + 4 <= bytes.size) {
                    val hsType = bytes[pos].toInt() and 0xFF
                    val hsLen = ((bytes[pos + 1].toInt() and 0xFF) shl 16) or
                            ((bytes[pos + 2].toInt() and 0xFF) shl 8) or
                            (bytes[pos + 3].toInt() and 0xFF)
                    if (pos + 4 + hsLen > bytes.size) break
                    val fullHsMsg = bytes.copyOfRange(pos, pos + 4 + hsLen)
                    val hsBody = bytes.copyOfRange(pos + 4, pos + 4 + hsLen)
                    handshakeTranscript.write(fullHsMsg)

                    when (hsType) {
                        HANDSHAKE_SERVER_HELLO -> {
                            if (hsBody.size < 38) throw SSLHandshakeException("Malformed ServerHello")
                            serverRandom = hsBody.copyOfRange(2, 34)
                            val sessionIdLen = hsBody[34].toInt() and 0xFF
                            val cipherPos = 35 + sessionIdLen
                            negotiatedCipherCode = ((hsBody[cipherPos].toInt() and 0xFF) shl 8) or
                                    (hsBody[cipherPos + 1].toInt() and 0xFF)
                            negotiatedCipherSuite = when (negotiatedCipherCode) {
                                CIPHER_RSA_AES_128_GCM_SHA256 -> "TLS_RSA_WITH_AES_128_GCM_SHA256"
                                CIPHER_RSA_AES_128_CBC_SHA256 -> "TLS_RSA_WITH_AES_128_CBC_SHA256"
                                else -> throw SSLHandshakeException("Unsupported cipher suite selected by server: 0x${negotiatedCipherCode.toString(16)}")
                            }
                        }
                        HANDSHAKE_CERTIFICATE -> {
                            peerCerts = parseCertificateList(hsBody)
                        }
                        HANDSHAKE_SERVER_HELLO_DONE -> {
                            serverDone = true
                        }
                    }
                    pos += 4 + hsLen
                }
                handshakeBuffer.reset()
                if (pos < bytes.size) {
                    handshakeBuffer.write(bytes, pos, bytes.size - pos)
                }
            }

            val srvRand = serverRandom ?: throw SSLHandshakeException("Missing ServerHello")
            if (peerCerts.isEmpty()) throw SSLHandshakeException("Server did not provide any certificates")

            // 3. Verify server certificate chain via TrustManager
            val x509Chain = peerCerts.filterIsInstance<X509Certificate>().toTypedArray()
            val tm = trustManager
            if (tm != null && tm !is SystemX509TrustManager) {
                try {
                    tm.checkServerTrusted(x509Chain, "RSA")
                } catch (e: Exception) {
                    throw SSLHandshakeException(e.message ?: "Certificate verification failed").apply { initCause(e) }
                }
            } else {
                // Default system trust: require non-self-signed valid certificate or reject untrusted self-signed cert
                for (cert in x509Chain) cert.checkValidity()
                if (x509Chain.size == 1 && x509Chain[0].getIssuerX500Principal() == x509Chain[0].getSubjectX500Principal()) {
                    throw SSLHandshakeException("Self-signed server certificate is not trusted by system default TrustManager")
                }
            }

            // 4. Generate PreMasterSecret and send ClientKeyExchange
            val preMasterSecret = ByteArray(48)
            random.nextBytes(preMasterSecret)
            preMasterSecret[0] = 0x03
            preMasterSecret[1] = 0x03

            val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, x509Chain[0].getPublicKey())
            val encryptedPms = rsaCipher.doFinal(preMasterSecret)
            val ckeBody = ByteArray(2 + encryptedPms.size)
            ckeBody[0] = ((encryptedPms.size ushr 8) and 0xFF).toByte()
            ckeBody[1] = (encryptedPms.size and 0xFF).toByte()
            encryptedPms.copyInto(ckeBody, 2)
            val ckeMsg = wrapHandshakeMessage(HANDSHAKE_CLIENT_KEY_EXCHANGE, ckeBody)
            handshakeTranscript.write(ckeMsg)
            writeTlsRecord(rawOut, RECORD_HANDSHAKE, ckeMsg)

            // 5. Derive MasterSecret & KeyBlock
            val masterSecret = tls12Prf(
                secret = preMasterSecret,
                label = "master secret",
                seed = clientRandom + srvRand,
                outputLength = 48
            )
            deriveKeys(masterSecret, clientRandom, srvRand)

            // 6. Send ChangeCipherSpec and encrypted Client Finished
            writeTlsRecord(rawOut, RECORD_CHANGE_CIPHER_SPEC, byteArrayOf(0x01))
            val clientHandshakeHash = MessageDigest.getInstance("SHA-256").digest(handshakeTranscript.toByteArray())
            val clientVerifyData = tls12Prf(masterSecret, "client finished", clientHandshakeHash, 12)
            val clientFinishedMsg = wrapHandshakeMessage(HANDSHAKE_FINISHED, clientVerifyData)
            handshakeTranscript.write(clientFinishedMsg)
            val encClientFinished = encryptRecordPayload(RECORD_HANDSHAKE, clientFinishedMsg)
            writeTlsRecord(rawOut, RECORD_HANDSHAKE, encClientFinished)

            // 7. Receive Server ChangeCipherSpec and encrypted Server Finished
            val (ccsType, _) = readTlsRecord(rawIn)
            if (ccsType != RECORD_CHANGE_CIPHER_SPEC) {
                throw SSLHandshakeException("Expected ChangeCipherSpec from server, got $ccsType")
            }
            val (finType, encServerFinished) = readTlsRecord(rawIn)
            if (finType != RECORD_HANDSHAKE) {
                throw SSLHandshakeException("Expected encrypted Finished from server, got $finType")
            }
            val serverFinishedMsg = decryptRecordPayload(RECORD_HANDSHAKE, encServerFinished)
            if (serverFinishedMsg.size < 16 || (serverFinishedMsg[0].toInt() and 0xFF) != HANDSHAKE_FINISHED) {
                throw SSLHandshakeException("Invalid Server Finished handshake message")
            }
            val expectedServerHash = MessageDigest.getInstance("SHA-256").digest(handshakeTranscript.toByteArray())
            val expectedServerVerify = tls12Prf(masterSecret, "server finished", expectedServerHash, 12)
            val actualServerVerify = serverFinishedMsg.copyOfRange(4, 16)
            if (!MessageDigest.isEqual(expectedServerVerify, actualServerVerify)) {
                throw SSLHandshakeException("Server Finished verify_data mismatch")
            }

            isHandshakeDone = true
        } catch (e: SSLException) {
            close()
            throw e
        } catch (e: Exception) {
            close()
            throw SSLHandshakeException(e.message ?: "TLS 1.2 handshake failed").apply { initCause(e) }
        }
    }

    private fun deriveKeys(masterSecret: ByteArray, clientRandom: ByteArray, serverRandom: ByteArray) {
        if (negotiatedCipherCode == CIPHER_RSA_AES_128_GCM_SHA256) {
            // 2 * 16B AES keys + 2 * 4B implicit salt = 40 bytes
            val kb = tls12Prf(masterSecret, "key expansion", serverRandom + clientRandom, 40)
            clientWriteKey = kb.copyOfRange(0, 16)
            serverWriteKey = kb.copyOfRange(16, 32)
            clientWriteIv = kb.copyOfRange(32, 36)
            serverWriteIv = kb.copyOfRange(36, 40)
        } else {
            // 2 * 32B MAC keys + 2 * 16B AES keys + 2 * 16B IVs = 128 bytes
            val kb = tls12Prf(masterSecret, "key expansion", serverRandom + clientRandom, 128)
            clientWriteMacKey = kb.copyOfRange(0, 32)
            serverWriteMacKey = kb.copyOfRange(32, 64)
            clientWriteKey = kb.copyOfRange(64, 80)
            serverWriteKey = kb.copyOfRange(80, 96)
            clientWriteIv = kb.copyOfRange(96, 112)
            serverWriteIv = kb.copyOfRange(112, 128)
        }
    }

    private fun encryptRecordPayload(contentType: Int, plaintext: ByteArray): ByteArray {
        val seq = clientSeqNum++
        return if (negotiatedCipherCode == CIPHER_RSA_AES_128_GCM_SHA256) {
            val explicitNonce = ByteArray(8)
            for (i in 0 until 8) explicitNonce[7 - i] = ((seq ushr (i * 8)) and 0xFF).toByte()
            val nonce = clientWriteIv + explicitNonce
            val aad = buildRecordAad(seq, contentType, plaintext.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(clientWriteKey, "AES"), GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            explicitNonce + cipher.doFinal(plaintext)
        } else {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(clientWriteMacKey, "HmacSHA256"))
            mac.update(buildRecordAad(seq, contentType, plaintext.size))
            mac.update(plaintext)
            val macBytes = mac.doFinal()

            val dataWithMac = plaintext + macBytes
            val padLen = 16 - (dataWithMac.size % 16)
            val padded = ByteArray(dataWithMac.size + padLen)
            dataWithMac.copyInto(padded, 0)
            for (i in dataWithMac.size until padded.size) {
                padded[i] = (padLen - 1).toByte()
            }
            val explicitIv = ByteArray(16).also { random.nextBytes(it) }
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(clientWriteKey, "AES"), IvParameterSpec(explicitIv))
            explicitIv + cipher.doFinal(padded)
        }
    }

    private fun decryptRecordPayload(contentType: Int, fragment: ByteArray): ByteArray {
        val seq = serverSeqNum++
        return if (negotiatedCipherCode == CIPHER_RSA_AES_128_GCM_SHA256) {
            if (fragment.size < 24) throw SSLProtocolException("GCM record too short")
            val explicitNonce = fragment.copyOfRange(0, 8)
            val ciphertextAndTag = fragment.copyOfRange(8, fragment.size)
            val plaintextLen = ciphertextAndTag.size - 16
            val nonce = serverWriteIv + explicitNonce
            val aad = buildRecordAad(seq, contentType, plaintextLen)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(serverWriteKey, "AES"), GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            cipher.doFinal(ciphertextAndTag)
        } else {
            if (fragment.size < 48 || fragment.size % 16 != 0) {
                throw SSLProtocolException("CBC record length invalid: ${fragment.size}")
            }
            val explicitIv = fragment.copyOfRange(0, 16)
            val ciphertext = fragment.copyOfRange(16, fragment.size)
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(serverWriteKey, "AES"), IvParameterSpec(explicitIv))
            val decrypted = cipher.doFinal(ciphertext)
            val padByte = decrypted.last().toInt() and 0xFF
            val padTotal = padByte + 1
            if (padTotal + 32 > decrypted.size) throw SSLProtocolException("Invalid TLS CBC padding")
            val contentLen = decrypted.size - padTotal - 32
            val content = decrypted.copyOfRange(0, contentLen)
            val receivedMac = decrypted.copyOfRange(contentLen, contentLen + 32)

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(serverWriteMacKey, "HmacSHA256"))
            mac.update(buildRecordAad(seq, contentType, contentLen))
            mac.update(content)
            val expectedMac = mac.doFinal()
            if (!MessageDigest.isEqual(receivedMac, expectedMac)) {
                throw SSLProtocolException("TLS record MAC verification failed")
            }
            content
        }
    }

    private fun readAppData(b: ByteArray, off: Int, len: Int): Int {
        if (!isHandshakeDone) startHandshake()
        if (decryptedOffset < decryptedBuffer.size) {
            val avail = minOf(len, decryptedBuffer.size - decryptedOffset)
            decryptedBuffer.copyInto(b, off, decryptedOffset, decryptedOffset + avail)
            decryptedOffset += avail
            return avail
        }
        val rawIn = underlyingSocket.getInputStream()
        while (true) {
            val (recType, encPayload) = try {
                readTlsRecord(rawIn)
            } catch (_: EOFException) {
                return -1
            }
            val plain = decryptRecordPayload(recType, encPayload)
            if (recType == RECORD_ALERT) {
                return -1
            }
            if (recType == RECORD_APPLICATION_DATA && plain.isNotEmpty()) {
                val toCopy = minOf(len, plain.size)
                plain.copyInto(b, off, 0, toCopy)
                decryptedBuffer = plain
                decryptedOffset = toCopy
                return toCopy
            }
        }
    }

    private fun writeAppData(b: ByteArray, off: Int, len: Int) {
        if (!isHandshakeDone) startHandshake()
        val rawOut = underlyingSocket.getOutputStream()
        var pos = off
        val end = off + len
        while (pos < end) {
            val chunkLen = minOf(16384, end - pos)
            val chunk = b.copyOfRange(pos, pos + chunkLen)
            val enc = encryptRecordPayload(RECORD_APPLICATION_DATA, chunk)
            writeTlsRecord(rawOut, RECORD_APPLICATION_DATA, enc)
            pos += chunkLen
        }
        rawOut.flush()
    }

    override fun getInputStream(): InputStream = sslInputStream
    override fun getOutputStream(): OutputStream = sslOutputStream

    override fun getSession(): SSLSession {
        if (!isHandshakeDone && !isClosedSocket) {
            try {
                startHandshake()
            } catch (_: Exception) {
            }
        }
        return NativeSSLSession(
            host = peerHostName,
            port = getPort(),
            peerCerts = peerCerts,
            negotiatedProtocol = "TLSv1.2",
            negotiatedCipherSuite = negotiatedCipherSuite,
            sessionValid = isHandshakeDone && !isClosedSocket
        )
    }

    override fun isClosed(): Boolean = isClosedSocket || underlyingSocket.isClosed()

    override fun close() {
        if (!isClosedSocket) {
            isClosedSocket = true
            if (isHandshakeDone) {
                try {
                    val alertEnc = encryptRecordPayload(RECORD_ALERT, byteArrayOf(1, 0)) // warning, close_notify
                    writeTlsRecord(underlyingSocket.getOutputStream(), RECORD_ALERT, alertEnc)
                } catch (_: Exception) {
                }
            }
            if (autoClose) {
                underlyingSocket.close()
            }
        }
    }

    companion object {
        internal const val RECORD_CHANGE_CIPHER_SPEC = 20
        internal const val RECORD_ALERT = 21
        internal const val RECORD_HANDSHAKE = 22
        internal const val RECORD_APPLICATION_DATA = 23

        internal const val HANDSHAKE_CLIENT_HELLO = 1
        internal const val HANDSHAKE_SERVER_HELLO = 2
        internal const val HANDSHAKE_CERTIFICATE = 11
        internal const val HANDSHAKE_SERVER_HELLO_DONE = 14
        internal const val HANDSHAKE_CLIENT_KEY_EXCHANGE = 16
        internal const val HANDSHAKE_FINISHED = 20

        internal const val CIPHER_RSA_AES_128_GCM_SHA256 = 0x009C
        internal const val CIPHER_RSA_AES_128_CBC_SHA256 = 0x003C

        internal fun tls12Prf(secret: ByteArray, label: String, seed: ByteArray, outputLength: Int): ByteArray {
            val labelAndSeed = label.encodeToByteArray() + seed
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secret, "HmacSHA256")
            mac.init(keySpec)

            val result = ByteArray(outputLength)
            var a = mac.doFinal(labelAndSeed) // A(1)
            var offset = 0
            while (offset < outputLength) {
                mac.init(keySpec)
                mac.update(a)
                mac.update(labelAndSeed)
                val block = mac.doFinal()
                val toCopy = minOf(block.size, outputLength - offset)
                block.copyInto(result, offset, 0, toCopy)
                offset += toCopy
                if (offset < outputLength) {
                    mac.init(keySpec)
                    a = mac.doFinal(a)
                }
            }
            return result
        }

        internal fun buildRecordAad(seqNum: Long, contentType: Int, length: Int): ByteArray {
            val aad = ByteArray(13)
            for (i in 0 until 8) {
                aad[7 - i] = ((seqNum ushr (i * 8)) and 0xFF).toByte()
            }
            aad[8] = contentType.toByte()
            aad[9] = 0x03
            aad[10] = 0x03
            aad[11] = ((length ushr 8) and 0xFF).toByte()
            aad[12] = (length and 0xFF).toByte()
            return aad
        }

        internal fun wrapHandshakeMessage(type: Int, body: ByteArray): ByteArray {
            val out = ByteArray(4 + body.size)
            out[0] = type.toByte()
            out[1] = ((body.size ushr 16) and 0xFF).toByte()
            out[2] = ((body.size ushr 8) and 0xFF).toByte()
            out[3] = (body.size and 0xFF).toByte()
            body.copyInto(out, 4)
            return out
        }

        internal fun writeTlsRecord(out: OutputStream, type: Int, payload: ByteArray) {
            val header = byteArrayOf(
                type.toByte(),
                0x03,
                0x03,
                ((payload.size ushr 8) and 0xFF).toByte(),
                (payload.size and 0xFF).toByte()
            )
            out.write(header)
            out.write(payload)
            out.flush()
        }

        internal fun readTlsRecord(inp: InputStream): Pair<Int, ByteArray> {
            val header = readExact(inp, 5)
            val type = header[0].toInt() and 0xFF
            val len = ((header[3].toInt() and 0xFF) shl 8) or (header[4].toInt() and 0xFF)
            val payload = readExact(inp, len)
            return Pair(type, payload)
        }

        private fun readExact(inp: InputStream, len: Int): ByteArray {
            val buf = ByteArray(len)
            var off = 0
            while (off < len) {
                val n = inp.read(buf, off, len - off)
                if (n < 0) throw EOFException("Unexpected EOF in TLS stream")
                off += n
            }
            return buf
        }

        private fun buildClientHelloBody(clientRandom: ByteArray, host: String): ByteArray {
            val out = ByteArrayOutputStream()
            out.write(0x03)
            out.write(0x03) // TLS 1.2
            out.write(clientRandom)
            out.write(0x00) // session_id length = 0

            // Cipher suites (4 bytes: 0x009C, 0x003C)
            out.write(0x00)
            out.write(0x04)
            out.write(0x00)
            out.write(0x9C)
            out.write(0x00)
            out.write(0x3C)

            // Compression methods (1 byte: null)
            out.write(0x01)
            out.write(0x00)

            // Extensions (SNI + signature_algorithms)
            val extOut = ByteArrayOutputStream()
            if (host.isNotEmpty() && !host.all { it.isDigit() || it == '.' }) {
                val hostBytes = host.encodeToByteArray()
                val sniListLen = 3 + hostBytes.size
                val sniExtLen = 2 + sniListLen
                extOut.write(0x00); extOut.write(0x00) // server_name (0)
                extOut.write((sniExtLen ushr 8) and 0xFF); extOut.write(sniExtLen and 0xFF)
                extOut.write((sniListLen ushr 8) and 0xFF); extOut.write(sniListLen and 0xFF)
                extOut.write(0x00) // host_name (0)
                extOut.write((hostBytes.size ushr 8) and 0xFF); extOut.write(hostBytes.size and 0xFF)
                extOut.write(hostBytes)
            }
            // signature_algorithms (0x000D): SHA256/RSA (0x0401), SHA256/ECDSA (0x0403), SHA1/RSA (0x0201)
            val sigAlgs = byteArrayOf(0x00, 0x0D, 0x00, 0x08, 0x00, 0x06, 0x04, 0x01, 0x04, 0x03, 0x02, 0x01)
            extOut.write(sigAlgs)

            val extBytes = extOut.toByteArray()
            out.write((extBytes.size ushr 8) and 0xFF)
            out.write(extBytes.size and 0xFF)
            out.write(extBytes)
            return out.toByteArray()
        }

        private fun parseCertificateList(body: ByteArray): Array<Certificate> {
            if (body.size < 3) return emptyArray()
            val totalLen = ((body[0].toInt() and 0xFF) shl 16) or
                    ((body[1].toInt() and 0xFF) shl 8) or
                    (body[2].toInt() and 0xFF)
            val certs = mutableListOf<Certificate>()
            val cf = CertificateFactory.getInstance("X.509")
            var pos = 3
            val limit = minOf(body.size, 3 + totalLen)
            while (pos + 3 <= limit) {
                val certLen = ((body[pos].toInt() and 0xFF) shl 16) or
                        ((body[pos + 1].toInt() and 0xFF) shl 8) or
                        (body[pos + 2].toInt() and 0xFF)
                pos += 3
                if (pos + certLen > limit) break
                val certBytes = body.copyOfRange(pos, pos + certLen)
                certs.add(cf.generateCertificate(ByteArrayInputStream(certBytes)))
                pos += certLen
            }
            return certs.toTypedArray()
        }
    }
}
