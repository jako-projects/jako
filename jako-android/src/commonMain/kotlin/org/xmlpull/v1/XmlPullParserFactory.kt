@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.xmlpull.v1

import java.lang.Class

/**
 * [Nullability Decision] XmlPullParserFactory.newInstance() and newPullParser()/newSerializer()
 * return non-null instances (throwing XmlPullParserException on failure).
 */
expect open class XmlPullParserFactory {
    protected constructor()

    open var isNamespaceAware: Boolean
    open var isValidating: Boolean

    open fun setFeature(name: String?, state: Boolean)
    open fun getFeature(name: String?): Boolean
    open fun setNamespaceAware(awareness: Boolean)
    open fun isNamespaceAware(): Boolean
    open fun setValidating(validating: Boolean)
    open fun isValidating(): Boolean
    open fun newPullParser(): XmlPullParser
    open fun newSerializer(): XmlSerializer

    companion object {
        val PROPERTY_NAME: String
        fun newInstance(): XmlPullParserFactory
        fun newInstance(classNames: String?, context: Class<*>?): XmlPullParserFactory
    }
}
