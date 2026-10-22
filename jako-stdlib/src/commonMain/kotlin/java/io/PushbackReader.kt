@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PushbackReader : FilterReader {
    constructor(inReader: Reader, size: Int)
    constructor(inReader: Reader)
    open fun unread(c: Int)
    open fun unread(cbuf: CharArray, off: Int, len: Int)
    open fun unread(cbuf: CharArray)
}
