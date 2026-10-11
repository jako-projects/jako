@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.InputStream
import java.io.Serializable

expect open class URL : Serializable {
    constructor(spec: String?)
    constructor(protocol: String, host: String, port: Int, file: String)
    constructor(protocol: String, host: String, file: String)

    open fun getProtocol(): String?
    open fun getHost(): String?
    open fun getPort(): Int
    open fun getPath(): String?

    open val protocol: String?
    open val host: String?
    open val port: Int
    open val path: String?

    fun getFile(): String
    fun getQuery(): String?
    fun openStream(): InputStream
    fun toURI(): URI

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
