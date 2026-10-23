@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.xmlpull.v1

import java.io.InputStream
import java.io.Reader

/**
 * [Nullability Decision] XmlPullParser methods follow the XMLPull v1 specification:
 * - Tag/attribute names and values by index are non-null when valid.
 * - Optional namespaces, prefixes, text (on non-text events), and attribute lookups by name return String?.
 */
expect interface XmlPullParser {
    val inputEncoding: String?
    val depth: Int
    val positionDescription: String
    val lineNumber: Int
    val columnNumber: Int
    val isWhitespace: Boolean
    val text: String?
    val namespace: String?
    val name: String?
    val prefix: String?
    val isEmptyElementTag: Boolean
    val attributeCount: Int
    val eventType: Int

    fun setFeature(name: String?, state: Boolean)
    fun getFeature(name: String?): Boolean
    fun setProperty(name: String?, value: Any?)
    fun getProperty(name: String?): Any?
    fun setInput(inReader: Reader?)
    fun setInput(inputStream: InputStream?, inputEncoding: String?)
    fun getInputEncoding(): String?
    fun defineEntityReplacementText(entityName: String?, replacementText: String?)
    fun getNamespaceCount(depth: Int): Int
    fun getNamespacePrefix(pos: Int): String?
    fun getNamespaceUri(pos: Int): String?
    fun getNamespace(prefix: String?): String?
    fun getDepth(): Int
    fun getPositionDescription(): String
    fun getLineNumber(): Int
    fun getColumnNumber(): Int
    fun isWhitespace(): Boolean
    fun getText(): String?
    fun getTextCharacters(holderForStartAndLength: IntArray): CharArray?
    fun getNamespace(): String?
    fun getName(): String?
    fun getPrefix(): String?
    fun isEmptyElementTag(): Boolean
    fun getAttributeCount(): Int
    fun getAttributeNamespace(index: Int): String
    fun getAttributeName(index: Int): String
    fun getAttributePrefix(index: Int): String?
    fun getAttributeType(index: Int): String
    fun isAttributeDefault(index: Int): Boolean
    fun getAttributeValue(index: Int): String
    fun getAttributeValue(namespace: String?, name: String?): String?
    fun getEventType(): Int
    fun next(): Int
    fun nextToken(): Int
    fun require(type: Int, namespace: String?, name: String?)
    fun nextText(): String
    fun nextTag(): Int

    companion object {
        val NO_NAMESPACE: String
        val START_DOCUMENT: Int
        val END_DOCUMENT: Int
        val START_TAG: Int
        val END_TAG: Int
        val TEXT: Int
        val CDSECT: Int
        val ENTITY_REF: Int
        val IGNORABLE_WHITESPACE: Int
        val PROCESSING_INSTRUCTION: Int
        val COMMENT: Int
        val DOCDECL: Int
        val TYPES: Array<String>
        val FEATURE_PROCESS_NAMESPACES: String
        val FEATURE_REPORT_NAMESPACE_ATTRIBUTES: String
        val FEATURE_PROCESS_DOCDECL: String
        val FEATURE_VALIDATION: String
    }
}
