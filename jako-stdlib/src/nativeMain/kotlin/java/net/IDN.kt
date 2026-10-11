package java.net

actual class IDN {
    actual companion object {
        actual val ALLOW_UNASSIGNED: Int = 1
        actual val USE_STD3_ASCII_RULES: Int = 2

        actual fun toASCII(input: String, flag: Int): String {
            val labels = input.split('.')
            return labels.joinToString(".") { label ->
                if (isAllAscii(label)) label else "xn--" + punycodeEncode(label)
            }
        }

        actual fun toASCII(input: String): String = toASCII(input, 0)

        actual fun toUnicode(input: String, flag: Int): String {
            val labels = input.split('.')
            return labels.joinToString(".") { label ->
                if (label.startsWith("xn--", ignoreCase = true)) {
                    try {
                        punycodeDecode(label.substring(4))
                    } catch (_: Throwable) {
                        label
                    }
                } else {
                    label
                }
            }
        }

        actual fun toUnicode(input: String): String = toUnicode(input, 0)

        private fun isAllAscii(input: String): Boolean {
            for (c in input) {
                if (c.code > 127) return false
            }
            return true
        }

        private const val BASE = 36
        private const val TMIN = 1
        private const val TMAX = 26
        private const val SKEW = 38
        private const val DAMP = 700
        private const val INITIAL_BIAS = 72
        private const val INITIAL_N = 128
        private const val DELIMITER = '-'

        private fun adapt(delta: Int, numpoints: Int, firsttime: Boolean): Int {
            var d = if (firsttime) delta / DAMP else delta / 2
            d += d / numpoints
            var k = 0
            while (d > ((BASE - TMIN) * TMAX) / 2) {
                d /= BASE - TMIN
                k += BASE
            }
            return k + ((BASE - TMIN + 1) * d) / (d + SKEW)
        }

        private fun punycodeEncode(input: String): String {
            var n = INITIAL_N
            var delta = 0
            var bias = INITIAL_BIAS
            val output = StringBuilder()

            for (c in input) {
                if (c.code < 128) output.append(c)
            }

            val b = output.length
            var h = b
            if (b > 0) output.append(DELIMITER)

            val codePoints = input.map { it.code }
            val inputLength = codePoints.size

            while (h < inputLength) {
                var m = Int.MAX_VALUE
                for (cp in codePoints) {
                    if (cp in n until m) m = cp
                }

                delta += (m - n) * (h + 1)
                n = m

                for (cp in codePoints) {
                    if (cp < n) {
                        delta++
                    } else if (cp == n) {
                        var q = delta
                        var k = BASE
                        while (true) {
                            val t = when {
                                k <= bias -> TMIN
                                k >= bias + TMAX -> TMAX
                                else -> k - bias
                            }
                            if (q < t) break
                            val code = t + ((q - t) % (BASE - t))
                            output.append(encodeDigit(code))
                            q = (q - t) / (BASE - t)
                            k += BASE
                        }
                        output.append(encodeDigit(q))
                        bias = adapt(delta, h + 1, h == b)
                        delta = 0
                        h++
                    }
                }
                delta++
                n++
            }
            return output.toString()
        }

        private fun punycodeDecode(input: String): String {
            var n = INITIAL_N
            var i = 0
            var bias = INITIAL_BIAS
            val output = StringBuilder()

            val d = input.lastIndexOf(DELIMITER)
            var pos = 0
            if (d > 0) {
                for (j in 0 until d) {
                    output.append(input[j])
                }
                pos = d + 1
            }

            while (pos < input.length) {
                val oldi = i
                var w = 1
                var k = BASE
                while (true) {
                    if (pos >= input.length) throw IllegalArgumentException("Bad punycode input")
                    val digit = decodeDigit(input[pos++])
                    i += digit * w
                    val t = when {
                        k <= bias -> TMIN
                        k >= bias + TMAX -> TMAX
                        else -> k - bias
                    }
                    if (digit < t) break
                    w *= BASE - t
                    k += BASE
                }
                bias = adapt(i - oldi, output.length + 1, oldi == 0)
                n += i / (output.length + 1)
                i %= (output.length + 1)
                output.insert(i, n.toChar())
                i++
            }
            return output.toString()
        }

        private fun encodeDigit(d: Int): Char {
            return when {
                d < 26 -> ('a'.code + d).toChar()
                d < 36 -> ('0'.code + (d - 26)).toChar()
                else -> throw IllegalArgumentException("Invalid digit: $d")
            }
        }

        private fun decodeDigit(c: Char): Int {
            return when (c) {
                in 'a'..'z' -> c - 'a'
                in 'A'..'Z' -> c - 'A'
                in '0'..'9' -> c - '0' + 26
                else -> throw IllegalArgumentException("Invalid punycode char: $c")
            }
        }
    }
}
