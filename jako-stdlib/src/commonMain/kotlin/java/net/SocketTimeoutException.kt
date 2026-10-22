@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.InterruptedIOException

expect open class SocketTimeoutException : InterruptedIOException {
    constructor()
    constructor(msg: String?)
}
