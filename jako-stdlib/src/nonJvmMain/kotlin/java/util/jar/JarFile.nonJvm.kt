package java.util.jar

import java.io.File
import java.io.InputStream
import java.util.Enumeration
import java.util.NoSuchElementException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

actual open class JarFile : ZipFile {
    private var parsedManifest: Manifest? = null
    private var manifestLoaded = false

    actual constructor(name: String) : super(File(name), OPEN_READ)
    actual constructor(name: String, verify: Boolean) : super(File(name), OPEN_READ)
    actual constructor(file: File) : super(file, OPEN_READ)
    actual constructor(file: File, verify: Boolean) : super(file, OPEN_READ)
    actual constructor(file: File, verify: Boolean, mode: Int) : super(file, mode)

    actual open fun getManifest(): Manifest? {
        if (!manifestLoaded) {
            manifestLoaded = true
            val manEntry = super.getEntry(MANIFEST_NAME)
                ?: findCaseInsensitiveManifestEntry()
            if (manEntry != null) {
                val ins = super.getInputStream(manEntry)
                if (ins != null) {
                    try {
                        parsedManifest = Manifest(ins)
                    } finally {
                        ins.close()
                    }
                }
            }
        }
        return parsedManifest
    }

    private fun findCaseInsensitiveManifestEntry(): ZipEntry? {
        val e = super.entries()
        while (e.hasMoreElements()) {
            val ze = e.nextElement()
            if (ze.name.equals(MANIFEST_NAME, ignoreCase = true)) {
                return ze
            }
        }
        return null
    }

    actual open val manifest: Manifest? get() = getManifest()

    actual open fun getJarEntry(name: String): JarEntry? = getEntry(name) as? JarEntry

    actual override fun getEntry(name: String): ZipEntry? {
        val ze = super.getEntry(name) ?: return null
        return toJarEntry(ze)
    }

    actual override fun entries(): Enumeration<JarEntry> {
        val base = super.entries()
        return object : Enumeration<JarEntry> {
            override fun hasMoreElements(): Boolean = base.hasMoreElements()
            override fun nextElement(): JarEntry {
                if (!base.hasMoreElements()) throw NoSuchElementException()
                return toJarEntry(base.nextElement())
            }
        }
    }

    actual override fun getInputStream(ze: ZipEntry): InputStream? = super.getInputStream(ze)

    private fun toJarEntry(ze: ZipEntry): JarEntry {
        val je = JarEntry(ze)
        val man = getManifest()
        if (man != null) {
            je.entryAttrs = man.getAttributes(ze.name)
        }
        return je
    }

    actual companion object {
        actual const val MANIFEST_NAME: String = "META-INF/MANIFEST.MF"
    }
}
