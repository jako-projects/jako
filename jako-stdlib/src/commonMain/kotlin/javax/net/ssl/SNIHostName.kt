@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect class SNIHostName {
    constructor(hostname: String)
    constructor(encoded: ByteArray)
    fun getAsciiName(): String
    val asciiName: String
    fun getEncoded(): ByteArray
}
