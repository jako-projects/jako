package android.webkit

import java.util.Locale
import java.util.regex.Pattern

/**
 * Desktop JVM shim for `android.webkit.MimeTypeMap` (whose `android-all` `libcore.net.MimeUtils`
 * expects `/libcore/net/mime.types` on the boot classpath).
 */
open class MimeTypeMap private constructor() {

    open fun hasMimeType(mimeType: String?): Boolean {
        if (mimeType.isNullOrEmpty()) return false
        return mimeToExt.containsKey(mimeType.lowercase(Locale.ROOT))
    }

    open fun getMimeTypeFromExtension(extension: String?): String? {
        if (extension.isNullOrEmpty()) return null
        return extToMime[extension.lowercase(Locale.ROOT)]
    }

    open fun hasExtension(extension: String?): Boolean {
        if (extension.isNullOrEmpty()) return false
        return extToMime.containsKey(extension.lowercase(Locale.ROOT))
    }

    open fun getExtensionFromMimeType(mimeType: String?): String? {
        if (mimeType.isNullOrEmpty()) return null
        return mimeToExt[mimeType.lowercase(Locale.ROOT)]
    }

    companion object {
        private val singleton = MimeTypeMap()
        private val validFilenamePattern = Pattern.compile("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+")

        private val extToMime = LinkedHashMap<String, String>()
        private val mimeToExt = LinkedHashMap<String, String>()

        private fun add(mimeType: String, vararg extensions: String) {
            val normalizedMime = mimeType.lowercase(Locale.ROOT)
            for ((index, ext) in extensions.withIndex()) {
                val normalizedExt = ext.lowercase(Locale.ROOT)
                extToMime[normalizedExt] = normalizedMime
                if (index == 0 && !mimeToExt.containsKey(normalizedMime)) {
                    mimeToExt[normalizedMime] = normalizedExt
                }
            }
        }

        init {
            add("text/plain", "txt", "text", "conf", "def", "list", "log", "in", "ini")
            add("text/html", "html", "htm", "shtml")
            add("text/css", "css")
            add("text/csv", "csv")
            add("text/calendar", "ics", "ifb")
            add("text/markdown", "md", "markdown")
            add("text/rtf", "rtf")
            add("text/vcard", "vcf", "vcard")
            add("text/xml", "xml")
            add("application/xml", "xml", "xsl", "xsd")
            add("application/xhtml+xml", "xhtml", "xht")
            add("application/json", "json", "map")
            add("application/ld+json", "jsonld")
            add("application/javascript", "js", "mjs")
            add("text/javascript", "js")
            add("application/wasm", "wasm")

            add("image/png", "png")
            add("image/jpeg", "jpg", "jpeg", "jpe")
            add("image/gif", "gif")
            add("image/webp", "webp")
            add("image/svg+xml", "svg", "svgz")
            add("image/bmp", "bmp")
            add("image/x-ms-bmp", "bmp")
            add("image/vnd.microsoft.icon", "ico")
            add("image/x-icon", "ico")
            add("image/tiff", "tif", "tiff")
            add("image/heic", "heic")
            add("image/heif", "heif")
            add("image/avif", "avif")

            add("audio/mpeg", "mp3", "mpga", "mp2", "mp2a", "m2a", "m3a")
            add("audio/mp4", "m4a", "mp4a")
            add("audio/x-wav", "wav")
            add("audio/wav", "wav")
            add("audio/ogg", "oga", "ogg", "spx", "opus")
            add("audio/flac", "flac")
            add("audio/aac", "aac")
            add("audio/midi", "mid", "midi", "kar", "rmi")
            add("audio/webm", "weba")
            add("audio/3gpp", "3gp")
            add("audio/3gpp2", "3g2")

            add("video/mp4", "mp4", "mp4v", "mpg4", "m4v")
            add("video/webm", "webm")
            add("video/ogg", "ogv")
            add("video/x-matroska", "mkv", "mk3d", "mks")
            add("video/quicktime", "mov", "qt")
            add("video/x-msvideo", "avi")
            add("video/mpeg", "mpeg", "mpg", "mpe", "m1v", "m2v")
            add("video/3gpp", "3gp", "3gpp")
            add("video/3gpp2", "3g2", "3gpp2")
            add("video/mp2t", "ts")

            add("application/zip", "zip")
            add("application/gzip", "gz", "tgz")
            add("application/x-gzip", "gz")
            add("application/x-tar", "tar")
            add("application/x-bzip2", "bz2", "tbz2")
            add("application/x-xz", "xz")
            add("application/x-7z-compressed", "7z")
            add("application/vnd.rar", "rar")
            add("application/x-rar-compressed", "rar")
            add("application/java-archive", "jar")
            add("application/vnd.android.package-archive", "apk")

            add("application/pdf", "pdf")
            add("application/epub+zip", "epub")
            add("application/msword", "doc", "dot")
            add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx")
            add("application/vnd.ms-excel", "xls", "xlm", "xla", "xlc", "xlt", "xlw")
            add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx")
            add("application/vnd.ms-powerpoint", "ppt", "pps", "pot")
            add("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx")
            add("font/ttf", "ttf")
            add("font/otf", "otf")
            add("font/woff", "woff")
            add("font/woff2", "woff2")
            add("application/octet-stream", "bin", "dms", "lrf", "mar", "so", "dist", "distz", "pkg", "bpk", "dump", "elc", "deploy")
        }

        @JvmStatic
        fun getFileExtensionFromUrl(url: String?): String {
            if (!url.isNullOrEmpty()) {
                var u = url
                val fragment = u.lastIndexOf('#')
                if (fragment > 0) u = u.substring(0, fragment)
                val query = u.lastIndexOf('?')
                if (query > 0) u = u.substring(0, query)
                val filenamePos = u.lastIndexOf('/')
                val filename = if (0 <= filenamePos) u.substring(filenamePos + 1) else u
                if (filename.isNotEmpty() && validFilenamePattern.matcher(filename).matches()) {
                    val dotPos = filename.lastIndexOf('.')
                    if (0 <= dotPos) {
                        return filename.substring(dotPos + 1)
                    }
                }
            }
            return ""
        }

        @JvmStatic
        fun getSingleton(): MimeTypeMap = singleton
    }
}
