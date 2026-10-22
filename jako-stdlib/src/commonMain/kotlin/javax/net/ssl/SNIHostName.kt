@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect class SNIHostName(asciiName: String) {
    fun getAsciiName(): String
    val asciiName: String
}
