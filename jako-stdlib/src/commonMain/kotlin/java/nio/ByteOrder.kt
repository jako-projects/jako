@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

expect open class ByteOrder {
    companion object {
        val BIG_ENDIAN: ByteOrder
        val LITTLE_ENDIAN: ByteOrder
        fun nativeOrder(): ByteOrder
    }
}
