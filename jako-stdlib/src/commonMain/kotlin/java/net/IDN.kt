@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect class IDN {
    companion object {
        val ALLOW_UNASSIGNED: Int
        val USE_STD3_ASCII_RULES: Int

        fun toASCII(input: String, flag: Int): String
        fun toASCII(input: String): String
        fun toUnicode(input: String, flag: Int): String
        fun toUnicode(input: String): String
    }
}
