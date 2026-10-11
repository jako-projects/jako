package java.net

actual class URLEncoder private constructor() {
    actual companion object {
        actual fun encode(s: String, enc: String): String {
            val bytes = s.encodeToByteArray()
            val sb = StringBuilder()
            for (b in bytes) {
                val c = b.toInt() and 0xFF
                when {
                    c == ' '.code -> sb.append('+')
                    isUnreserved(c) -> sb.append(c.toChar())
                    else -> {
                        sb.append('%')
                        sb.append(digits[(c ushr 4) and 0x0F])
                        sb.append(digits[c and 0x0F])
                    }
                }
            }
            return sb.toString()
        }

        private fun isUnreserved(c: Int): Boolean {
            return (c in 'a'.code..'z'.code) ||
                    (c in 'A'.code..'Z'.code) ||
                    (c in '0'.code..'9'.code) ||
                    c == '.'.code || c == '-'.code || c == '*'.code || c == '_'.code
        }

        private val digits = "0123456789ABCDEF".toCharArray()
    }
}
