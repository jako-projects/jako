package android.webkit

import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.regex.Pattern

open class URLUtil {
    companion object {
        private const val ASSET_BASE = "file:///android_asset/"
        private const val FILE_BASE = "file:"
        private const val CONTENT_BASE = "content:"

        private val CONTENT_DISPOSITION_PATTERN = Pattern.compile(
            "attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$",
            Pattern.CASE_INSENSITIVE
        )

        @JvmStatic
        fun guessUrl(inUrl: String?): String {
            if (inUrl.isNullOrEmpty()) return ""
            if (inUrl.startsWith("about:") ||
                inUrl.startsWith("data:") ||
                inUrl.startsWith("file:") ||
                inUrl.startsWith("javascript:")
            ) {
                return inUrl
            }
            var url = inUrl
            if (url.endsWith(".")) {
                url = url.substring(0, url.length - 1)
            }
            val uri = Uri.parse(url)
            val scheme = uri.scheme
            if (scheme == null) {
                return "http://$url"
            }
            return url
        }

        @JvmStatic
        fun composeSearchUrl(inQuery: String?, template: String?, queryPlaceHolder: String?): String? {
            if (inQuery == null || template == null || queryPlaceHolder == null) return null
            val placeHolderIndex = template.indexOf(queryPlaceHolder)
            if (placeHolderIndex < 0) return null
            val encoded = Uri.encode(inQuery) ?: return null
            return template.substring(0, placeHolderIndex) +
                encoded +
                template.substring(placeHolderIndex + queryPlaceHolder.length)
        }

        @JvmStatic
        fun decode(url: ByteArray?): ByteArray? {
            if (url == null) return null
            if (url.isEmpty()) return ByteArray(0)
            val out = ByteArrayOutputStream(url.size)
            var i = 0
            while (i < url.size) {
                val b = url[i].toInt()
                if (b == '%'.code) {
                    if (i + 2 >= url.size) {
                        throw IllegalArgumentException("Invalid % sequence at index $i")
                    }
                    val d1 = parseHexDigit(url[i + 1])
                    val d2 = parseHexDigit(url[i + 2])
                    out.write((d1 shl 4) + d2)
                    i += 3
                } else {
                    out.write(b)
                    i++
                }
            }
            return out.toByteArray()
        }

        private fun parseHexDigit(b: Byte): Int {
            val c = b.toInt().toChar()
            return when (c) {
                in '0'..'9' -> c - '0'
                in 'a'..'f' -> c - 'a' + 10
                in 'A'..'F' -> c - 'A' + 10
                else -> throw IllegalArgumentException("Invalid hex digit: $c")
            }
        }

        @JvmStatic
        fun isAssetUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith(ASSET_BASE)

        @JvmStatic
        fun isFileUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith(FILE_BASE) && !url.startsWith(ASSET_BASE)

        @JvmStatic
        fun isAboutUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith("about:")

        @JvmStatic
        fun isDataUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith("data:")

        @JvmStatic
        fun isJavaScriptUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith("javascript:")

        @JvmStatic
        fun isHttpUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.length > 6 && url.substring(0, 7).equals("http://", ignoreCase = true)

        @JvmStatic
        fun isHttpsUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.length > 7 && url.substring(0, 8).equals("https://", ignoreCase = true)

        @JvmStatic
        fun isNetworkUrl(url: String?): Boolean {
            if (url.isNullOrEmpty()) return false
            return isHttpUrl(url) || isHttpsUrl(url)
        }

        @JvmStatic
        fun isContentUrl(url: String?): Boolean =
            !url.isNullOrEmpty() && url.startsWith(CONTENT_BASE)

        @JvmStatic
        fun isValidUrl(url: String?): Boolean {
            if (url.isNullOrEmpty()) return false
            return isAssetUrl(url) ||
                isFileUrl(url) ||
                isAboutUrl(url) ||
                isHttpUrl(url) ||
                isHttpsUrl(url) ||
                isJavaScriptUrl(url) ||
                isContentUrl(url)
        }

        @JvmStatic
        fun stripAnchor(url: String?): String? {
            if (url == null) return null
            val anchorIndex = url.indexOf('#')
            return if (anchorIndex != -1) url.substring(0, anchorIndex) else url
        }

        @JvmStatic
        fun guessFileName(url: String?, contentDisposition: String?, mimeType: String?): String {
            var filename: String? = null
            var extension: String? = null

            if (contentDisposition != null) {
                filename = parseContentDisposition(contentDisposition)
                if (filename != null) {
                    val index = filename.lastIndexOf('/') + 1
                    if (index > 0) {
                        filename = filename.substring(index)
                    }
                }
            }

            if (filename == null && url != null) {
                var decodedUrl = Uri.decode(url)
                if (decodedUrl != null) {
                    val queryIndex = decodedUrl.indexOf('?')
                    if (queryIndex > 0) {
                        decodedUrl = decodedUrl.substring(0, queryIndex)
                    }
                    if (!decodedUrl.endsWith("/")) {
                        val index = decodedUrl.lastIndexOf('/') + 1
                        if (index > 0) {
                            filename = decodedUrl.substring(index)
                        }
                    }
                }
            }

            if (filename == null) {
                filename = "downloadfile"
            }

            val dotIndex = filename.indexOf('.')
            if (dotIndex < 0) {
                if (mimeType != null) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                    if (extension != null) {
                        extension = ".$extension"
                    }
                }
                if (extension == null) {
                    extension = if (mimeType != null && mimeType.lowercase(Locale.ROOT).startsWith("text/")) {
                        if (mimeType.equals("text/html", ignoreCase = true)) ".html" else ".txt"
                    } else {
                        ".bin"
                    }
                }
            } else {
                if (mimeType != null) {
                    val lastDotIndex = filename.lastIndexOf('.')
                    val typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        filename.substring(lastDotIndex + 1)
                    )
                    if (typeFromExt != null && !typeFromExt.equals(mimeType, ignoreCase = true)) {
                        extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        if (extension != null) {
                            extension = ".$extension"
                        }
                    }
                }
                if (extension == null) {
                    extension = filename.substring(dotIndex)
                }
                filename = filename.substring(0, dotIndex)
            }

            return filename + extension
        }

        private fun parseContentDisposition(contentDisposition: String): String? {
            try {
                val m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition.trim())
                if (m.find()) {
                    return m.group(2)
                }
            } catch (_: IllegalStateException) {
            }
            return null
        }
    }
}
