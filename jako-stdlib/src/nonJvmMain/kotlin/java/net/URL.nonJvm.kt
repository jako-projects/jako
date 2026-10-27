package java.net

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

actual open class URL : Serializable {
    private val uri: URI

    actual constructor(spec: String?) {
        val s = spec ?: throw MalformedURLException("null URL spec")
        try {
            this.uri = URI(s)
        } catch (e: URISyntaxException) {
            throw MalformedURLException(e.message)
        }
        val scheme = uri.getScheme()?.lowercase()
            ?: throw MalformedURLException("no protocol: $s")
        if (scheme != "http" && scheme != "https" && scheme != "file" && scheme != "jar") {
            throw MalformedURLException("unknown protocol: $scheme")
        }
    }

    actual constructor(protocol: String, host: String, port: Int, file: String) :
            this("$protocol://$host${if (port != -1) ":$port" else ""}${if (file.startsWith("/") || file.isEmpty()) file else "/$file"}")

    actual constructor(protocol: String, host: String, file: String) :
            this(protocol, host, -1, file)

    actual open fun getProtocol(): String? = uri.getScheme()
    actual open fun getHost(): String? = uri.getHost()
    actual open fun getPort(): Int = uri.getPort()
    actual open fun getPath(): String? = uri.getPath()

    actual open val protocol: String? get() = getProtocol()
    actual open val host: String? get() = getHost()
    actual open val port: Int get() = getPort()
    actual open val path: String? get() = getPath()

    actual fun getFile(): String = (uri.getPath() ?: "") + if (uri.getQuery() != null) "?" + uri.getQuery() else ""
    actual fun getQuery(): String? = uri.getQuery()

    actual open fun openConnection(): URLConnection {
        return when (val proto = getProtocol()?.lowercase()) {
            "file" -> FileURLConnection(this)
            "http" -> SocketHttpURLConnection(this)
            "https" -> javax.net.ssl.SocketHttpsURLConnection(this)
            else -> throw MalformedURLException("Unsupported protocol: $proto")
        }
    }

    actual fun openStream(): InputStream = openConnection().getInputStream()

    actual fun toURI(): URI = uri

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is URL) return false
        return uri == other.uri
    }

    actual override fun hashCode(): Int = uri.hashCode()

    actual override fun toString(): String = uri.toString()
}

internal class FileURLConnection(url: URL) : URLConnection(url) {
    private var file: File? = null

    override fun connect() {
        if (!connected) {
            val path = url?.getPath() ?: throw IOException("Empty file URL path")
            file = File(URLDecoder.decode(path, "UTF-8"))
            connected = true
        }
    }

    override fun getContentLengthLong(): Long {
        connect()
        return file?.length() ?: -1L
    }

    override fun getInputStream(): InputStream {
        connect()
        return FileInputStream(file!!)
    }

    override fun getOutputStream(): OutputStream {
        connect()
        return FileOutputStream(file!!)
    }
}
