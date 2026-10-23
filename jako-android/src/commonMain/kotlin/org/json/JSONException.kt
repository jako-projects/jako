@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.json

/**
 * [Nullability Decision] JSONException constructors accept nullable String? and Throwable?,
 * matching AOSP org.json.JSONException.
 */
expect open class JSONException : Exception {
    constructor(s: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
