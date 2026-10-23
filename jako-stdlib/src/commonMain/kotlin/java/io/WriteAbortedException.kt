@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class WriteAbortedException : ObjectStreamException {
    var detail: Exception?
    constructor(s: String?, ex: Exception?)
    override val message: String?
    override val cause: Throwable?
}
