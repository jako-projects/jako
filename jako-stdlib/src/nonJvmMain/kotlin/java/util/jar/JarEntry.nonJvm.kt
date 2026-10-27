package java.util.jar

import java.util.zip.ZipEntry

actual open class JarEntry : ZipEntry {
    internal var entryAttrs: Attributes? = null

    actual constructor(name: String) : super(name)

    actual constructor(ze: ZipEntry) : super(ze)

    actual constructor(je: JarEntry) : super(je) {
        this.entryAttrs = je.entryAttrs?.let { Attributes(it) }
    }

    actual open fun getAttributes(): Attributes? = entryAttrs
    actual open val attributes: Attributes? get() = getAttributes()
}
