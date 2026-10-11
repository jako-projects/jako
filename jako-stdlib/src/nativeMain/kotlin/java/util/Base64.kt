package java.util

import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual class Base64 private constructor() {

    actual class Encoder internal constructor(
        private val isUrlSafe: Boolean,
        private val isMime: Boolean
    ) {
        private val table = if (isUrlSafe) URL_TABLE else STD_TABLE

        actual fun encode(src: ByteArray): ByteArray {
            return encodeToString(src).encodeToByteArray()
        }

        actual fun encode(src: ByteArray, dst: ByteArray): Int {
            val encoded = encode(src)
            if (dst.size < encoded.size) throw IllegalArgumentException("Output buffer too small")
            encoded.copyInto(dst)
            return encoded.size
        }

        actual fun encodeToString(src: ByteArray): String {
            if (src.isEmpty()) return ""
            val sb = StringBuilder()
            var i = 0
            val len = src.size
            var lineCount = 0

            while (i < len) {
                val b0 = src[i++].toInt() and 0xFF
                if (i < len) {
                    val b1 = src[i++].toInt() and 0xFF
                    if (i < len) {
                        val b2 = src[i++].toInt() and 0xFF
                        sb.append(table[b0 ushr 2])
                        sb.append(table[((b0 and 0x03) shl 4) or (b1 ushr 4)])
                        sb.append(table[((b1 and 0x0F) shl 2) or (b2 ushr 6)])
                        sb.append(table[b2 and 0x3F])
                    } else {
                        sb.append(table[b0 ushr 2])
                        sb.append(table[((b0 and 0x03) shl 4) or (b1 ushr 4)])
                        sb.append(table[(b1 and 0x0F) shl 2])
                        sb.append('=')
                    }
                } else {
                    sb.append(table[b0 ushr 2])
                    sb.append(table[(b0 and 0x03) shl 4])
                    sb.append("==")
                }

                if (isMime) {
                    lineCount += 4
                    if (lineCount >= 76 && i < len) {
                        sb.append("\r\n")
                        lineCount = 0
                    }
                }
            }
            return sb.toString()
        }

        actual fun wrap(src: OutputStream): OutputStream {
            return object : OutputStream() {
                override fun write(b: Int) {
                    write(byteArrayOf(b.toByte()), 0, 1)
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    val chunk = b.copyOfRange(off, off + len)
                    val bytes = encode(chunk)
                    src.write(bytes)
                }

                override fun flush() = src.flush()
                override fun close() = src.close()
            }
        }

        actual fun encode(src: OutputStream): OutputStream = wrap(src)
    }

    actual class Decoder internal constructor(
        private val isUrlSafe: Boolean,
        private val isMime: Boolean
    ) {
        actual fun decode(src: ByteArray): ByteArray {
            return decode(src.decodeToString())
        }

        actual fun decode(src: String): ByteArray {
            val clean = src.replace("\r", "").replace("\n", "").trimEnd('=')
            if (clean.isEmpty()) return ByteArray(0)

            val table = if (isUrlSafe) URL_MAP else STD_MAP
            val bytes = ByteArray(clean.length * 3 / 4)
            var bIdx = 0
            var i = 0
            while (i < clean.length) {
                val c0 = table[clean[i++].code]
                val c1 = if (i < clean.length) table[clean[i++].code] else 0
                val c2 = if (i < clean.length) table[clean[i++].code] else 0
                val c3 = if (i < clean.length) table[clean[i++].code] else 0

                val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
                if (bIdx < bytes.size) bytes[bIdx++] = ((triple ushr 16) and 0xFF).toByte()
                if (bIdx < bytes.size) bytes[bIdx++] = ((triple ushr 8) and 0xFF).toByte()
                if (bIdx < bytes.size) bytes[bIdx++] = (triple and 0xFF).toByte()
            }
            return bytes.copyOf(bIdx)
        }

        actual fun decode(src: ByteArray, dst: ByteArray): Int {
            val decoded = decode(src)
            if (dst.size < decoded.size) throw IllegalArgumentException("Output buffer too small")
            decoded.copyInto(dst)
            return decoded.size
        }

        actual fun wrap(isStream: InputStream): InputStream {
            val bytes = ByteArray(4096)
            val bos = ByteArrayOutputStream()
            var n: Int
            while (isStream.read(bytes).also { n = it } != -1) {
                bos.write(bytes, 0, n)
            }
            val decoded = decode(bos.toByteArray())
            return ByteArrayInputStream(decoded)
        }

        actual fun decode(isStream: InputStream): InputStream = wrap(isStream)
    }

    actual companion object {
        private val STD_ENCODER = Encoder(isUrlSafe = false, isMime = false)
        private val URL_ENCODER = Encoder(isUrlSafe = true, isMime = false)
        private val MIME_ENCODER = Encoder(isUrlSafe = false, isMime = true)

        private val STD_DECODER = Decoder(isUrlSafe = false, isMime = false)
        private val URL_DECODER = Decoder(isUrlSafe = true, isMime = false)
        private val MIME_DECODER = Decoder(isUrlSafe = false, isMime = true)

        actual fun getEncoder(): Encoder = STD_ENCODER
        actual fun getUrlEncoder(): Encoder = URL_ENCODER
        actual fun getMimeEncoder(): Encoder = MIME_ENCODER

        actual fun getDecoder(): Decoder = STD_DECODER
        actual fun getUrlDecoder(): Decoder = URL_DECODER
        actual fun getMimeDecoder(): Decoder = MIME_DECODER

        private const val STD_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private const val URL_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        private val STD_MAP = IntArray(256) { -1 }
        private val URL_MAP = IntArray(256) { -1 }

        init {
            for (i in STD_TABLE.indices) STD_MAP[STD_TABLE[i].code] = i
            for (i in URL_TABLE.indices) URL_MAP[URL_TABLE[i].code] = i
        }
    }
}
