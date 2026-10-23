@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FileOutputStream : OutputStream {
    constructor(file: File)
    constructor(name: String)
    constructor(name: String, append: Boolean)
    constructor(file: File, append: Boolean)
    constructor(fd: FileDescriptor)
    fun getFD(): FileDescriptor
    val fd: FileDescriptor
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}
