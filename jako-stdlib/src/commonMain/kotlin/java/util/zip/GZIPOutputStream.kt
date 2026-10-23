@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.OutputStream

expect open class GZIPOutputStream : DeflaterOutputStream {
    constructor(outStream: OutputStream)
    constructor(outStream: OutputStream, size: Int)
    constructor(outStream: OutputStream, size: Int, syncFlush: Boolean)
    constructor(outStream: OutputStream, syncFlush: Boolean)

    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun finish()
}
