@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.util.zip.ZipException

expect open class JarException : ZipException {
    constructor()
    constructor(s: String?)
}
