@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.File
import java.io.InputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
expect open class JarFile : ZipFile {
    constructor(name: String)
    constructor(name: String, verify: Boolean)
    constructor(file: File)
    constructor(file: File, verify: Boolean)
    constructor(file: File, verify: Boolean, mode: Int)

    open fun getManifest(): Manifest?
    open val manifest: Manifest?
    open fun getJarEntry(name: String): JarEntry?
    override fun getEntry(name: String): ZipEntry?
    override fun entries(): Enumeration<JarEntry>
    override fun getInputStream(ze: ZipEntry): InputStream?

    companion object {
        val MANIFEST_NAME: String
    }
}
