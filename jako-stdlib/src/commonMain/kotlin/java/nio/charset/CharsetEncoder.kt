@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

import java.nio.ByteBuffer
import java.nio.CharBuffer

expect abstract class CharsetEncoder {
    /** [Nullability Decision] Java CharsetEncoder(Charset,float,float) cs is non-null. */
    protected constructor(cs: Charset, averageBytesPerChar: Float, maxBytesPerChar: Float)
    /** [Nullability Decision] Java CharsetEncoder(..., byte[]) replacement is non-null. */
    protected constructor(cs: Charset, averageBytesPerChar: Float, maxBytesPerChar: Float, replacement: ByteArray)
    fun charset(): Charset
    /** [Nullability Decision] Java CharsetEncoder.replacement() is a non-null byte[]. */
    fun replacement(): ByteArray
    /** [Nullability Decision] Java CharsetEncoder.replaceWith(byte[]) newReplacement is non-null. */
    fun replaceWith(newReplacement: ByteArray): CharsetEncoder
    fun malformedInputAction(): CodingErrorAction
    fun unmappableCharacterAction(): CodingErrorAction
    /** [Nullability Decision] Java onMalformedInput(CodingErrorAction) newAction is non-null. */
    fun onMalformedInput(newAction: CodingErrorAction): CharsetEncoder
    /** [Nullability Decision] Java onUnmappableCharacter(CodingErrorAction) newAction is non-null. */
    fun onUnmappableCharacter(newAction: CodingErrorAction): CharsetEncoder
    fun averageBytesPerChar(): Float
    fun maxBytesPerChar(): Float
    fun reset(): CharsetEncoder
    /**
     * [Nullability Decision] Java encode(CharBuffer,ByteBuffer,boolean) in/out are non-null.
     */
    fun encode(`in`: CharBuffer, out: ByteBuffer, endOfInput: Boolean): CoderResult
    /** [Nullability Decision] Java encode(CharBuffer) in and the returned ByteBuffer are non-null. */
    fun encode(`in`: CharBuffer): ByteBuffer
    /** [Nullability Decision] Java flush(ByteBuffer) out is non-null. */
    fun flush(out: ByteBuffer): CoderResult

    val charset: Charset
    val replacement: ByteArray
    val malformedInputAction: CodingErrorAction
    val unmappableCharacterAction: CodingErrorAction
    val averageBytesPerChar: Float
    val maxBytesPerChar: Float
}
