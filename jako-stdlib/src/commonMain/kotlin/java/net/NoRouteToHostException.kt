@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class NoRouteToHostException : SocketException {
    constructor()
    constructor(msg: String?)
}
