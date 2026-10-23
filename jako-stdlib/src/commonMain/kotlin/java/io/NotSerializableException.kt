@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class NotSerializableException : ObjectStreamException {
    constructor(classname: String?)
    constructor()
}
