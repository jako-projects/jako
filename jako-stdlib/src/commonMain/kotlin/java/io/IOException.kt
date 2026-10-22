@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class IOException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
