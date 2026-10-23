@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect abstract class CollationKey : Comparable<CollationKey> {
    protected constructor(source: String?)
    open fun getSourceString(): String
    abstract override fun compareTo(other: CollationKey): Int
    abstract fun toByteArray(): ByteArray
}
