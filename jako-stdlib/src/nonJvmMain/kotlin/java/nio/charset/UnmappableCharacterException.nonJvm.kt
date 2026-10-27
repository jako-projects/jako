package java.nio.charset

actual open class UnmappableCharacterException actual constructor(
    inputLength: Int
) : CharacterCodingException() {
    private val length: Int = inputLength
    actual fun getInputLength(): Int = length
    actual val inputLength: Int get() = length
    override val message: String? get() = "Input length = $length"
}
