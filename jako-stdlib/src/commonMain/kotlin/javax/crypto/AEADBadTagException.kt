@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

expect open class AEADBadTagException : BadPaddingException {
    constructor()
    constructor(msg: String?)
}
