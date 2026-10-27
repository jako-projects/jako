package android.util

import java.nio.charset.StandardCharsets
import kotlin.math.abs

actual class Base64 private constructor() {
    actual companion object {
        actual val DEFAULT: Int = 0
        actual val NO_PADDING: Int = 1
        actual val NO_WRAP: Int = 2
        actual val CRLF: Int = 4
        actual val URL_SAFE: Int = 8
        actual val NO_CLOSE: Int = 16

        private const val STD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private const val URL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        actual fun encodeToString(input: ByteArray, flags: Int): String {
            return String(encode(input, 0, input.size, flags), StandardCharsets.US_ASCII)
        }

        actual fun encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String {
            return String(encode(input, offset, len, flags), StandardCharsets.US_ASCII)
        }

        actual fun encode(input: ByteArray, flags: Int): ByteArray {
            return encode(input, 0, input.size, flags)
        }

        actual fun encode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray {
            if (offset < 0 || len < 0 || offset + len > input.size) {
                throw IllegalArgumentException("Invalid offset/len")
            }
            val alphabet = if ((flags and URL_SAFE) != 0) URL_ALPHABET else STD_ALPHABET
            val doPadding = (flags and NO_PADDING) == 0
            val doNewline = (flags and NO_WRAP) == 0
            val doCr = (flags and CRLF) != 0

            val out = ArrayList<Byte>(len * 4 / 3 + 16)
            var p = offset
            val end = offset + len
            var lineGroups = 19 // 19 * 4 = 76 chars per line

            while (p + 3 <= end) {
                val v = ((input[p].toInt() and 0xFF) shl 16) or
                    ((input[p + 1].toInt() and 0xFF) shl 8) or
                    (input[p + 2].toInt() and 0xFF)
                out.add(alphabet[(v ushr 18) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 12) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                p += 3
                lineGroups--
                if (doNewline && lineGroups == 0 && p < end) {
                    if (doCr) out.add('\r'.code.toByte())
                    out.add('\n'.code.toByte())
                    lineGroups = 19
                }
            }

            val rem = end - p
            if (rem == 1) {
                val v = (input[p].toInt() and 0xFF) shl 4
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                if (doPadding) {
                    out.add('='.code.toByte())
                    out.add('='.code.toByte())
                }
            } else if (rem == 2) {
                val v = ((input[p].toInt() and 0xFF) shl 10) or
                    ((input[p + 1].toInt() and 0xFF) shl 2)
                out.add(alphabet[(v ushr 12) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                if (doPadding) {
                    out.add('='.code.toByte())
                }
            }

            if (doNewline && (len > 0 || true)) {
                // Note: In AOSP Base64, when do_newline is true, even empty or exact-line inputs get a trailing newline
                if (doCr) out.add('\r'.code.toByte())
                out.add('\n'.code.toByte())
            }

            return out.toByteArray()
        }

        actual fun decode(str: String, flags: Int): ByteArray {
            val bytes = str.toByteArray(StandardCharsets.US_ASCII)
            return decode(bytes, 0, bytes.size, flags)
        }

        actual fun decode(input: ByteArray, flags: Int): ByteArray {
            return decode(input, 0, input.size, flags)
        }

        actual fun decode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray {
            if (offset < 0 || len < 0 || offset + len > input.size) {
                throw IllegalArgumentException("Invalid offset/len")
            }
            val urlSafe = (flags and URL_SAFE) != 0
            val out = ArrayList<Byte>(len * 3 / 4)
            var state = 0
            var value = 0

            for (i in offset until (offset + len)) {
                val b = input[i].toInt() and 0xFF
                val d = decodeByte(b, urlSafe)
                if (d == SKIP) continue
                if (d == EQUALS) {
                    when (state) {
                        0, 1 -> throw IllegalArgumentException("bad base-64")
                        2 -> {
                            out.add((value shr 4).toByte())
                            state = 4
                        }
                        3 -> {
                            out.add((value shr 10).toByte())
                            out.add((value shr 2).toByte())
                            state = 5
                        }
                        4 -> state = 5
                        else -> throw IllegalArgumentException("bad base-64")
                    }
                    continue
                }
                if (d < 0) {
                    throw IllegalArgumentException("bad base-64")
                }
                when (state) {
                    0 -> {
                        value = d
                        state = 1
                    }
                    1 -> {
                        value = (value shl 6) or d
                        state = 2
                    }
                    2 -> {
                        value = (value shl 6) or d
                        state = 3
                    }
                    3 -> {
                        value = (value shl 6) or d
                        out.add((value shr 16).toByte())
                        out.add((value shr 8).toByte())
                        out.add(value.toByte())
                        state = 0
                    }
                    else -> throw IllegalArgumentException("bad base-64")
                }
            }

            when (state) {
                1, 4 -> throw IllegalArgumentException("bad base-64")
                2 -> out.add((value shr 4).toByte())
                3 -> {
                    out.add((value shr 10).toByte())
                    out.add((value shr 2).toByte())
                }
            }
            return out.toByteArray()
        }

        private const val SKIP = -1
        private const val EQUALS = -2
        private const val INVALID = -3

        private fun decodeByte(b: Int, urlSafe: Boolean): Int {
            return when (b.toChar()) {
                in 'A'..'Z' -> b - 'A'.code
                in 'a'..'z' -> b - 'a'.code + 26
                in '0'..'9' -> b - '0'.code + 52
                '+' -> if (urlSafe) INVALID else 62
                '/' -> if (urlSafe) INVALID else 63
                '-' -> if (urlSafe) 62 else INVALID
                '_' -> if (urlSafe) 63 else INVALID
                '=' -> EQUALS
                ' ', '\t', '\r', '\n' -> SKIP
                else -> INVALID
            }
        }
    }
}
