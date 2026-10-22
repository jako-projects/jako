@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.IOException

expect open class UnknownHostException : IOException {
    constructor()
    constructor(message: String?)
}
