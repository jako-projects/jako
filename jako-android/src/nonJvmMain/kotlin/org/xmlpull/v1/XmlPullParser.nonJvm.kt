package org.xmlpull.v1

import java.io.InputStream
import java.io.Reader

actual interface XmlPullParser {
    actual val inputEncoding: String?
    actual val depth: Int
    actual val positionDescription: String
    actual val lineNumber: Int
    actual val columnNumber: Int
    actual val isWhitespace: Boolean
    actual val text: String?
    actual val namespace: String?
    actual val name: String?
    actual val prefix: String?
    actual val isEmptyElementTag: Boolean
    actual val attributeCount: Int
    actual val eventType: Int

    actual fun setFeature(name: String?, state: Boolean)
    actual fun getFeature(name: String?): Boolean
    actual fun setProperty(name: String?, value: Any?)
    actual fun getProperty(name: String?): Any?
    actual fun setInput(inReader: Reader?)
    actual fun setInput(inputStream: InputStream?, inputEncoding: String?)
    actual fun getInputEncoding(): String?
    actual fun defineEntityReplacementText(entityName: String?, replacementText: String?)
    actual fun getNamespaceCount(depth: Int): Int
    actual fun getNamespacePrefix(pos: Int): String?
    actual fun getNamespaceUri(pos: Int): String?
    actual fun getNamespace(prefix: String?): String?
    actual fun getDepth(): Int
    actual fun getPositionDescription(): String
    actual fun getLineNumber(): Int
    actual fun getColumnNumber(): Int
    actual fun isWhitespace(): Boolean
    actual fun getText(): String?
    actual fun getTextCharacters(holderForStartAndLength: IntArray): CharArray?
    actual fun getNamespace(): String?
    actual fun getName(): String?
    actual fun getPrefix(): String?
    actual fun isEmptyElementTag(): Boolean
    actual fun getAttributeCount(): Int
    actual fun getAttributeNamespace(index: Int): String
    actual fun getAttributeName(index: Int): String
    actual fun getAttributePrefix(index: Int): String?
    actual fun getAttributeType(index: Int): String
    actual fun isAttributeDefault(index: Int): Boolean
    actual fun getAttributeValue(index: Int): String
    actual fun getAttributeValue(namespace: String?, name: String?): String?
    actual fun getEventType(): Int
    actual fun next(): Int
    actual fun nextToken(): Int
    actual fun require(type: Int, namespace: String?, name: String?)
    actual fun nextText(): String
    actual fun nextTag(): Int

    actual companion object {
        actual const val NO_NAMESPACE: String = ""
        actual const val START_DOCUMENT: Int = 0
        actual const val END_DOCUMENT: Int = 1
        actual const val START_TAG: Int = 2
        actual const val END_TAG: Int = 3
        actual const val TEXT: Int = 4
        actual const val CDSECT: Int = 5
        actual const val ENTITY_REF: Int = 6
        actual const val IGNORABLE_WHITESPACE: Int = 7
        actual const val PROCESSING_INSTRUCTION: Int = 8
        actual const val COMMENT: Int = 9
        actual const val DOCDECL: Int = 10
        actual val TYPES: Array<String> = arrayOf(
            "START_DOCUMENT",
            "END_DOCUMENT",
            "START_TAG",
            "END_TAG",
            "TEXT",
            "CDSECT",
            "ENTITY_REF",
            "IGNORABLE_WHITESPACE",
            "PROCESSING_INSTRUCTION",
            "COMMENT",
            "DOCDECL"
        )
        actual const val FEATURE_PROCESS_NAMESPACES: String =
            "http://xmlpull.org/v1/doc/features.html#process-namespaces"
        actual const val FEATURE_REPORT_NAMESPACE_ATTRIBUTES: String =
            "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes"
        actual const val FEATURE_PROCESS_DOCDECL: String =
            "http://xmlpull.org/v1/doc/features.html#process-docdecl"
        actual const val FEATURE_VALIDATION: String =
            "http://xmlpull.org/v1/doc/features.html#validation"
    }
}
