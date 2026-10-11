@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.Serializable

expect class URI : Serializable, Comparable<URI> {
    constructor(str: String)
    constructor(
        scheme: String?,
        authority: String?,
        path: String?,
        query: String?,
        fragment: String?
    )

    fun getScheme(): String?
    fun getAuthority(): String?
    fun getPath(): String?
    fun getQuery(): String?
    fun getFragment(): String?
    fun getHost(): String?
    fun getPort(): Int

    val scheme: String?
    val authority: String?
    val path: String?
    val query: String?
    val fragment: String?
    val host: String?
    val port: Int

    fun isAbsolute(): Boolean
    fun isOpaque(): Boolean
    fun resolve(str: String): URI
    fun resolve(uri: URI): URI
    fun toURL(): URL

    override fun compareTo(other: URI): Int
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        fun create(str: String): URI
    }
}
