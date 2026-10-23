@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable

expect class UUID : Serializable, Comparable<UUID> {
    constructor(mostSigBits: Long, leastSigBits: Long)
    fun getMostSignificantBits(): Long
    fun getLeastSignificantBits(): Long
    val mostSignificantBits: Long
    val leastSignificantBits: Long
    override fun compareTo(other: UUID): Int
    override fun toString(): String

    companion object {
        fun randomUUID(): UUID
        fun nameUUIDFromBytes(name: ByteArray): UUID
        fun fromString(name: String): UUID
    }
}
