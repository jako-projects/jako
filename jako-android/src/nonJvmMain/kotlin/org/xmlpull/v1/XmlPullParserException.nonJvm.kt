package org.xmlpull.v1

/**
 * [Nullability Decision] XmlPullParserException messages, parser references, and chained throwables
 * are nullable, matching the XMLPull v1 specification.
 */
actual open class XmlPullParserException : Exception {
    private val _detail: Throwable?
    private val _lineNumber: Int
    private val _columnNumber: Int

    actual constructor(s: String?) : super(s) {
        _detail = null
        _lineNumber = -1
        _columnNumber = -1
    }

    actual constructor(msg: String?, parser: XmlPullParser?, chain: Throwable?) : super(
        buildString {
            if (msg != null) append("$msg ")
            if (parser != null) append("(position:${parser.getPositionDescription()}) ")
            if (chain != null) append("caused by: $chain")
        }.trim(),
        chain
    ) {
        _detail = chain
        _lineNumber = parser?.getLineNumber() ?: -1
        _columnNumber = parser?.getColumnNumber() ?: -1
    }

    actual val detail: Throwable? get() = _detail
    actual val lineNumber: Int get() = _lineNumber
    actual val columnNumber: Int get() = _columnNumber

    actual open fun getDetail(): Throwable? = _detail
    actual open fun getLineNumber(): Int = _lineNumber
    actual open fun getColumnNumber(): Int = _columnNumber
}
