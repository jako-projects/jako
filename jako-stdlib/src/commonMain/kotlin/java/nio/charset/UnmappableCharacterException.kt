@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

expect open class UnmappableCharacterException : CharacterCodingException {
    /** [Nullability Decision] Java UnmappableCharacterException(int) inputLength is a primitive. */
    constructor(inputLength: Int)
    fun getInputLength(): Int
    val inputLength: Int
}
