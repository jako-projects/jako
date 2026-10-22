@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class InvalidClassException : ObjectStreamException {
    var classname: String?
    constructor(reason: String?)
    constructor(cname: String?, reason: String?)
    override val message: String?
}
