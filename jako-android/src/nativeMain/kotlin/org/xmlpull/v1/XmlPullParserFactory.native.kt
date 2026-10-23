package org.xmlpull.v1

import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.lang.Class
import java.nio.charset.Charset

actual open class XmlPullParserFactory protected actual constructor() {
    private val features = LinkedHashMap<String, Boolean>()

    actual open var isNamespaceAware: Boolean
        get() = features[XmlPullParser.FEATURE_PROCESS_NAMESPACES] ?: false
        set(value) {
            features[XmlPullParser.FEATURE_PROCESS_NAMESPACES] = value
        }

    actual open var isValidating: Boolean
        get() = features[XmlPullParser.FEATURE_VALIDATION] ?: false
        set(value) {
            features[XmlPullParser.FEATURE_VALIDATION] = value
        }

    actual open fun setFeature(name: String?, state: Boolean) {
        if (name == null) throw IllegalArgumentException("Feature name cannot be null")
        features[name] = state
    }

    actual open fun getFeature(name: String?): Boolean {
        if (name == null) return false
        return features[name] ?: false
    }

    actual open fun setNamespaceAware(awareness: Boolean) {
        isNamespaceAware = awareness
    }

    actual open fun isNamespaceAware(): Boolean = isNamespaceAware

    actual open fun setValidating(validating: Boolean) {
        isValidating = validating
    }

    actual open fun isValidating(): Boolean = isValidating

    actual open fun newPullParser(): XmlPullParser {
        val parser = NativeXmlPullParser()
        for ((k, v) in features) {
            parser.setFeature(k, v)
        }
        return parser
    }

    actual open fun newSerializer(): XmlSerializer = NativeXmlSerializer()

    actual companion object {
        actual const val PROPERTY_NAME: String = "org.xmlpull.v1.XmlPullParserFactory"

        actual fun newInstance(): XmlPullParserFactory = XmlPullParserFactory()

        actual fun newInstance(classNames: String?, context: Class<*>?): XmlPullParserFactory =
            XmlPullParserFactory()
    }
}

internal class NativeXmlPullParser : XmlPullParser {
    private val features = LinkedHashMap<String, Boolean>()
    private val properties = LinkedHashMap<String, Any?>()
    private val customEntities = LinkedHashMap<String, String>()

    private var source: String = ""
    private var pos: Int = 0
    private var _line: Int = 1
    private var _column: Int = 0
    private var _encoding: String? = null

    private var _eventType: Int = XmlPullParser.START_DOCUMENT
    private var _text: String? = null
    private var _name: String? = null
    private var _namespace: String? = null
    private var _prefix: String? = null
    private var _isEmptyElementTag: Boolean = false
    private var pendingEndTag: Boolean = false

    private data class Attr(
        val namespace: String,
        val prefix: String?,
        val name: String,
        val value: String
    )

    private data class NsDecl(
        val depth: Int,
        val prefix: String?,
        val uri: String
    )

    private data class ElementFrame(
        val namespace: String,
        val prefix: String?,
        val name: String
    )

    private val attributes = ArrayList<Attr>()
    private val nsStack = ArrayList<NsDecl>()
    private val elementStack = ArrayList<ElementFrame>()

    init {
        resetEntities()
    }

    private fun resetEntities() {
        customEntities.clear()
        customEntities["lt"] = "<"
        customEntities["gt"] = ">"
        customEntities["amp"] = "&"
        customEntities["apos"] = "'"
        customEntities["quot"] = "\""
    }

    override val inputEncoding: String? get() = _encoding
    override val depth: Int get() = elementStack.size
    override val positionDescription: String
        get() {
            val typeName = if (_eventType in XmlPullParser.TYPES.indices) XmlPullParser.TYPES[_eventType] else "UNKNOWN"
            return "$typeName @$_line:$_column"
        }
    override val lineNumber: Int get() = _line
    override val columnNumber: Int get() = _column
    override val isWhitespace: Boolean get() = isWhitespace()
    override val text: String? get() = getText()
    override val namespace: String? get() = _namespace
    override val name: String? get() = _name
    override val prefix: String? get() = _prefix
    override val isEmptyElementTag: Boolean get() = isEmptyElementTag()
    override val attributeCount: Int get() = getAttributeCount()
    override val eventType: Int get() = _eventType

    override fun setFeature(name: String?, state: Boolean) {
        if (name == null) throw IllegalArgumentException("Feature name cannot be null")
        if (_eventType != XmlPullParser.START_DOCUMENT) {
            throw XmlPullParserException("Features can only be set in START_DOCUMENT state", this, null)
        }
        features[name] = state
    }

    override fun getFeature(name: String?): Boolean {
        if (name == null) return false
        return features[name] ?: false
    }

    override fun setProperty(name: String?, value: Any?) {
        if (name == null) throw IllegalArgumentException("Property name cannot be null")
        properties[name] = value
    }

    override fun getProperty(name: String?): Any? {
        if (name == null) return null
        return properties[name]
    }

    override fun setInput(inReader: Reader?) {
        resetState()
        if (inReader != null) {
            val sb = StringBuilder()
            val buf = CharArray(4096)
            while (true) {
                val n = inReader.read(buf, 0, buf.size)
                if (n < 0) break
                sb.appendRange(buf, 0, n)
            }
            source = sb.toString()
        }
    }

    override fun setInput(inputStream: InputStream?, inputEncoding: String?) {
        if (inputStream == null) throw IllegalArgumentException("inputStream cannot be null")
        val charset = if (inputEncoding != null) Charset.forName(inputEncoding) else Charsets.UTF_8
        setInput(InputStreamReader(inputStream, charset))
        _encoding = inputEncoding ?: "UTF-8"
    }

    private fun resetState() {
        source = ""
        pos = 0
        _line = 1
        _column = 0
        _encoding = null
        _eventType = XmlPullParser.START_DOCUMENT
        _text = null
        _name = null
        _namespace = null
        _prefix = null
        _isEmptyElementTag = false
        pendingEndTag = false
        attributes.clear()
        nsStack.clear()
        elementStack.clear()
        resetEntities()
    }

    override fun getInputEncoding(): String? = _encoding

    override fun defineEntityReplacementText(entityName: String?, replacementText: String?) {
        if (entityName != null && replacementText != null) {
            customEntities[entityName] = replacementText
        }
    }

    override fun getNamespaceCount(depth: Int): Int {
        if (depth < 0 || depth > elementStack.size) {
            throw IndexOutOfBoundsException("Invalid depth: $depth")
        }
        return nsStack.count { it.depth <= depth }
    }

    override fun getNamespacePrefix(pos: Int): String? = nsStack[pos].prefix

    override fun getNamespaceUri(pos: Int): String? = nsStack[pos].uri

    override fun getNamespace(prefix: String?): String? {
        if (prefix == "xml") return "http://www.w3.org/XML/1998/namespace"
        if (prefix == "xmlns") return "http://www.w3.org/2000/xmlns/"
        for (i in nsStack.lastIndex downTo 0) {
            val decl = nsStack[i]
            if (decl.prefix == prefix || (prefix.isNullOrEmpty() && decl.prefix.isNullOrEmpty())) {
                return decl.uri
            }
        }
        return null
    }

    override fun getDepth(): Int = elementStack.size
    override fun getPositionDescription(): String = positionDescription
    override fun getLineNumber(): Int = _line
    override fun getColumnNumber(): Int = _column

    override fun isWhitespace(): Boolean {
        if (_eventType != XmlPullParser.TEXT && _eventType != XmlPullParser.IGNORABLE_WHITESPACE && _eventType != XmlPullParser.CDSECT) {
            throw XmlPullParserException("isWhitespace() only valid on TEXT/CDSECT/IGNORABLE_WHITESPACE", this, null)
        }
        val t = _text ?: return true
        return t.all { it == ' ' || it == '\t' || it == '\r' || it == '\n' }
    }

    override fun getText(): String? {
        return if (_eventType < XmlPullParser.TEXT || (_eventType == XmlPullParser.ENTITY_REF && _text == null)) {
            null
        } else {
            _text
        }
    }

    override fun getTextCharacters(holderForStartAndLength: IntArray): CharArray? {
        val t = getText() ?: run {
            holderForStartAndLength[0] = -1
            holderForStartAndLength[1] = -1
            return null
        }
        val chars = t.toCharArray()
        holderForStartAndLength[0] = 0
        holderForStartAndLength[1] = chars.size
        return chars
    }

    override fun getNamespace(): String? = _namespace
    override fun getName(): String? = _name
    override fun getPrefix(): String? = _prefix

    override fun isEmptyElementTag(): Boolean {
        if (_eventType != XmlPullParser.START_TAG) {
            throw XmlPullParserException("isEmptyElementTag() only valid on START_TAG", this, null)
        }
        return _isEmptyElementTag
    }

    override fun getAttributeCount(): Int {
        if (_eventType != XmlPullParser.START_TAG) return -1
        return attributes.size
    }

    override fun getAttributeNamespace(index: Int): String {
        if (_eventType != XmlPullParser.START_TAG) throw IndexOutOfBoundsException()
        return attributes[index].namespace
    }

    override fun getAttributeName(index: Int): String {
        if (_eventType != XmlPullParser.START_TAG) throw IndexOutOfBoundsException()
        return attributes[index].name
    }

    override fun getAttributePrefix(index: Int): String? {
        if (_eventType != XmlPullParser.START_TAG) throw IndexOutOfBoundsException()
        return attributes[index].prefix
    }

    override fun getAttributeType(index: Int): String = "CDATA"
    override fun isAttributeDefault(index: Int): Boolean = false

    override fun getAttributeValue(index: Int): String {
        if (_eventType != XmlPullParser.START_TAG) throw IndexOutOfBoundsException()
        return attributes[index].value
    }

    override fun getAttributeValue(namespace: String?, name: String?): String? {
        if (_eventType != XmlPullParser.START_TAG || name == null) return null
        val processNs = getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES)
        for (attr in attributes) {
            if (attr.name == name) {
                if (!processNs || namespace.isNullOrEmpty() || attr.namespace == namespace) {
                    return attr.value
                }
            }
        }
        return null
    }

    override fun getEventType(): Int = _eventType

    override fun next(): Int {
        while (true) {
            val token = nextTokenInternal(coalesceText = true)
            when (token) {
                XmlPullParser.START_TAG,
                XmlPullParser.END_TAG,
                XmlPullParser.END_DOCUMENT -> return token
                XmlPullParser.TEXT,
                XmlPullParser.CDSECT,
                XmlPullParser.ENTITY_REF -> {
                    if (elementStack.isNotEmpty()) {
                        _eventType = XmlPullParser.TEXT
                        return XmlPullParser.TEXT
                    }
                }
                else -> {
                    // Skip comments, processing instructions, docdecl, top-level whitespace in next()
                }
            }
        }
    }

    override fun nextToken(): Int = nextTokenInternal(coalesceText = false)

    override fun require(type: Int, namespace: String?, name: String?) {
        if (type != _eventType ||
            (namespace != null && namespace != getNamespace()) ||
            (name != null && name != getName())
        ) {
            throw XmlPullParserException(
                "expected ${XmlPullParser.TYPES.getOrElse(type) { "$type" }} ${namespace ?: ""}:${name ?: ""}",
                this,
                null
            )
        }
    }

    override fun nextText(): String {
        if (_eventType != XmlPullParser.START_TAG) {
            throw XmlPullParserException("parser must be on START_TAG to read next text", this, null)
        }
        var ev = next()
        return when (ev) {
            XmlPullParser.TEXT -> {
                val result = getText() ?: ""
                ev = next()
                if (ev != XmlPullParser.END_TAG) {
                    throw XmlPullParserException("TEXT must be immediately followed by END_TAG", this, null)
                }
                result
            }
            XmlPullParser.END_TAG -> ""
            else -> throw XmlPullParserException("parser must be on START_TAG or TEXT to read text", this, null)
        }
    }

    override fun nextTag(): Int {
        var ev = next()
        if (ev == XmlPullParser.TEXT && isWhitespace()) {
            ev = next()
        }
        if (ev != XmlPullParser.START_TAG && ev != XmlPullParser.END_TAG) {
            throw XmlPullParserException("expected start or end tag", this, null)
        }
        return ev
    }

    private fun readChar(): Char {
        val ch = source[pos++]
        if (ch == '\n') {
            _line++
            _column = 0
        } else {
            _column++
        }
        return ch
    }

    private fun peekChar(offset: Int = 0): Char {
        val idx = pos + offset
        return if (idx < source.length) source[idx] else '\u0000'
    }

    private fun startsWithAtPos(prefixStr: String): Boolean =
        source.regionMatches(pos, prefixStr, 0, prefixStr.length)

    private fun advanceBy(count: Int) {
        repeat(count) { readChar() }
    }

    private fun skipWhitespace() {
        while (pos < source.length) {
            val ch = source[pos]
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                readChar()
            } else {
                break
            }
        }
    }

    private fun nextTokenInternal(coalesceText: Boolean): Int {
        if (_eventType == XmlPullParser.END_TAG && elementStack.isNotEmpty()) {
            val poppedDepth = elementStack.size
            elementStack.removeAt(elementStack.lastIndex)
            while (nsStack.isNotEmpty() && nsStack.last().depth >= poppedDepth) {
                nsStack.removeAt(nsStack.lastIndex)
            }
        }

        if (pendingEndTag) {
            pendingEndTag = false
            _isEmptyElementTag = false
            attributes.clear()
            val top = elementStack.lastOrNull()
                ?: throw XmlPullParserException("Unexpected empty element end tag", this, null)
            _namespace = top.namespace
            _prefix = top.prefix
            _name = top.name
            _text = null
            _eventType = XmlPullParser.END_TAG
            return _eventType
        }

        _isEmptyElementTag = false
        attributes.clear()

        while (true) {
            if (pos >= source.length) {
                if (elementStack.isNotEmpty()) {
                    throw XmlPullParserException(
                        "Unclosed element <${elementStack.last().name}> at end of document",
                        this,
                        null
                    )
                }
                _name = null
                _namespace = null
                _prefix = null
                _text = null
                _eventType = XmlPullParser.END_DOCUMENT
                return _eventType
            }

            if (startsWithAtPos("<?xml") && (peekChar(5).isWhitespace() || peekChar(5) == '?')) {
                advanceBy(5)
                val endIdx = source.indexOf("?>", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed XML declaration", this, null)
                val declText = source.substring(pos, endIdx)
                val encMatch = Regex("""encoding\s*=\s*['"]([^'"]+)['"]""").find(declText)
                if (encMatch != null && _encoding == null) {
                    _encoding = encMatch.groupValues[1]
                }
                advanceBy((endIdx - pos) + 2)
                skipWhitespace()
                continue
            }

            if (startsWithAtPos("<!--")) {
                advanceBy(4)
                val endIdx = source.indexOf("-->", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed XML comment", this, null)
                val commentBody = source.substring(pos, endIdx)
                advanceBy((endIdx - pos) + 3)
                _name = null
                _namespace = null
                _prefix = null
                _text = commentBody
                _eventType = XmlPullParser.COMMENT
                return _eventType
            }

            if (startsWithAtPos("<![CDATA[")) {
                advanceBy(9)
                val endIdx = source.indexOf("]]>", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed CDATA section", this, null)
                val cdataBody = source.substring(pos, endIdx)
                advanceBy((endIdx - pos) + 3)
                _name = null
                _namespace = null
                _prefix = null
                if (coalesceText) {
                    val sb = StringBuilder(cdataBody)
                    readRemainingCoalescedText(sb)
                    _text = sb.toString()
                    _eventType = XmlPullParser.TEXT
                } else {
                    _text = cdataBody
                    _eventType = XmlPullParser.CDSECT
                }
                return _eventType
            }

            if (startsWithAtPos("<!DOCTYPE")) {
                advanceBy(9)
                val sb = StringBuilder()
                var bracketDepth = 0
                while (pos < source.length) {
                    val ch = readChar()
                    if (ch == '[') bracketDepth++
                    else if (ch == ']') bracketDepth = maxOf(0, bracketDepth - 1)
                    else if (ch == '>' && bracketDepth == 0) break
                    sb.append(ch)
                }
                _name = null
                _namespace = null
                _prefix = null
                _text = sb.toString()
                _eventType = XmlPullParser.DOCDECL
                return _eventType
            }

            if (startsWithAtPos("<?")) {
                advanceBy(2)
                val endIdx = source.indexOf("?>", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed processing instruction", this, null)
                val piBody = source.substring(pos, endIdx)
                advanceBy((endIdx - pos) + 2)
                _name = null
                _namespace = null
                _prefix = null
                _text = piBody
                _eventType = XmlPullParser.PROCESSING_INSTRUCTION
                return _eventType
            }

            if (startsWithAtPos("</")) {
                advanceBy(2)
                skipWhitespace()
                val rawTagName = readNameToken()
                if (rawTagName.isEmpty()) {
                    throw XmlPullParserException("Expected element name in end tag", this, null)
                }
                skipWhitespace()
                if (pos >= source.length || readChar() != '>') {
                    throw XmlPullParserException("Expected '>' closing end tag </$rawTagName>", this, null)
                }
                val top = elementStack.lastOrNull()
                    ?: throw XmlPullParserException("Unexpected end tag </$rawTagName> with empty stack", this, null)
                val expectedRaw = if (top.prefix != null) "${top.prefix}:${top.name}" else top.name
                val processNs = getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES)
                if ((processNs && expectedRaw != rawTagName) || (!processNs && top.name != rawTagName)) {
                    throw XmlPullParserException("Mismatched end tag: expected </$expectedRaw> but found </$rawTagName>", this, null)
                }
                _namespace = top.namespace
                _prefix = top.prefix
                _name = top.name
                _text = "</$rawTagName>"
                _eventType = XmlPullParser.END_TAG
                return _eventType
            }

            if (peekChar() == '<') {
                readChar() // '<'
                val rawTagName = readNameToken()
                if (rawTagName.isEmpty()) {
                    throw XmlPullParserException("Expected element name after '<'", this, null)
                }

                val rawAttrs = ArrayList<Pair<String, String>>()
                var emptyTag = false
                while (pos < source.length) {
                    skipWhitespace()
                    if (startsWithAtPos("/>")) {
                        advanceBy(2)
                        emptyTag = true
                        break
                    }
                    if (peekChar() == '>') {
                        readChar()
                        break
                    }
                    val attrName = readNameToken()
                    if (attrName.isEmpty()) {
                        throw XmlPullParserException("Unexpected character '${peekChar()}' in start tag <$rawTagName>", this, null)
                    }
                    skipWhitespace()
                    if (pos >= source.length || readChar() != '=') {
                        throw XmlPullParserException("Expected '=' after attribute '$attrName'", this, null)
                    }
                    skipWhitespace()
                    val quote = if (pos < source.length) readChar() else '\u0000'
                    if (quote != '"' && quote != '\'') {
                        throw XmlPullParserException("Expected quote for attribute '$attrName'", this, null)
                    }
                    val valSb = StringBuilder()
                    while (pos < source.length) {
                        val c = readChar()
                        if (c == quote) break
                        if (c == '&') {
                            valSb.append(readEntityValue())
                        } else {
                            valSb.append(c)
                        }
                    }
                    rawAttrs.add(attrName to valSb.toString())
                }

                val newDepth = elementStack.size + 1
                val processNs = getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES)
                val reportNsAttrs = getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES)

                if (processNs) {
                    for ((aName, aVal) in rawAttrs) {
                        if (aName == "xmlns") {
                            nsStack.add(NsDecl(newDepth, null, aVal))
                        } else if (aName.startsWith("xmlns:")) {
                            val pfx = aName.substringAfter("xmlns:")
                            nsStack.add(NsDecl(newDepth, pfx, aVal))
                        }
                    }
                }

                val (tagPrefix, localTagName) = if (processNs && ':' in rawTagName) {
                    rawTagName.substringBefore(':') to rawTagName.substringAfter(':')
                } else {
                    null to rawTagName
                }
                val resolvedTagNs = if (processNs) {
                    getNamespace(tagPrefix) ?: XmlPullParser.NO_NAMESPACE
                } else {
                    XmlPullParser.NO_NAMESPACE
                }

                for ((aName, aVal) in rawAttrs) {
                    if (processNs && (aName == "xmlns" || aName.startsWith("xmlns:"))) {
                        if (!reportNsAttrs) continue
                    }
                    val (aPrefix, aLocal) = if (processNs && ':' in aName) {
                        aName.substringBefore(':') to aName.substringAfter(':')
                    } else {
                        null to aName
                    }
                    val aNs = if (processNs && aPrefix != null) {
                        getNamespace(aPrefix) ?: XmlPullParser.NO_NAMESPACE
                    } else {
                        XmlPullParser.NO_NAMESPACE
                    }
                    attributes.add(Attr(aNs, aPrefix, aLocal, aVal))
                }

                elementStack.add(ElementFrame(resolvedTagNs, tagPrefix, localTagName))
                _namespace = resolvedTagNs
                _prefix = tagPrefix
                _name = localTagName
                _text = null
                _isEmptyElementTag = emptyTag
                pendingEndTag = emptyTag
                _eventType = XmlPullParser.START_TAG
                return _eventType
            }

            // Text content
            val sb = StringBuilder()
            if (coalesceText) {
                readRemainingCoalescedText(sb)
            } else {
                while (pos < source.length && peekChar() != '<') {
                    val c = readChar()
                    if (c == '&') {
                        sb.append(readEntityValue())
                    } else {
                        sb.append(c)
                    }
                }
            }
            val textVal = sb.toString()
            if (elementStack.isEmpty() && textVal.all { it == ' ' || it == '\t' || it == '\r' || it == '\n' }) {
                if (coalesceText) continue
                _name = null
                _namespace = null
                _prefix = null
                _text = textVal
                _eventType = XmlPullParser.IGNORABLE_WHITESPACE
                return _eventType
            }
            _name = null
            _namespace = null
            _prefix = null
            _text = textVal
            _eventType = XmlPullParser.TEXT
            return _eventType
        }
    }

    private fun readRemainingCoalescedText(sb: StringBuilder) {
        while (pos < source.length) {
            if (startsWithAtPos("<![CDATA[")) {
                advanceBy(9)
                val endIdx = source.indexOf("]]>", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed CDATA section", this, null)
                sb.append(source, pos, endIdx)
                advanceBy((endIdx - pos) + 3)
            } else if (startsWithAtPos("<!--")) {
                advanceBy(4)
                val endIdx = source.indexOf("-->", pos)
                if (endIdx < 0) throw XmlPullParserException("Unclosed XML comment", this, null)
                advanceBy((endIdx - pos) + 3)
            } else if (peekChar() == '<') {
                break
            } else {
                val c = readChar()
                if (c == '&') {
                    sb.append(readEntityValue())
                } else {
                    sb.append(c)
                }
            }
        }
    }

    private fun readNameToken(): String {
        val sb = StringBuilder()
        while (pos < source.length) {
            val c = peekChar()
            if (c.isWhitespace() || c == '=' || c == '/' || c == '>' || c == '<' || c == '?' || c == '"' || c == '\'') {
                break
            }
            sb.append(readChar())
        }
        return sb.toString()
    }

    private fun readEntityValue(): String {
        val semi = source.indexOf(';', pos)
        if (semi < 0) throw XmlPullParserException("Unclosed entity reference", this, null)
        val entity = source.substring(pos, semi)
        advanceBy((semi - pos) + 1)
        if (entity.startsWith("#x") || entity.startsWith("#X")) {
            val code = entity.substring(2).toIntOrNull(16)
                ?: throw XmlPullParserException("Invalid hex character entity &#$entity;", this, null)
            return code.toChar().toString()
        }
        if (entity.startsWith("#")) {
            val code = entity.substring(1).toIntOrNull(10)
                ?: throw XmlPullParserException("Invalid decimal character entity &$entity;", this, null)
            return code.toChar().toString()
        }
        return customEntities[entity]
            ?: throw XmlPullParserException("Undefined entity &$entity;", this, null)
    }
}

internal class NativeXmlSerializer : XmlSerializer {
    private val features = LinkedHashMap<String, Boolean>()
    private val properties = LinkedHashMap<String, Any?>()
    private var outWriter: Writer? = null

    private data class TagEntry(val namespace: String?, val name: String)
    private data class NsEntry(val depth: Int, val prefix: String, val namespace: String)

    private val tagStack = ArrayList<TagEntry>()
    private val nsStack = ArrayList<NsEntry>()
    private val pendingPrefixes = ArrayList<Pair<String, String>>()
    private var inStartTag: Boolean = false
    private var autoPrefixCounter: Int = 0

    override val depth: Int get() = tagStack.size
    override val namespace: String? get() = tagStack.lastOrNull()?.namespace
    override val name: String? get() = tagStack.lastOrNull()?.name

    override fun setFeature(name: String?, state: Boolean) {
        if (name == null) throw IllegalArgumentException("Feature name cannot be null")
        features[name] = state
    }

    override fun getFeature(name: String?): Boolean {
        if (name == null) return false
        return features[name] ?: false
    }

    override fun setProperty(name: String?, value: Any?) {
        if (name == null) throw IllegalArgumentException("Property name cannot be null")
        properties[name] = value
    }

    override fun getProperty(name: String?): Any? {
        if (name == null) return null
        return properties[name]
    }

    override fun setOutput(os: OutputStream?, encoding: String?) {
        if (os == null) throw IllegalArgumentException("OutputStream cannot be null")
        val charset = if (encoding != null) Charset.forName(encoding) else Charsets.UTF_8
        setOutput(OutputStreamWriter(os, charset))
    }

    override fun setOutput(writer: Writer?) {
        outWriter = writer
        tagStack.clear()
        nsStack.clear()
        pendingPrefixes.clear()
        inStartTag = false
        autoPrefixCounter = 0
    }

    private fun requireWriter(): Writer =
        outWriter ?: throw IllegalStateException("setOutput() must be called before writing")

    private fun closePendingStartTag(selfClosing: Boolean = false) {
        if (!inStartTag) return
        val w = requireWriter()
        val currentDepth = tagStack.size
        for ((pfx, uri) in pendingPrefixes) {
            if (pfx.isEmpty()) {
                w.write(" xmlns=\"")
            } else {
                w.write(" xmlns:$pfx=\"")
            }
            writeEscaped(w, uri, isAttribute = true)
            w.write("\"")
            nsStack.add(NsEntry(currentDepth, pfx, uri))
        }
        pendingPrefixes.clear()
        if (selfClosing) {
            w.write(" />")
        } else {
            w.write(">")
        }
        inStartTag = false
    }

    override fun startDocument(encoding: String?, standalone: Boolean?) {
        val w = requireWriter()
        val enc = encoding ?: "UTF-8"
        w.write("<?xml version='1.0' encoding='$enc'")
        if (standalone != null) {
            w.write(" standalone='${if (standalone) "yes" else "no"}'")
        }
        w.write(" ?>")
        if (getFeature("http://xmlpull.org/v1/doc/features.html#indent-output")) {
            w.write("\n")
        }
    }

    override fun endDocument() {
        while (tagStack.isNotEmpty()) {
            val top = tagStack.last()
            endTag(top.namespace, top.name)
        }
        flush()
    }

    override fun setPrefix(prefix: String?, namespace: String?) {
        if (prefix == null || namespace == null) {
            throw IllegalArgumentException("prefix and namespace cannot be null")
        }
        pendingPrefixes.add(prefix to namespace)
    }

    override fun getPrefix(namespace: String?, generatePrefix: Boolean): String? {
        if (namespace.isNullOrEmpty()) return null
        for (i in pendingPrefixes.lastIndex downTo 0) {
            if (pendingPrefixes[i].second == namespace) return pendingPrefixes[i].first
        }
        for (i in nsStack.lastIndex downTo 0) {
            if (nsStack[i].namespace == namespace) return nsStack[i].prefix
        }
        if (!generatePrefix) return null
        autoPrefixCounter++
        val generated = "n$autoPrefixCounter"
        setPrefix(generated, namespace)
        return generated
    }

    override fun getDepth(): Int = tagStack.size
    override fun getNamespace(): String? = tagStack.lastOrNull()?.namespace
    override fun getName(): String? = tagStack.lastOrNull()?.name

    override fun startTag(namespace: String?, name: String?): XmlSerializer {
        val tagName = name ?: throw IllegalArgumentException("Tag name cannot be null")
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        if (getFeature("http://xmlpull.org/v1/doc/features.html#indent-output") && tagStack.isNotEmpty()) {
            w.write("\n")
            repeat(tagStack.size) { w.write("  ") }
        }
        val pfx = if (!namespace.isNullOrEmpty()) getPrefix(namespace, true) else null
        w.write("<")
        if (!pfx.isNullOrEmpty()) {
            w.write("$pfx:$tagName")
        } else {
            w.write(tagName)
        }
        tagStack.add(TagEntry(namespace, tagName))
        inStartTag = true
        return this
    }

    override fun attribute(namespace: String?, name: String?, value: String?): XmlSerializer {
        if (!inStartTag) throw IllegalStateException("attribute() must be called immediately after startTag()")
        val attrName = name ?: throw IllegalArgumentException("Attribute name cannot be null")
        val attrVal = value ?: throw IllegalArgumentException("Attribute value cannot be null")
        val w = requireWriter()
        val pfx = if (!namespace.isNullOrEmpty()) getPrefix(namespace, true) else null
        w.write(" ")
        if (!pfx.isNullOrEmpty()) {
            w.write("$pfx:$attrName")
        } else {
            w.write(attrName)
        }
        w.write("=\"")
        writeEscaped(w, attrVal, isAttribute = true)
        w.write("\"")
        return this
    }

    override fun endTag(namespace: String?, name: String?): XmlSerializer {
        val tagName = name ?: throw IllegalArgumentException("Tag name cannot be null")
        val top = tagStack.lastOrNull()
            ?: throw IllegalStateException("endTag($tagName) called with empty stack")
        if (top.name != tagName) {
            throw IllegalArgumentException("endTag($tagName) does not match startTag(${top.name})")
        }
        val w = requireWriter()
        val poppedDepth = tagStack.size
        if (inStartTag) {
            closePendingStartTag(selfClosing = true)
        } else {
            val pfx = if (!namespace.isNullOrEmpty()) getPrefix(namespace, false) else null
            w.write("</")
            if (!pfx.isNullOrEmpty()) {
                w.write("$pfx:$tagName")
            } else {
                w.write(tagName)
            }
            w.write(">")
        }
        tagStack.removeAt(tagStack.lastIndex)
        while (nsStack.isNotEmpty() && nsStack.last().depth >= poppedDepth) {
            nsStack.removeAt(nsStack.lastIndex)
        }
        return this
    }

    override fun text(text: String?): XmlSerializer {
        closePendingStartTag(selfClosing = false)
        if (text != null) {
            writeEscaped(requireWriter(), text, isAttribute = false)
        }
        return this
    }

    override fun text(buf: CharArray, start: Int, len: Int): XmlSerializer {
        return text(buf.concatToString(start, start + len))
    }

    override fun cdsect(text: String) {
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        w.write("<![CDATA[")
        w.write(text)
        w.write("]]>")
    }

    override fun entityRef(text: String) {
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        w.write("&$text;")
    }

    override fun processingInstruction(text: String) {
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        w.write("<?$text?>")
    }

    override fun comment(text: String) {
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        w.write("<!--$text-->")
    }

    override fun docdecl(text: String) {
        closePendingStartTag(selfClosing = false)
        val w = requireWriter()
        w.write("<!DOCTYPE$text>")
    }

    override fun ignorableWhitespace(text: String) {
        closePendingStartTag(selfClosing = false)
        requireWriter().write(text)
    }

    override fun flush() {
        closePendingStartTag(selfClosing = false)
        outWriter?.flush()
    }

    private fun writeEscaped(w: Writer, s: String, isAttribute: Boolean) {
        for (ch in s) {
            when (ch) {
                '&' -> w.write("&amp;")
                '<' -> w.write("&lt;")
                '>' -> w.write("&gt;")
                '"' -> if (isAttribute) w.write("&quot;") else w.write("\"")
                '\'' -> if (isAttribute) w.write("&apos;") else w.write("'")
                '\n' -> if (isAttribute) w.write("&#10;") else w.write("\n")
                '\r' -> if (isAttribute) w.write("&#13;") else w.write("\r")
                '\t' -> if (isAttribute) w.write("&#9;") else w.write("\t")
                else -> w.write(ch.toString())
            }
        }
    }
}
