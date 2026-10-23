@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect abstract class ExtendedSSLSession() : SSLSession {
    open fun getRequestedServerNames(): List<Any>?
    open val requestedServerNames: List<Any>?
}
