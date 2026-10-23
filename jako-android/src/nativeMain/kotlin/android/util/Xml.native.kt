package android.util

import java.io.UnsupportedEncodingException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer

/**
 * [Nullability Decision] Xml.newPullParser(), newSerializer(), and findEncodingByName() return non-null instances,
 * matching AOSP android.util.Xml.
 */
actual open class Xml {
    actual enum class Encoding(internal val expatName: String) {
        US_ASCII("US-ASCII"),
        UTF_8("UTF-8"),
        UTF_16("UTF-16"),
        ISO_8859_1("ISO-8859-1")
    }

    actual companion object {
        actual val FEATURE_RELAXED: String = "http://xmlpull.org/v1/doc/features.html#relaxed"

        actual fun newPullParser(): XmlPullParser {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, true)
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            return parser
        }

        actual fun newSerializer(): XmlSerializer =
            XmlPullParserFactory.newInstance().newSerializer()

        actual fun findEncodingByName(encodingName: String?): Encoding {
            if (encodingName == null) return Encoding.UTF_8
            for (encoding in Encoding.entries) {
                if (encoding.expatName.equals(encodingName, ignoreCase = true) ||
                    encoding.name.equals(encodingName, ignoreCase = true)
                ) {
                    return encoding
                }
            }
            throw UnsupportedEncodingException(encodingName)
        }
    }
}
