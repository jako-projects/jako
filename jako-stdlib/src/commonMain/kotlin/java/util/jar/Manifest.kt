@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.io.InputStream
import java.io.OutputStream
import java.lang.Cloneable

/**
 * [Nullability Decision] getAttributes(name) returns Attributes? because a named section may not exist.
 */
expect open class Manifest : Cloneable {
    constructor()
    constructor(isStream: InputStream)
    constructor(man: Manifest)

    open fun getMainAttributes(): Attributes
    open val mainAttributes: Attributes
    open fun getEntries(): MutableMap<String, Attributes>
    open val entries: MutableMap<String, Attributes>
    open fun getAttributes(name: String): Attributes?
    open fun clear()
    open fun write(out: OutputStream)
    open fun read(isStream: InputStream)
    open fun clone(): Any
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
