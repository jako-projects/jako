package java.text

actual abstract class CollationKey protected actual constructor(
    source: String?
) : Comparable<CollationKey> {
    private val sourceStr: String = source ?: throw NullPointerException()

    actual open val sourceString: String get() = getSourceString()
    actual open fun getSourceString(): String = sourceStr

    actual abstract override fun compareTo(other: CollationKey): Int

    actual abstract fun toByteArray(): ByteArray
}
