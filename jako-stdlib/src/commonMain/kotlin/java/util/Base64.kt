@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.InputStream
import java.io.OutputStream

expect class Base64 {
    class Encoder {
        fun encode(src: ByteArray): ByteArray
        fun encode(src: ByteArray, dst: ByteArray): Int
        fun encodeToString(src: ByteArray): String
        fun wrap(src: OutputStream): OutputStream
        fun encode(src: OutputStream): OutputStream
    }

    class Decoder {
        fun decode(src: ByteArray): ByteArray
        fun decode(src: String): ByteArray
        fun decode(src: ByteArray, dst: ByteArray): Int
        fun wrap(isStream: InputStream): InputStream
        fun decode(isStream: InputStream): InputStream
    }

    companion object {
        fun getEncoder(): Encoder
        fun getUrlEncoder(): Encoder
        fun getMimeEncoder(): Encoder
        fun getDecoder(): Decoder
        fun getUrlDecoder(): Decoder
        fun getMimeDecoder(): Decoder
    }
}
