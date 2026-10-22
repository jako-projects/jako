@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

expect open class JarInputStream : ZipInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, verify: Boolean)

    open fun getManifest(): Manifest?
    open val manifest: Manifest?
    override fun getNextEntry(): ZipEntry?
    open fun getNextJarEntry(): JarEntry?
    open val nextJarEntry: JarEntry?
    override fun createZipEntry(name: String): ZipEntry
}
