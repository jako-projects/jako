@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
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
