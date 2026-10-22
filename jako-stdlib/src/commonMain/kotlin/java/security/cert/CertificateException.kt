@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

import java.security.GeneralSecurityException

expect open class CertificateException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
