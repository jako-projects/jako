@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

expect open class SSLPeerUnverifiedException : SSLException {
    constructor(reason: String?)
}
