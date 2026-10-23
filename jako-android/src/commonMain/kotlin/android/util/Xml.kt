@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

/**
 * [Nullability Decision] Xml.newPullParser(), newSerializer(), and findEncodingByName() return non-null instances,
 * matching AOSP android.util.Xml.
 */
expect open class Xml {
    enum class Encoding {
        US_ASCII,
        UTF_8,
        UTF_16,
        ISO_8859_1
    }

    companion object {
        val FEATURE_RELAXED: String
        fun newPullParser(): XmlPullParser
        fun newSerializer(): XmlSerializer
        fun findEncodingByName(encodingName: String?): Encoding
    }
}
