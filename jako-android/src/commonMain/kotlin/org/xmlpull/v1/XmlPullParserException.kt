@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.xmlpull.v1

/**
 * [Nullability Decision] XmlPullParserException messages, parser references, and chained throwables
 * are nullable, matching the XMLPull v1 specification and AOSP org.xmlpull.v1.XmlPullParserException.
 */
expect open class XmlPullParserException : Exception {
    constructor(s: String?)
    constructor(msg: String?, parser: XmlPullParser?, chain: Throwable?)

    val detail: Throwable?
    val lineNumber: Int
    val columnNumber: Int

    open fun getDetail(): Throwable?
    open fun getLineNumber(): Int
    open fun getColumnNumber(): Int
}
