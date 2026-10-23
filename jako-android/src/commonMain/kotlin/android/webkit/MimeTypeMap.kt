@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.webkit

/**
 * [Nullability Decision] MimeTypeMap.getSingleton() and getFileExtensionFromUrl() return non-null values,
 * while lookup methods getMimeTypeFromExtension() and getExtensionFromMimeType() return String? (null when unknown),
 * matching AOSP android.webkit.MimeTypeMap.
 */
expect open class MimeTypeMap {
    open fun hasMimeType(mimeType: String?): Boolean
    open fun getMimeTypeFromExtension(extension: String?): String?
    open fun hasExtension(extension: String?): Boolean
    open fun getExtensionFromMimeType(mimeType: String?): String?

    companion object {
        fun getFileExtensionFromUrl(url: String?): String
        fun getSingleton(): MimeTypeMap
    }
}
