@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect open class SSLProtocolException : SSLException {
    constructor(reason: String?)
}
