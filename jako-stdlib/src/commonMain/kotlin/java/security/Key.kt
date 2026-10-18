@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.io.Serializable

expect interface Key : Serializable {
    fun getAlgorithm(): String
    fun getFormat(): String?
    fun getEncoded(): ByteArray?
    open val algorithm: String
    open val format: String?
    open val encoded: ByteArray
}
