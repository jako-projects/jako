package java.util.jar

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

actual open class JarInputStream : ZipInputStream {
    private var man: Manifest? = null
    private var firstPending: JarEntry? = null

    actual constructor(inStream: InputStream) : this(inStream, true)

    actual constructor(inStream: InputStream, verify: Boolean) : super(inStream) {
        var e = super.getNextEntry() as? JarEntry
        if (e != null && e.name.equals("META-INF/", ignoreCase = true)) {
            e = super.getNextEntry() as? JarEntry
        }
        if (e != null && e.name.equals(JarFile.MANIFEST_NAME, ignoreCase = true)) {
            man = Manifest()
            val bytes = readAllStreamBytes(this)
            closeEntry()
            man!!.read(ByteArrayInputStream(bytes))
        } else {
            firstPending = e
        }
    }

    actual open fun getManifest(): Manifest? = man
    actual open val manifest: Manifest? get() = getManifest()

    actual override fun getNextEntry(): ZipEntry? {
        val je = if (firstPending != null) {
            val tmp = firstPending
            firstPending = null
            tmp
        } else {
            super.getNextEntry() as? JarEntry
        }
        if (je != null && man != null) {
            je.entryAttrs = man!!.getAttributes(je.name)
        }
        return je
    }

    actual open fun getNextJarEntry(): JarEntry? = getNextEntry() as? JarEntry
    actual open val nextJarEntry: JarEntry? get() = getNextJarEntry()

    actual override fun createZipEntry(name: String): ZipEntry {
        val e = JarEntry(name)
        if (man != null) {
            e.entryAttrs = man!!.getAttributes(name)
        }
        return e
    }
}
