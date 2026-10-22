@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class ConnectException : SocketException {
    constructor()
    constructor(msg: String?)
}
