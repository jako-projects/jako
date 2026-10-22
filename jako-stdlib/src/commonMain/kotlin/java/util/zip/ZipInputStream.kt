@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.InputStream

expect open class ZipInputStream : InflaterInputStream {
    constructor(inStream: InputStream)

    // [Nullability Decision] getNextEntry returns null at end of archive (Java 8).
    open fun getNextEntry(): ZipEntry?
    open fun closeEntry()
    override fun available(): Int
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()
    protected open fun createZipEntry(name: String): ZipEntry
}
