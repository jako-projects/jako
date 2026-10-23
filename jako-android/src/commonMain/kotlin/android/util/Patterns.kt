@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.util.regex.Pattern

/**
 * Commonly used regular expression patterns.
 */
expect class Patterns {
    companion object {
        val GOOD_IRI_CHAR: String
        val IP_ADDRESS: Pattern
        val DOMAIN_NAME: Pattern
        val WEB_URL: Pattern
        val EMAIL_ADDRESS: Pattern
        val PHONE: Pattern

        fun concatGroups(matcher: java.util.regex.Matcher): String
        fun digitsAndPlusOnly(matcher: java.util.regex.Matcher): String
    }
}
