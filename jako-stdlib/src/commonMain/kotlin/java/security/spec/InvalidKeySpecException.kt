@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

import java.security.GeneralSecurityException

expect open class InvalidKeySpecException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
