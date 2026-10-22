@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class NotActiveException : ObjectStreamException {
    constructor(reason: String?)
    constructor()
}
