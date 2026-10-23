package android.util

import java.util.regex.Pattern
import kotlin.math.abs

actual class Patterns private constructor() {
    actual companion object {
        actual val GOOD_IRI_CHAR: String = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF"

        actual val IP_ADDRESS: Pattern = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]" +
                "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]" +
                "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" +
                "|[1-9][0-9]|[0-9]))"
        )

        actual val DOMAIN_NAME: Pattern = Pattern.compile(
            "(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}|" +
                IP_ADDRESS.pattern() + ")"
        )

        actual val WEB_URL: Pattern = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)" +
                "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_" +
                "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?" +
                "(?:" + DOMAIN_NAME.pattern() + ")" +
                "(?:\\:\\d{1,5})?)" +
                "(\\/(?:(?:[a-zA-Z0-9;\\/\\?\\:\\@\\&\\=\\#\\~" +
                "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?" +
                "(?:\\b|$)"
        )

        actual val EMAIL_ADDRESS: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
        )

        actual val PHONE: Pattern = Pattern.compile(
            "(\\+[0-9]+[\\- \\.]*)?" +
                "(\\([0-9]+\\)[\\- \\.]*)?" +
                "([0-9][0-9\\- \\.]+[0-9])"
        )

        actual fun concatGroups(matcher: java.util.regex.Matcher): String {
            val b = StringBuilder()
            val numGroups = matcher.groupCount()
            for (i in 1..numGroups) {
                val s = matcher.group(i)
                if (s != null) {
                    b.append(s)
                }
            }
            return b.toString()
        }

        actual fun digitsAndPlusOnly(matcher: java.util.regex.Matcher): String {
            val buffer = StringBuilder()
            val matchingRegion = matcher.group() ?: return ""
            for (i in 0 until matchingRegion.length) {
                val character = matchingRegion[i]
                if (character == '+' || character in '0'..'9') {
                    buffer.append(character)
                }
            }
            return buffer.toString()
        }
    }
}
