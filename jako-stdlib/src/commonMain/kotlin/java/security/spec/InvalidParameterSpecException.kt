@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

import java.security.GeneralSecurityException

expect open class InvalidParameterSpecException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}
