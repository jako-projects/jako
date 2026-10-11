@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.CharBuffer

expect open class CharacterCodingException : IOException {
    constructor()
}

expect open class MalformedInputException : CharacterCodingException {
    /** [Nullability Decision] Java MalformedInputException(int) inputLength is a primitive. */
    constructor(inputLength: Int)
    fun getInputLength(): Int
    val inputLength: Int
}

expect open class UnmappableCharacterException : CharacterCodingException {
    /** [Nullability Decision] Java UnmappableCharacterException(int) inputLength is a primitive. */
    constructor(inputLength: Int)
    fun getInputLength(): Int
    val inputLength: Int
}

expect open class IllegalCharsetNameException : IllegalArgumentException {
    /** [Nullability Decision] Java IllegalCharsetNameException(String) charsetName is non-null. */
    constructor(charsetName: String)
    fun getCharsetName(): String
    val charsetName: String
}

expect open class UnsupportedCharsetException : IllegalArgumentException {
    /** [Nullability Decision] Java UnsupportedCharsetException(String) charsetName is non-null. */
    constructor(charsetName: String)
    fun getCharsetName(): String
    val charsetName: String
}

expect class CodingErrorAction {
    override fun toString(): String

    companion object {
        val IGNORE: CodingErrorAction
        val REPLACE: CodingErrorAction
        val REPORT: CodingErrorAction
    }
}

expect open class CoderResult {
    fun isUnderflow(): Boolean
    fun isOverflow(): Boolean
    fun isError(): Boolean
    fun isMalformed(): Boolean
    fun isUnmappable(): Boolean
    fun length(): Int
    fun throwException()

    val isUnderflow: Boolean
    val isOverflow: Boolean
    val isError: Boolean
    val isMalformed: Boolean
    val isUnmappable: Boolean

    companion object {
        val UNDERFLOW: CoderResult
        val OVERFLOW: CoderResult
        fun malformedForLength(length: Int): CoderResult
        fun unmappableForLength(length: Int): CoderResult
    }
}

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
