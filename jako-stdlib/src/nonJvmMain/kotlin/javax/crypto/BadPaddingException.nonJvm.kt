package javax.crypto

import java.security.*
import java.security.spec.*

actual open class BadPaddingException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
