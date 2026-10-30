@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

expect open class PrintWriter : Writer {
    constructor(out: OutputStream)
    constructor(out: Writer)
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    constructor(file: File)
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    constructor(fileName: String)
    fun print(s: String?)
    fun println(s: String?)
    fun println()
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}
