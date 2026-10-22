package java.util.jar

import java.io.BufferedOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

actual open class JarOutputStream : ZipOutputStream {
    actual constructor(out: OutputStream, man: Manifest) : super(out) {
        val e = ZipEntry(JarFile.MANIFEST_NAME)
        putNextEntry(e)
        man.write(BufferedOutputStream(this))
        closeEntry()
    }

    actual constructor(out: OutputStream) : super(out)

    actual override fun putNextEntry(ze: ZipEntry) {
        super.putNextEntry(ze)
    }
}
