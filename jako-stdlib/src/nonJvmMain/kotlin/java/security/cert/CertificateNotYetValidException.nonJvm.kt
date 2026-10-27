package java.security.cert

import java.security.*

actual open class CertificateNotYetValidException : CertificateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
