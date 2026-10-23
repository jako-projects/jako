@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.webkit

/**
 * [Nullability Decision] Boolean URL predicate methods accept `String?` and return `false` for `null`/empty strings.
 * `guessFileName` always returns a non-null fallback filename (e.g. `"downloadfile.bin"`), matching AOSP `URLUtil`.
 */
expect open class URLUtil() {
    companion object {
        fun guessUrl(inUrl: String?): String
        fun composeSearchUrl(inQuery: String?, template: String?, queryPlaceHolder: String?): String?
        fun decode(url: ByteArray?): ByteArray?
        fun isAssetUrl(url: String?): Boolean
        fun isFileUrl(url: String?): Boolean
        fun isAboutUrl(url: String?): Boolean
        fun isDataUrl(url: String?): Boolean
        fun isJavaScriptUrl(url: String?): Boolean
        fun isHttpUrl(url: String?): Boolean
        fun isHttpsUrl(url: String?): Boolean
        fun isNetworkUrl(url: String?): Boolean
        fun isContentUrl(url: String?): Boolean
        fun isValidUrl(url: String?): Boolean
        fun stripAnchor(url: String?): String?
        fun guessFileName(url: String?, contentDisposition: String?, mimeType: String?): String
    }
}
