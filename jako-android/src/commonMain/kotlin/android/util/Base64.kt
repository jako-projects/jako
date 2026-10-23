@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Utilities for encoding and decoding the Base64 representation of binary data.
 */
expect class Base64 {
    companion object {
        val DEFAULT: Int
        val NO_PADDING: Int
        val NO_WRAP: Int
        val CRLF: Int
        val URL_SAFE: Int
        val NO_CLOSE: Int

        fun decode(str: String, flags: Int): ByteArray
        fun decode(input: ByteArray, flags: Int): ByteArray
        fun decode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray
        fun encodeToString(input: ByteArray, flags: Int): String
        fun encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String
        fun encode(input: ByteArray, flags: Int): ByteArray
        fun encode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray
    }
}
