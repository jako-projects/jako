package android.text

import java.util.regex.Pattern

actual class TextUtils private constructor() {
    actual companion object {
        actual fun isEmpty(str: CharSequence?): Boolean {
            return str == null || str.isEmpty()
        }

        actual fun equals(a: CharSequence?, b: CharSequence?): Boolean {
            if (a === b) return true
            if (a != null && b != null && a.length == b.length) {
                if (a is String && b is String) {
                    return a == b
                }
                for (i in 0 until a.length) {
                    if (a[i] != b[i]) return false
                }
                return true
            }
            return false
        }

        actual fun isDigitsOnly(str: CharSequence): Boolean {
            val len = str.length
            var i = 0
            while (i < len) {
                val cp = Character.codePointAt(str, i)
                if (!Character.isDigit(cp)) {
                    return false
                }
                i += Character.charCount(cp)
            }
            return true
        }

        actual fun getTrimmedLength(s: CharSequence): Int {
            val len = s.length
            var start = 0
            while (start < len && s[start] <= ' ') {
                start++
            }
            var end = len
            while (end > start && s[end - 1] <= ' ') {
                end--
            }
            return end - start
        }

        actual fun join(delimiter: CharSequence, tokens: Array<*>): String {
            val sb = StringBuilder()
            var firstTime = true
            for (token in tokens) {
                if (firstTime) {
                    firstTime = false
                } else {
                    sb.append(delimiter)
                }
                sb.append(token)
            }
            return sb.toString()
        }

        actual fun join(delimiter: CharSequence, tokens: Iterable<*>): String {
            val sb = StringBuilder()
            val it = tokens.iterator()
            if (it.hasNext()) {
                sb.append(it.next())
                while (it.hasNext()) {
                    sb.append(delimiter)
                    sb.append(it.next())
                }
            }
            return sb.toString()
        }

        actual fun split(text: String, expression: String): Array<String> {
            if (text.isEmpty()) {
                return emptyArray()
            }
            return Pattern.compile(expression).split(text, -1)
        }

        actual fun split(text: String, pattern: Pattern): Array<String> {
            if (text.isEmpty()) {
                return emptyArray()
            }
            return pattern.split(text, -1)
        }

        actual fun htmlEncode(s: String): String {
            val sb = StringBuilder()
            for (i in 0 until s.length) {
                when (val c = s[i]) {
                    '<' -> sb.append("&lt;")
                    '>' -> sb.append("&gt;")
                    '&' -> sb.append("&amp;")
                    '\'' -> sb.append("&#39;")
                    '"' -> sb.append("&quot;")
                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }

        actual fun indexOf(s: CharSequence, ch: Char): Int = indexOf(s, ch, 0)

        actual fun indexOf(s: CharSequence, ch: Char, start: Int): Int = indexOf(s, ch, start, s.length)

        actual fun indexOf(s: CharSequence, ch: Char, start: Int, end: Int): Int {
            if (s is String) {
                val res = s.indexOf(ch, start)
                return if (res in start until end) res else -1
            }
            val actualStart = maxOf(0, start)
            val actualEnd = minOf(s.length, end)
            for (i in actualStart until actualEnd) {
                if (s[i] == ch) return i
            }
            return -1
        }

        actual fun indexOf(s: CharSequence, needle: CharSequence): Int = indexOf(s, needle, 0, s.length)

        actual fun indexOf(s: CharSequence, needle: CharSequence, start: Int): Int = indexOf(s, needle, start, s.length)

        actual fun indexOf(s: CharSequence, needle: CharSequence, start: Int, end: Int): Int {
            val nlen = needle.length
            if (nlen == 0) return start
            val ch = needle[0]
            var c = start
            while (true) {
                c = indexOf(s, ch, c)
                if (c > end - nlen) break
                if (c < 0) return -1
                if (regionMatches(s, c, needle, 0, nlen)) {
                    return c
                }
                c++
            }
            return -1
        }

        actual fun lastIndexOf(s: CharSequence, ch: Char): Int = lastIndexOf(s, ch, s.length - 1)

        actual fun lastIndexOf(s: CharSequence, ch: Char, last: Int): Int = lastIndexOf(s, ch, 0, last)

        actual fun lastIndexOf(s: CharSequence, ch: Char, start: Int, last: Int): Int {
            if (last < 0) return -1
            val actualLast = minOf(last, s.length - 1)
            for (i in actualLast downTo maxOf(0, start)) {
                if (s[i] == ch) return i
            }
            return -1
        }

        actual fun substring(source: CharSequence, start: Int, end: Int): String {
            if (source is String) {
                return source.substring(start, end)
            }
            if (source is StringBuilder) {
                return source.substring(start, end)
            }
            return source.subSequence(start, end).toString()
        }

        actual fun regionMatches(
            one: CharSequence,
            toffset: Int,
            two: CharSequence,
            ooffset: Int,
            len: Int
        ): Boolean {
            if (toffset < 0 || ooffset < 0 || toffset + len > one.length || ooffset + len > two.length) {
                return false
            }
            for (i in 0 until len) {
                if (one[toffset + i] != two[ooffset + i]) return false
            }
            return true
        }
    }
}
