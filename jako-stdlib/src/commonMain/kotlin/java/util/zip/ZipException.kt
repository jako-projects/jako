@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.IOException

expect open class ZipException : IOException {
    constructor()
    constructor(s: String?)
}
