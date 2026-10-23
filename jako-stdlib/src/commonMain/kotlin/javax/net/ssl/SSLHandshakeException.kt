@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect open class SSLHandshakeException : SSLException {
    constructor(reason: String?)
}
