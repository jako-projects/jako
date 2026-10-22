@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PushbackInputStream : FilterInputStream {
    constructor(inStream: InputStream, size: Int)
    constructor(inStream: InputStream)
    open fun unread(b: Int)
    open fun unread(b: ByteArray, off: Int, len: Int)
    open fun unread(b: ByteArray)
}
