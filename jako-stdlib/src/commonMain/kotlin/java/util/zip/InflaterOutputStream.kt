@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterOutputStream
import java.io.OutputStream

expect open class InflaterOutputStream : FilterOutputStream {
    constructor(out: OutputStream)
    constructor(out: OutputStream, infl: Inflater)
    constructor(out: OutputStream, infl: Inflater, bufLen: Int)

    override fun close()
    override fun flush()
    open fun finish()
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
}
