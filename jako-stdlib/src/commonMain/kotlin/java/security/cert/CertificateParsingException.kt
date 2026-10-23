@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

expect open class CertificateParsingException : CertificateException {
    constructor()
    constructor(message: String?)
}
