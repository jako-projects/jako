@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
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
