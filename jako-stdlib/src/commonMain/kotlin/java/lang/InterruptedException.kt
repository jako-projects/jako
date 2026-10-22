@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class InterruptedException : Exception {
    constructor()
    constructor(message: String?)
}
