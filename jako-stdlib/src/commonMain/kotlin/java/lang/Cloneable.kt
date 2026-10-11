@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect interface Cloneable

expect open class CloneNotSupportedException : Exception {
    constructor()
    constructor(message: String?)
}
