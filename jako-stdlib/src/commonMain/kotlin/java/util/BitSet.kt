@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class BitSet : Cloneable, Serializable {
    constructor()
    constructor(nbits: Int)

    fun get(bitIndex: Int): Boolean
    fun get(fromIndex: Int, toIndex: Int): BitSet
    fun set(bitIndex: Int)
    fun set(bitIndex: Int, value: Boolean)
    fun set(fromIndex: Int, toIndex: Int)
    fun set(fromIndex: Int, toIndex: Int, value: Boolean)
    fun clear(bitIndex: Int)
    fun clear(fromIndex: Int, toIndex: Int)
    fun clear()
    fun flip(bitIndex: Int)
    fun flip(fromIndex: Int, toIndex: Int)
    fun and(set: BitSet)
    fun or(set: BitSet)
    fun xor(set: BitSet)
    fun andNot(set: BitSet)
    fun intersects(set: BitSet): Boolean
    fun cardinality(): Int
    fun isEmpty(): Boolean
    fun length(): Int
    fun size(): Int
    fun toByteArray(): ByteArray
    fun toLongArray(): LongArray
    fun nextSetBit(fromIndex: Int): Int
    fun nextClearBit(fromIndex: Int): Int
    fun previousSetBit(fromIndex: Int): Int
    fun previousClearBit(fromIndex: Int): Int

    val isEmpty: Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun valueOf(bytes: ByteArray): BitSet
        fun valueOf(longs: LongArray): BitSet
    }
}
