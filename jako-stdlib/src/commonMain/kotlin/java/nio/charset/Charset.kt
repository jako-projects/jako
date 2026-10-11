@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

import java.nio.ByteBuffer
import java.nio.CharBuffer

expect abstract class Charset protected constructor(
    nameString: String,
    aliases: Array<String>
) : Comparable<Charset> {
    fun name(): String
    fun displayName(): String
    fun isRegistered(): Boolean
    /** [Nullability Decision] Java Charset.newEncoder() returns a non-null encoder. */
    fun newEncoder(): CharsetEncoder
    /** [Nullability Decision] Java Charset.newDecoder() returns a non-null decoder. */
    fun newDecoder(): CharsetDecoder
    /** [Nullability Decision] Java Charset.encode(CharBuffer) cb and the result are non-null. */
    fun encode(cb: CharBuffer): ByteBuffer
    /** [Nullability Decision] Java Charset.encode(String) str and the result are non-null. */
    fun encode(str: String): ByteBuffer
    /** [Nullability Decision] Java Charset.decode(ByteBuffer) bb and the result are non-null. */
    fun decode(bb: ByteBuffer): CharBuffer
    override fun compareTo(other: Charset): Int
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        fun forName(charsetName: String): Charset
        fun isSupported(charsetName: String): Boolean
        fun defaultCharset(): Charset
    }
}


