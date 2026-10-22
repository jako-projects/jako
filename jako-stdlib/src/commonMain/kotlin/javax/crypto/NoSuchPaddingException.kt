@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.security.GeneralSecurityException

expect open class NoSuchPaddingException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}
