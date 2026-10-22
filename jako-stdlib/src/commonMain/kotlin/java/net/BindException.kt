@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class BindException : SocketException {
    constructor()
    constructor(msg: String?)
}
