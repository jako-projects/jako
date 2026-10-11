package jako.test.util

import java.util.*

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.*

class Base64Test {

    @Test
    fun testStandardBase64() {
        val text = "Jako Base64 Multiplatform Encoding Test!"
        val bytes = text.encodeToByteArray()

        val encoder = Base64.getEncoder()
        val decoder = Base64.getDecoder()

        val encodedStr = encoder.encodeToString(bytes)
        val decodedBytes = decoder.decode(encodedStr)

        assertEquals(text, decodedBytes.decodeToString())
    }

    @Test
    fun testUrlSafeBase64() {
        val bytes = byteArrayOf(-1, -2, -3, 64, 65, 66)
        val urlEncoder = Base64.getUrlEncoder()
        val urlDecoder = Base64.getUrlDecoder()

        val encoded = urlEncoder.encodeToString(bytes)
        assertFalse(encoded.contains('+'), "URL safe base64 should not contain '+'")
        assertFalse(encoded.contains('/'), "URL safe base64 should not contain '/'")

        val decoded = urlDecoder.decode(encoded)
        assertContentEquals(bytes, decoded)
    }

    @Test
    fun testMimeBase64() {
        val longText = "A".repeat(200)
        val mimeEncoder = Base64.getMimeEncoder()
        val mimeDecoder = Base64.getMimeDecoder()

        val encoded = mimeEncoder.encodeToString(longText.encodeToByteArray())
        assertTrue(encoded.contains("\r\n"), "MIME base64 should contain \\r\\n line breaks")

        val decoded = mimeDecoder.decode(encoded)
        assertEquals(longText, decoded.decodeToString())
    }

    @Test
    fun testBufferEncodeAndDecode() {
        val src = "Hello Buffer".encodeToByteArray()
        val encoder = Base64.getEncoder()
        val decoder = Base64.getDecoder()

        val dstEnc = ByteArray(100)
        val encLen = encoder.encode(src, dstEnc)

        val dstDec = ByteArray(100)
        val decLen = decoder.decode(dstEnc.copyOf(encLen), dstDec)

        assertEquals("Hello Buffer", dstDec.copyOf(decLen).decodeToString())
    }

    @Test
    fun testStreamEncodeAndDecode() {
        val originalText = "Stream-based Base64 Test Data"
        val bosEnc = ByteArrayOutputStream()
        val encoderStream = Base64.getEncoder().wrap(bosEnc)

        encoderStream.write(originalText.encodeToByteArray())
        encoderStream.flush()
        encoderStream.close()

        val encodedBytes = bosEnc.toByteArray()

        val bis = ByteArrayInputStream(encodedBytes)
        val decoderStream = Base64.getDecoder().wrap(bis)
        
        val bosDec = ByteArrayOutputStream()
        val buf = ByteArray(128)
        var n: Int
        while (decoderStream.read(buf).also { n = it } != -1) {
            bosDec.write(buf, 0, n)
        }
        val decodedBytes = bosDec.toByteArray()

        assertEquals(originalText, decodedBytes.decodeToString())
    }

    @Test
    fun testEmptyData() {
        val encoder = Base64.getEncoder()
        val decoder = Base64.getDecoder()

        assertEquals("", encoder.encodeToString(ByteArray(0)))
        assertContentEquals(ByteArray(0), decoder.decode(""))
    }
}
