@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.util.zip.ZipEntry

expect open class JarEntry : ZipEntry {
    constructor(name: String)
    constructor(ze: ZipEntry)
    constructor(je: JarEntry)

    open fun getAttributes(): Attributes?
    open val attributes: Attributes?
}
