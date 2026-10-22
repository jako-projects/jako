@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect class URLDecoder {
    companion object {
        fun decode(s: String, enc: String): String
    }
}
