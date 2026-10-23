@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.IOException

expect open class ProtocolException : IOException {
    constructor()
    constructor(host: String?)
}
