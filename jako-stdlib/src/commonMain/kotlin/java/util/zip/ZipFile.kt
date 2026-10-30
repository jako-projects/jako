@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.Enumeration

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
expect open class ZipFile : Closeable {
    constructor(name: String)
    constructor(file: File)
    constructor(file: File, mode: Int)

    // [Nullability Decision] getComment is null when the archive has no comment (Java 8).
    open fun getComment(): String?
    open val comment: String?
    // [Nullability Decision] getEntry is null when the name is absent (Java 8).
    open fun getEntry(name: String): ZipEntry?
    // [Nullability Decision] getInputStream is null when the entry name is absent (Java 8).
    open fun getInputStream(entry: ZipEntry): InputStream?
    open fun getName(): String
    open val name: String
    open fun entries(): Enumeration<out ZipEntry>
    open fun size(): Int
    override fun close()

    companion object {
        val OPEN_READ: Int
        val OPEN_DELETE: Int
    }
}
