package java.nio

actual open class ByteOrder private constructor(private val nameStr: String) {
    override fun toString(): String = nameStr

    actual companion object {
        actual val BIG_ENDIAN: ByteOrder = ByteOrder("BIG_ENDIAN")
        actual val LITTLE_ENDIAN: ByteOrder = ByteOrder("LITTLE_ENDIAN")
        actual fun nativeOrder(): ByteOrder = LITTLE_ENDIAN
    }
}
