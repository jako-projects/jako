@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterOutputStream
import java.io.OutputStream

expect open class DeflaterOutputStream : FilterOutputStream {
    constructor(outStream: OutputStream)
    constructor(outStream: OutputStream, def: Deflater)
    constructor(outStream: OutputStream, def: Deflater, size: Int)
    constructor(outStream: OutputStream, def: Deflater, size: Int, syncFlush: Boolean)
    constructor(outStream: OutputStream, def: Deflater, syncFlush: Boolean)
    constructor(outStream: OutputStream, syncFlush: Boolean)

    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun finish()
    override fun close()
    override fun flush()
}
