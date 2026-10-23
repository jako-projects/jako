@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FileInputStream : InputStream {
    constructor(file: File)
    constructor(name: String)
    constructor(fd: FileDescriptor)
    fun getFD(): FileDescriptor
    val fd: FileDescriptor
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()
}
