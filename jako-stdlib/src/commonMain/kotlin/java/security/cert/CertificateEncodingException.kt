@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

expect open class CertificateEncodingException : CertificateException {
    constructor()
    constructor(message: String?)
}
