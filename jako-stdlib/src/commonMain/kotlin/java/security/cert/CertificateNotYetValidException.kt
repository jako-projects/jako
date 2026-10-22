@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

expect open class CertificateNotYetValidException : CertificateException {
    constructor()
    constructor(message: String?)
}
