@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect class URLEncoder {
    companion object {
        fun encode(s: String, enc: String): String
    }
}
