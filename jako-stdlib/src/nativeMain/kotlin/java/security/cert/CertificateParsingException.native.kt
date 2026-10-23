package java.security.cert

import java.security.*

actual open class CertificateParsingException : CertificateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
