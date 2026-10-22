package java.text

actual object Normalizer {
    actual enum class Form {
        NFD,
        NFC,
        NFKD,
        NFKC,
    }

    // Hangul constants (UAX #15)
    private const val S_BASE = 0xAC00
    private const val L_BASE = 0x1100
    private const val V_BASE = 0x1161
    private const val T_BASE = 0x11A7
    private const val L_COUNT = 19
    private const val V_COUNT = 21
    private const val T_COUNT = 28
    private const val N_COUNT = V_COUNT * T_COUNT // 588
    private const val S_COUNT = L_COUNT * N_COUNT // 11172

    // Canonical two-char decompositions: precomposed -> (base, combiningMark)
    private val CANONICAL_PAIRS: Map<Char, Pair<Char, Char>> = buildMap {
        // Latin-1 Supplement (U+00C0..U+00FF)
        put('\u00C0', 'A' to '\u0300'); put('\u00C1', 'A' to '\u0301'); put('\u00C2', 'A' to '\u0302')
        put('\u00C3', 'A' to '\u0303'); put('\u00C4', 'A' to '\u0308'); put('\u00C5', 'A' to '\u030A')
        put('\u00C7', 'C' to '\u0327')
        put('\u00C8', 'E' to '\u0300'); put('\u00C9', 'E' to '\u0301'); put('\u00CA', 'E' to '\u0302'); put('\u00CB', 'E' to '\u0308')
        put('\u00CC', 'I' to '\u0300'); put('\u00CD', 'I' to '\u0301'); put('\u00CE', 'I' to '\u0302'); put('\u00CF', 'I' to '\u0308')
        put('\u00D1', 'N' to '\u0303')
        put('\u00D2', 'O' to '\u0300'); put('\u00D3', 'O' to '\u0301'); put('\u00D4', 'O' to '\u0302')
        put('\u00D5', 'O' to '\u0303'); put('\u00D6', 'O' to '\u0308')
        put('\u00D9', 'U' to '\u0300'); put('\u00DA', 'U' to '\u0301'); put('\u00DB', 'U' to '\u0302'); put('\u00DC', 'U' to '\u0308')
        put('\u00DD', 'Y' to '\u0301')
        put('\u00E0', 'a' to '\u0300'); put('\u00E1', 'a' to '\u0301'); put('\u00E2', 'a' to '\u0302')
        put('\u00E3', 'a' to '\u0303'); put('\u00E4', 'a' to '\u0308'); put('\u00E5', 'a' to '\u030A')
        put('\u00E7', 'c' to '\u0327')
        put('\u00E8', 'e' to '\u0300'); put('\u00E9', 'e' to '\u0301'); put('\u00EA', 'e' to '\u0302'); put('\u00EB', 'e' to '\u0308')
        put('\u00EC', 'i' to '\u0300'); put('\u00ED', 'i' to '\u0301'); put('\u00EE', 'i' to '\u0302'); put('\u00EF', 'i' to '\u0308')
        put('\u00F1', 'n' to '\u0303')
        put('\u00F2', 'o' to '\u0300'); put('\u00F3', 'o' to '\u0301'); put('\u00F4', 'o' to '\u0302')
        put('\u00F5', 'o' to '\u0303'); put('\u00F6', 'o' to '\u0308')
        put('\u00F9', 'u' to '\u0300'); put('\u00FA', 'u' to '\u0301'); put('\u00FB', 'u' to '\u0302'); put('\u00FC', 'u' to '\u0308')
        put('\u00FD', 'y' to '\u0301'); put('\u00FF', 'y' to '\u0308')

        // Latin Extended-A & common European characters
        put('\u0100', 'A' to '\u0304'); put('\u0101', 'a' to '\u0304')
        put('\u0102', 'A' to '\u0306'); put('\u0103', 'a' to '\u0306')
        put('\u0104', 'A' to '\u0328'); put('\u0105', 'a' to '\u0328')
        put('\u0106', 'C' to '\u0301'); put('\u0107', 'c' to '\u0301')
        put('\u0108', 'C' to '\u0302'); put('\u0109', 'c' to '\u0302')
        put('\u010C', 'C' to '\u030C'); put('\u010D', 'c' to '\u030C')
        put('\u010E', 'D' to '\u030C'); put('\u010F', 'd' to '\u030C')
        put('\u0112', 'E' to '\u0304'); put('\u0113', 'e' to '\u0304')
        put('\u0116', 'E' to '\u0307'); put('\u0117', 'e' to '\u0307')
        put('\u0118', 'E' to '\u0328'); put('\u0119', 'e' to '\u0328')
        put('\u011A', 'E' to '\u030C'); put('\u011B', 'e' to '\u030C')
        put('\u011E', 'G' to '\u0306'); put('\u011F', 'g' to '\u0306')
        put('\u0122', 'G' to '\u0327'); put('\u0123', 'g' to '\u0327')
        put('\u012A', 'I' to '\u0304'); put('\u012B', 'i' to '\u0304')
        put('\u012E', 'I' to '\u0328'); put('\u012F', 'i' to '\u0328')
        put('\u0130', 'I' to '\u0307')
        put('\u0136', 'K' to '\u0327'); put('\u0137', 'k' to '\u0327')
        put('\u0139', 'L' to '\u0301'); put('\u013A', 'l' to '\u0301')
        put('\u013B', 'L' to '\u0327'); put('\u013C', 'l' to '\u0327')
        put('\u013D', 'L' to '\u030C'); put('\u013E', 'l' to '\u030C')
        put('\u0143', 'N' to '\u0301'); put('\u0144', 'n' to '\u0301')
        put('\u0145', 'N' to '\u0327'); put('\u0146', 'n' to '\u0327')
        put('\u0147', 'N' to '\u030C'); put('\u0148', 'n' to '\u030C')
        put('\u014C', 'O' to '\u0304'); put('\u014D', 'o' to '\u0304')
        put('\u0150', 'O' to '\u030B'); put('\u0151', 'o' to '\u030B')
        put('\u0154', 'R' to '\u0301'); put('\u0155', 'r' to '\u0301')
        put('\u0158', 'R' to '\u030C'); put('\u0159', 'r' to '\u030C')
        put('\u015A', 'S' to '\u0301'); put('\u015B', 's' to '\u0301')
        put('\u015E', 'S' to '\u0327'); put('\u015F', 's' to '\u0327')
        put('\u0160', 'S' to '\u030C'); put('\u0161', 's' to '\u030C')
        put('\u0162', 'T' to '\u0327'); put('\u0163', 't' to '\u0327')
        put('\u0164', 'T' to '\u030C'); put('\u0165', 't' to '\u030C')
        put('\u016A', 'U' to '\u0304'); put('\u016B', 'u' to '\u0304')
        put('\u016E', 'U' to '\u030A'); put('\u016F', 'u' to '\u030A')
        put('\u0170', 'U' to '\u030B'); put('\u0171', 'u' to '\u030B')
        put('\u0172', 'U' to '\u0328'); put('\u0173', 'u' to '\u0328')
        put('\u0178', 'Y' to '\u0308')
        put('\u0179', 'Z' to '\u0301'); put('\u017A', 'z' to '\u0301')
        put('\u017B', 'Z' to '\u0307'); put('\u017C', 'z' to '\u0307')
        put('\u017D', 'Z' to '\u030C'); put('\u017E', 'z' to '\u030C')
        // Latin Extended Additional (dot below, multi-mark precomposed, etc.)
        put('\u1E08', '\u00C7' to '\u0301'); put('\u1E09', '\u00E7' to '\u0301')
        put('\u1E0C', 'D' to '\u0323'); put('\u1E0D', 'd' to '\u0323')
        put('\u1E24', 'H' to '\u0323'); put('\u1E25', 'h' to '\u0323')
        put('\u1E36', 'L' to '\u0323'); put('\u1E37', 'l' to '\u0323')
        put('\u1E42', 'M' to '\u0323'); put('\u1E43', 'm' to '\u0323')
        put('\u1E46', 'N' to '\u0323'); put('\u1E47', 'n' to '\u0323')
        put('\u1E5A', 'R' to '\u0323'); put('\u1E5B', 'r' to '\u0323')
        put('\u1E62', 'S' to '\u0323'); put('\u1E63', 's' to '\u0323')
        put('\u1E6C', 'T' to '\u0323'); put('\u1E6D', 't' to '\u0323')
        put('\u1EA0', 'A' to '\u0323'); put('\u1EA1', 'a' to '\u0323')
        put('\u1EB8', 'E' to '\u0323'); put('\u1EB9', 'e' to '\u0323')
        put('\u1ECA', 'I' to '\u0323'); put('\u1ECB', 'i' to '\u0323')
        put('\u1ECC', 'O' to '\u0323'); put('\u1ECD', 'o' to '\u0323')
        put('\u1EE4', 'U' to '\u0323'); put('\u1EE5', 'u' to '\u0323')
    }

    // Reverse composition map: (base, combiningMark) -> precomposed
    private val CANONICAL_COMPOSE: Map<Pair<Char, Char>, Char> = buildMap {
        for ((composed, pair) in CANONICAL_PAIRS) {
            put(pair, composed)
        }
    }

    // Singleton canonical mappings (excluded from composition back to singleton)
    private val CANONICAL_SINGLETONS: Map<Char, Char> = mapOf(
        '\u212B' to '\u00C5', // Angstrom -> Å
        '\u2126' to '\u03A9', // Ohm -> Ω
        '\u1FEF' to '\u0060', // Greek Varia
    )

    // Compatibility decomposition mappings for NFKD / NFKC
    private val COMPAT_MAP: Map<Char, String> = mapOf(
        '\u00A0' to " ",
        '\u00AA' to "a",
        '\u00B2' to "2",
        '\u00B3' to "3",
        '\u00B5' to "\u03BC",
        '\u00B9' to "1",
        '\u00BA' to "o",
        '\u00BC' to "1\u20444",
        '\u00BD' to "1\u20442",
        '\u00BE' to "3\u20444",
        '\u0132' to "IJ",
        '\u0133' to "ij",
        '\u017F' to "s",
        '\u2070' to "0",
        '\u2074' to "4",
        '\u2075' to "5",
        '\u2076' to "6",
        '\u2077' to "7",
        '\u2078' to "8",
        '\u2079' to "9",
        '\u2080' to "0",
        '\u2081' to "1",
        '\u2082' to "2",
        '\u2083' to "3",
        '\u2084' to "4",
        '\u2085' to "5",
        '\u2086' to "6",
        '\u2087' to "7",
        '\u2088' to "8",
        '\u2089' to "9",
        '\u2122' to "TM",
        '\u2160' to "I",
        '\u2161' to "II",
        '\u2162' to "III",
        '\u2163' to "IV",
        '\u2164' to "V",
        '\u2165' to "VI",
        '\u2166' to "VII",
        '\u2167' to "VIII",
        '\u2168' to "IX",
        '\u2169' to "X",
        '\u3000' to " ",
        '\uFB00' to "ff",
        '\uFB01' to "fi",
        '\uFB02' to "fl",
        '\uFB03' to "ffi",
        '\uFB04' to "ffl",
    )

    private fun combiningClass(c: Char): Int = when (c) {
        '\u0334', '\u0335', '\u0336', '\u0337', '\u0338' -> 1
        '\u0327', '\u0328' -> 202 // Cedilla, Ogonek
        '\u031B' -> 216 // Horn
        in '\u0316'..'\u0319', in '\u031C'..'\u0320', in '\u0323'..'\u0326', in '\u0329'..'\u0333', in '\u0339'..'\u033C' -> 220 // Below marks
        in '\u0300'..'\u0314', '\u033D', '\u033E', '\u033F', '\u0340', '\u0341', '\u0342', '\u0343', '\u0344', '\u0346', in '\u0350'..'\u0357' -> 230 // Above marks
        '\u0315', '\u031A' -> 232
        '\u0345' -> 240
        else -> 0
    }

    private fun decomposeChar(c: Char, compat: Boolean, out: StringBuilder) {
        // 1. Hangul syllable decomposition
        val sIndex = c.code - S_BASE
        if (sIndex in 0 until S_COUNT) {
            val l = L_BASE + sIndex / N_COUNT
            val v = V_BASE + (sIndex % N_COUNT) / T_COUNT
            val t = T_BASE + sIndex % T_COUNT
            out.append(l.toChar())
            out.append(v.toChar())
            if (t != T_BASE) {
                out.append(t.toChar())
            }
            return
        }

        // 2. Singleton canonical replacement
        val singleton = CANONICAL_SINGLETONS[c]
        if (singleton != null) {
            decomposeChar(singleton, compat, out)
            return
        }

        // 3. Canonical pair decomposition
        val pair = CANONICAL_PAIRS[c]
        if (pair != null) {
            decomposeChar(pair.first, compat, out)
            decomposeChar(pair.second, compat, out)
            return
        }

        // 4. Compatibility decomposition (NFKD / NFKC)
        if (compat) {
            // Fullwidth ASCII U+FF01..U+FF5E -> U+0021..U+007E
            if (c.code in 0xFF01..0xFF5E) {
                decomposeChar((c.code - 0xFEE0).toChar(), compat = true, out)
                return
            }
            val compatStr = COMPAT_MAP[c]
            if (compatStr != null) {
                for (ch in compatStr) {
                    decomposeChar(ch, compat = true, out)
                }
                return
            }
        }

        out.append(c)
    }

    private fun canonicalOrder(chars: CharArray) {
        var i = 1
        while (i < chars.size) {
            val cc = combiningClass(chars[i])
            if (cc != 0) {
                var j = i
                while (j > 0) {
                    val prevCc = combiningClass(chars[j - 1])
                    if (prevCc == 0 || prevCc <= cc) break
                    val tmp = chars[j - 1]
                    chars[j - 1] = chars[j]
                    chars[j] = tmp
                    j--
                }
            }
            i++
        }
    }

    private fun compose(decomposed: CharArray): String {
        if (decomposed.isEmpty()) return ""
        val buf = ArrayList<Char>(decomposed.size)
        for (c in decomposed) buf.add(c)

        var starterIdx = 0
        var starterChar = buf[0]
        var lastCc = combiningClass(starterChar)
        if (lastCc != 0) lastCc = 256

        var i = 1
        while (i < buf.size) {
            val ch = buf[i]
            val cc = combiningClass(ch)

            // 1. Check Hangul L + V and LV + T composition
            val lIndex = starterChar.code - L_BASE
            if (lIndex in 0 until L_COUNT) {
                val vIndex = ch.code - V_BASE
                if (vIndex in 0 until V_COUNT) {
                    starterChar = (S_BASE + (lIndex * V_COUNT + vIndex) * T_COUNT).toChar()
                    buf[starterIdx] = starterChar
                    buf.removeAt(i)
                    continue
                }
            }
            val sIndex = starterChar.code - S_BASE
            if (sIndex in 0 until S_COUNT && (sIndex % T_COUNT) == 0) {
                val tIndex = ch.code - T_BASE
                if (tIndex in 1 until T_COUNT) {
                    starterChar = (starterChar.code + tIndex).toChar()
                    buf[starterIdx] = starterChar
                    buf.removeAt(i)
                    continue
                }
            }

            // 2. Check general canonical pair composition if not blocked
            val composed = if (lastCc < cc || lastCc == 0) CANONICAL_COMPOSE[starterChar to ch] else null
            if (composed != null) {
                starterChar = composed
                buf[starterIdx] = starterChar
                buf.removeAt(i)
                continue
            }

            if (cc == 0) {
                starterIdx = i
                starterChar = ch
            }
            lastCc = cc
            i++
        }

        val sb = StringBuilder(buf.size)
        for (c in buf) sb.append(c)
        return sb.toString()
    }

    actual fun normalize(src: CharSequence, form: Form): String {
        val input = src.toString()
        if (input.isEmpty()) return ""
        val compat = (form == Form.NFKD || form == Form.NFKC)
        val decomposedBuilder = StringBuilder(input.length + 8)
        for (i in input.indices) {
            decomposeChar(input[i], compat, decomposedBuilder)
        }
        val chars = CharArray(decomposedBuilder.length) { decomposedBuilder[it] }
        canonicalOrder(chars)
        return when (form) {
            Form.NFD, Form.NFKD -> chars.concatToString()
            Form.NFC, Form.NFKC -> compose(chars)
        }
    }

    actual fun isNormalized(src: CharSequence, form: Form): Boolean {
        return normalize(src, form) == src.toString()
    }
}
