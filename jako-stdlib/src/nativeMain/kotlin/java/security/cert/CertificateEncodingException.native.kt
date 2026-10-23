package java.security.cert

import java.security.*

actual open class CertificateEncodingException : CertificateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
