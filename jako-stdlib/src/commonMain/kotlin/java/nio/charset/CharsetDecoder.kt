@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

import java.nio.ByteBuffer
import java.nio.CharBuffer

expect abstract class CharsetDecoder {
    /** [Nullability Decision] Java CharsetDecoder(Charset,float,float) cs is non-null. */
    protected constructor(cs: Charset, averageCharsPerByte: Float, maxCharsPerByte: Float)
    fun charset(): Charset
    /** [Nullability Decision] Java CharsetDecoder.replacement() is a non-null String. */
    fun replacement(): String
    /** [Nullability Decision] Java CharsetDecoder.replaceWith(String) newReplacement is non-null. */
    fun replaceWith(newReplacement: String): CharsetDecoder
    fun malformedInputAction(): CodingErrorAction
    fun unmappableCharacterAction(): CodingErrorAction
    /** [Nullability Decision] Java onMalformedInput(CodingErrorAction) newAction is non-null. */
    fun onMalformedInput(newAction: CodingErrorAction): CharsetDecoder
    /** [Nullability Decision] Java onUnmappableCharacter(CodingErrorAction) newAction is non-null. */
    fun onUnmappableCharacter(newAction: CodingErrorAction): CharsetDecoder
    fun averageCharsPerByte(): Float
    fun maxCharsPerByte(): Float
    fun reset(): CharsetDecoder
    /**
     * [Nullability Decision] Java decode(ByteBuffer,CharBuffer,boolean) in/out are non-null.
     */
    fun decode(`in`: ByteBuffer, out: CharBuffer, endOfInput: Boolean): CoderResult
    /** [Nullability Decision] Java decode(ByteBuffer) in and the returned CharBuffer are non-null. */
    fun decode(`in`: ByteBuffer): CharBuffer
    /** [Nullability Decision] Java flush(CharBuffer) out is non-null. */
    fun flush(out: CharBuffer): CoderResult

    val charset: Charset
    val replacement: String
    val malformedInputAction: CodingErrorAction
    val unmappableCharacterAction: CodingErrorAction
    val averageCharsPerByte: Float
    val maxCharsPerByte: Float
}
