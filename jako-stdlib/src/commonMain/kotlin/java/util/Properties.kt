@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Reader
import java.io.Serializable
import java.io.Writer

expect open class Properties : Serializable {
    constructor()
    constructor(defaults: Properties?)

    open fun setProperty(key: String, value: String): Any?
    open fun getProperty(key: String): String?
    open fun getProperty(key: String, defaultValue: String): String
    open fun propertyNames(): Enumeration<*>
    open fun stringPropertyNames(): Set<String>
    open fun load(inStream: InputStream)
    open fun load(reader: Reader)
    open fun store(out: OutputStream, comments: String?)
    open fun store(writer: Writer, comments: String?)
    open fun list(out: PrintStream)
    open fun list(out: PrintWriter)
    open fun clear()
    open fun size(): Int
    open fun isEmpty(): Boolean
    open fun containsKey(key: Any): Boolean
    open fun get(key: Any): Any?
    open fun put(key: Any, value: Any): Any?
    open fun remove(key: Any): Any?
}
