package javax.crypto

import java.security.*
import java.security.spec.*

actual open class AEADBadTagException : BadPaddingException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
