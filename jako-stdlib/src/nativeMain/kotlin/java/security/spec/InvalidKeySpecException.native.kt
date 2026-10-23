package java.security.spec

import java.security.GeneralSecurityException

actual open class InvalidKeySpecException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
