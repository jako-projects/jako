@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect interface HostnameVerifier {
    fun verify(hostname: String, session: SSLSession): Boolean
}
