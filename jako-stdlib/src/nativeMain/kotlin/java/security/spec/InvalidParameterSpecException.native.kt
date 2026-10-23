package java.security.spec

import java.security.GeneralSecurityException

actual open class InvalidParameterSpecException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
