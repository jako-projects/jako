@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

expect open class JarOutputStream : ZipOutputStream {
    constructor(out: OutputStream, man: Manifest)
    constructor(out: OutputStream)

    override fun putNextEntry(ze: ZipEntry)
}
