@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class EOFException : IOException {
    constructor()
    constructor(message: String?)
}
