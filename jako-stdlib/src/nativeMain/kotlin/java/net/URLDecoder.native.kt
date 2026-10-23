package java.net

actual class URLDecoder private constructor() {
    actual companion object {
        actual fun decode(s: String, enc: String): String {
            val bytes = ByteArray(s.length)
            var bIdx = 0
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '+') {
                    bytes[bIdx++] = ' '.code.toByte()
                    i++
                } else if (c == '%' && i + 2 < s.length) {
                    val v1 = s[i + 1].digitToIntOrNull(16)
                    val v2 = s[i + 2].digitToIntOrNull(16)
                    if (v1 != null && v2 != null) {
                        bytes[bIdx++] = ((v1 shl 4) or v2).toByte()
                        i += 3
                    } else {
                        bytes[bIdx++] = c.code.toByte()
                        i++
                    }
                } else {
                    bytes[bIdx++] = c.code.toByte()
                    i++
                }
            }
            return bytes.decodeToString(0, bIdx)
        }
    }
}
