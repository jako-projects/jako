@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.net

import android.os.Parcelable
import java.io.File

/**
 * Immutable URI reference. A URI reference includes a URI and a fragment, the
 * component of the URI following a '#'. Builds and parses URI references
 * which conform to RFC 2396.
 */
expect abstract class Uri : Parcelable, Comparable<Uri> {
    abstract val isHierarchical: Boolean
    open val isOpaque: Boolean
    abstract val isRelative: Boolean
    open val isAbsolute: Boolean
    abstract val scheme: String?
    abstract val schemeSpecificPart: String
    abstract val encodedSchemeSpecificPart: String
    abstract val authority: String?
    abstract val encodedAuthority: String?
    abstract val userInfo: String?
    abstract val encodedUserInfo: String?
    abstract val host: String?
    abstract val port: Int
    abstract val path: String?
    abstract val encodedPath: String?
    abstract val query: String?
    abstract val encodedQuery: String?
    abstract val fragment: String?
    abstract val encodedFragment: String?
    abstract val pathSegments: List<String>
    abstract val lastPathSegment: String?
    open val queryParameterNames: Set<String>

    abstract fun isHierarchical(): Boolean
    open fun isOpaque(): Boolean
    abstract fun isRelative(): Boolean
    open fun isAbsolute(): Boolean
    abstract fun getScheme(): String?
    abstract fun getSchemeSpecificPart(): String
    abstract fun getEncodedSchemeSpecificPart(): String
    abstract fun getAuthority(): String?
    abstract fun getEncodedAuthority(): String?
    abstract fun getUserInfo(): String?
    abstract fun getEncodedUserInfo(): String?
    abstract fun getHost(): String?
    abstract fun getPort(): Int
    abstract fun getPath(): String?
    abstract fun getEncodedPath(): String?
    abstract fun getQuery(): String?
    abstract fun getEncodedQuery(): String?
    abstract fun getFragment(): String?
    abstract fun getEncodedFragment(): String?
    abstract fun getPathSegments(): List<String>
    abstract fun getLastPathSegment(): String?
    open fun getQueryParameterNames(): Set<String>
    open fun getQueryParameters(key: String): List<String>
    open fun getQueryParameter(key: String): String?
    open fun getBooleanQueryParameter(key: String, defaultValue: Boolean): Boolean
    open fun normalizeScheme(): Uri
    abstract fun buildUpon(): Builder

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun compareTo(other: Uri): Int
    abstract override fun toString(): String

    final class Builder() {
        fun scheme(scheme: String?): Builder
        fun opaquePart(opaquePart: String?): Builder
        fun encodedOpaquePart(opaquePart: String?): Builder
        fun authority(authority: String?): Builder
        fun encodedAuthority(authority: String?): Builder
        fun path(path: String?): Builder
        fun encodedPath(path: String?): Builder
        fun appendPath(newSegment: String): Builder
        fun appendEncodedPath(newSegment: String): Builder
        fun query(query: String?): Builder
        fun encodedQuery(query: String?): Builder
        fun fragment(fragment: String?): Builder
        fun encodedFragment(fragment: String?): Builder
        fun appendQueryParameter(key: String, value: String?): Builder
        fun clearQuery(): Builder
        fun build(): Uri
        override fun toString(): String
    }

    companion object {
        val EMPTY: Uri

        fun parse(uriString: String): Uri
        fun fromFile(file: File): Uri
        fun fromParts(scheme: String, ssp: String, fragment: String?): Uri
        fun encode(s: String?): String?
        fun encode(s: String?, allow: String?): String?
        fun decode(s: String?): String?
        fun withAppendedPath(baseUri: Uri, pathSegment: String): Uri
    }
}
