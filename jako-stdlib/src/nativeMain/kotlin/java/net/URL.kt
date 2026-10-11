package java.net

import java.io.InputStream
import java.io.Serializable

actual open class URL : Serializable {
    private val uri: URI

    actual constructor(spec: String?) {
        this.uri = URI(spec ?: "")
    }

    actual constructor(protocol: String, host: String, port: Int, file: String) :
            this("$protocol://$host${if (port != -1) ":$port" else ""}$file")

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

    actual fun openStream(): InputStream {
        throw java.io.IOException("Network streams not implemented for URL: $uri")
    }

    actual fun toURI(): URI = uri

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is URL) return false
        return uri == other.uri
    }

    actual override fun hashCode(): Int = uri.hashCode()

    actual override fun toString(): String = uri.toString()
}
