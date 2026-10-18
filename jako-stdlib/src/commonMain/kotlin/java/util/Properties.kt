@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Reader
import java.io.Serializable
import java.io.Writer
import java.lang.Cloneable

expect open class Properties : Hashtable<Any, Any>, MutableMap<Any, Any>, Cloneable, Serializable {
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
    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: Any): Boolean
    override fun containsValue(value: Any): Boolean
    override fun get(key: Any): Any?
    override fun put(key: Any, value: Any): Any?
    override fun remove(key: Any): Any?
    override fun putAll(from: Map<out Any, Any>)
    override fun clear()
    override val keys: MutableSet<Any>
    override val values: MutableCollection<Any>
    override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
    override fun clone(): Any
}
