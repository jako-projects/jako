@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.OutputStream

expect open class ZipOutputStream : DeflaterOutputStream {
    constructor(outStream: OutputStream)

    // [Nullability Decision] setComment accepts null to clear the archive comment (Java 8).
    open fun setComment(comment: String?)
    open fun setMethod(method: Int)
    open fun setLevel(level: Int)
    open fun putNextEntry(e: ZipEntry)
    open fun closeEntry()
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun finish()
    override fun close()

    companion object {
        val STORED: Int
        val DEFLATED: Int
    }
}
