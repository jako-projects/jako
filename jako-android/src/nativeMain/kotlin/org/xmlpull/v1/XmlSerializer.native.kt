package org.xmlpull.v1

import java.io.OutputStream
import java.io.Writer

actual interface XmlSerializer {
    actual val depth: Int
    actual val namespace: String?
    actual val name: String?

    actual fun setFeature(name: String?, state: Boolean)
    actual fun getFeature(name: String?): Boolean
    actual fun setProperty(name: String?, value: Any?)
    actual fun getProperty(name: String?): Any?
    actual fun setOutput(os: OutputStream?, encoding: String?)
    actual fun setOutput(writer: Writer?)
    actual fun startDocument(encoding: String?, standalone: Boolean?)
    actual fun endDocument()
    actual fun setPrefix(prefix: String?, namespace: String?)
    actual fun getPrefix(namespace: String?, generatePrefix: Boolean): String?
    actual fun getDepth(): Int
    actual fun getNamespace(): String?
    actual fun getName(): String?
    actual fun startTag(namespace: String?, name: String?): XmlSerializer
    actual fun attribute(namespace: String?, name: String?, value: String?): XmlSerializer
    actual fun endTag(namespace: String?, name: String?): XmlSerializer
    actual fun text(text: String?): XmlSerializer
    actual fun text(buf: CharArray, start: Int, len: Int): XmlSerializer
    actual fun cdsect(text: String)
    actual fun entityRef(text: String)
    actual fun processingInstruction(text: String)
    actual fun comment(text: String)
    actual fun docdecl(text: String)
    actual fun ignorableWhitespace(text: String)
    actual fun flush()
}
