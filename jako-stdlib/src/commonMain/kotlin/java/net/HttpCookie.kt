@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.lang.Cloneable

expect class HttpCookie : Cloneable {
    constructor(name: String, value: String?)

    fun hasExpired(): Boolean
    fun setComment(purpose: String?)
    fun getComment(): String?
    var comment: String?
    fun setCommentURL(purpose: String?)
    fun getCommentURL(): String?
    var commentURL: String?
    fun setDiscard(discard: Boolean)
    fun getDiscard(): Boolean
    var discard: Boolean
    fun setPortlist(ports: String?)
    fun getPortlist(): String?
    var portlist: String?
    fun setDomain(pattern: String?)
    fun getDomain(): String?
    var domain: String?
    fun setMaxAge(expiry: Long)
    fun getMaxAge(): Long
    var maxAge: Long
    fun setPath(uri: String?)
    fun getPath(): String?
    var path: String?
    fun setSecure(flag: Boolean)
    fun getSecure(): Boolean
    var secure: Boolean
    fun getName(): String
    val name: String
    fun setValue(newValue: String?)
    fun getValue(): String?
    var value: String?
    fun getVersion(): Int
    fun setVersion(v: Int)
    var version: Int
    fun isHttpOnly(): Boolean
    fun setHttpOnly(httpOnly: Boolean)
    var isHttpOnly: Boolean
    fun clone(): Any
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun parse(header: String): List<HttpCookie>
        fun domainMatches(domain: String, host: String): Boolean
    }
}
