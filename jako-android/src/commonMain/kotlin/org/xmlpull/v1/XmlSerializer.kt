@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.xmlpull.v1

import java.io.OutputStream
import java.io.Writer

/**
 * [Nullability Decision] XmlSerializer methods return non-null XmlSerializer for fluent chaining,
 * while namespace, encoding, and standalone parameters are nullable per XMLPull v1 specification.
 */
expect interface XmlSerializer {
    val depth: Int
    val namespace: String?
    val name: String?

    fun setFeature(name: String?, state: Boolean)
    fun getFeature(name: String?): Boolean
    fun setProperty(name: String?, value: Any?)
    fun getProperty(name: String?): Any?
    fun setOutput(os: OutputStream?, encoding: String?)
    fun setOutput(writer: Writer?)
    fun startDocument(encoding: String?, standalone: Boolean?)
    fun endDocument()
    fun setPrefix(prefix: String?, namespace: String?)
    fun getPrefix(namespace: String?, generatePrefix: Boolean): String?
    fun getDepth(): Int
    fun getNamespace(): String?
    fun getName(): String?
    fun startTag(namespace: String?, name: String?): XmlSerializer
    fun attribute(namespace: String?, name: String?, value: String?): XmlSerializer
    fun endTag(namespace: String?, name: String?): XmlSerializer
    fun text(text: String?): XmlSerializer
    fun text(buf: CharArray, start: Int, len: Int): XmlSerializer
    fun cdsect(text: String)
    fun entityRef(text: String)
    fun processingInstruction(text: String)
    fun comment(text: String)
    fun docdecl(text: String)
    fun ignorableWhitespace(text: String)
    fun flush()
}
