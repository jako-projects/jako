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

/**
 * [Nullability Decision] JSONObject methods follow AOSP org.json.JSONObject contracts:
 * - get*(name) throws JSONException if missing or unconvertible, returns non-null.
 * - opt*(name) returns default value or null (opt, optJSONArray, optJSONObject).
 * - put(name, value: Any?) removes the mapping when value is null; use JSONObject.NULL for explicit JSON null.
 */
expect open class JSONObject {
    constructor()
    constructor(copyFrom: Map<*, *>)
    constructor(readFrom: JSONTokener)
    constructor(json: String)
    constructor(copyFrom: JSONObject, names: Array<String>)

    open fun length(): Int
    open fun put(name: String, value: Boolean): JSONObject
    open fun put(name: String, value: Double): JSONObject
    open fun put(name: String, value: Int): JSONObject
    open fun put(name: String, value: Long): JSONObject
    open fun put(name: String, value: Any?): JSONObject
    open fun putOpt(name: String?, value: Any?): JSONObject
    open fun accumulate(name: String, value: Any?): JSONObject
    open fun remove(name: String?): Any?
    open fun isNull(name: String?): Boolean
    open fun has(name: String?): Boolean
    open fun get(name: String): Any
    open fun opt(name: String?): Any?
    open fun getBoolean(name: String): Boolean
    open fun optBoolean(name: String?): Boolean
    open fun optBoolean(name: String?, fallback: Boolean): Boolean
    open fun getDouble(name: String): Double
    open fun optDouble(name: String?): Double
    open fun optDouble(name: String?, fallback: Double): Double
    open fun getInt(name: String): Int
    open fun optInt(name: String?): Int
    open fun optInt(name: String?, fallback: Int): Int
    open fun getLong(name: String): Long
    open fun optLong(name: String?): Long
    open fun optLong(name: String?, fallback: Long): Long
    open fun getString(name: String): String
    open fun optString(name: String?): String
    open fun optString(name: String?, fallback: String?): String?
    open fun getJSONArray(name: String): JSONArray
    open fun optJSONArray(name: String?): JSONArray?
    open fun getJSONObject(name: String): JSONObject
    open fun optJSONObject(name: String?): JSONObject?
    open fun toJSONArray(names: JSONArray?): JSONArray?
    open fun keys(): MutableIterator<String>
    open fun names(): JSONArray?
    override fun toString(): String
    open fun toString(indentSpaces: Int): String

    companion object {
        val NULL: Any
        fun quote(data: String?): String
        fun wrap(o: Any?): Any?
        fun numberToString(number: Number): String
    }
}

/**
 * [Nullability Decision] JSONArray methods follow AOSP org.json.JSONArray contracts:
 * - get*(index) throws JSONException if out of bounds or unconvertible, returns non-null.
 * - opt*(index) returns default value or null when out of bounds.
 */
expect open class JSONArray {
    constructor()
    constructor(copyFrom: Collection<*>?)
    constructor(readFrom: JSONTokener)
    constructor(json: String)
    constructor(array: Any?)

    open fun length(): Int
    open fun put(value: Boolean): JSONArray
    open fun put(value: Double): JSONArray
    open fun put(value: Int): JSONArray
    open fun put(value: Long): JSONArray
    open fun put(value: Any?): JSONArray
    open fun put(index: Int, value: Boolean): JSONArray
    open fun put(index: Int, value: Double): JSONArray
    open fun put(index: Int, value: Int): JSONArray
    open fun put(index: Int, value: Long): JSONArray
    open fun put(index: Int, value: Any?): JSONArray
    open fun isNull(index: Int): Boolean
    open fun get(index: Int): Any
    open fun opt(index: Int): Any?
    open fun remove(index: Int): Any?
    open fun getBoolean(index: Int): Boolean
    open fun optBoolean(index: Int): Boolean
    open fun optBoolean(index: Int, fallback: Boolean): Boolean
    open fun getDouble(index: Int): Double
    open fun optDouble(index: Int): Double
    open fun optDouble(index: Int, fallback: Double): Double
    open fun getInt(index: Int): Int
    open fun optInt(index: Int): Int
    open fun optInt(index: Int, fallback: Int): Int
    open fun getLong(index: Int): Long
    open fun optLong(index: Int): Long
    open fun optLong(index: Int, fallback: Long): Long
    open fun getString(index: Int): String
    open fun optString(index: Int): String
    open fun optString(index: Int, fallback: String?): String?
    open fun getJSONArray(index: Int): JSONArray
    open fun optJSONArray(index: Int): JSONArray?
    open fun getJSONObject(index: Int): JSONObject
    open fun optJSONObject(index: Int): JSONObject?
    open fun toJSONObject(names: JSONArray?): JSONObject?
    open fun join(separator: String): String
    override fun toString(): String
    open fun toString(indentSpaces: Int): String
}

/**
 * [Nullability Decision] JSONTokener(inString: String?) accepts nullable String in AOSP.
 */
expect open class JSONTokener {
    constructor(inString: String?)

    open fun nextValue(): Any
    open fun nextString(quote: Char): String
    open fun syntaxError(message: String): JSONException
    open fun more(): Boolean
    open fun next(): Char
    open fun next(c: Char): Char
    open fun next(length: Int): String
    open fun nextClean(): Char
    open fun nextTo(excluded: String): String
    open fun nextTo(excluded: Char): String
    open fun skipPast(thru: String)
    open fun skipTo(to: Char): Char
    open fun back()

    companion object {
        fun dehexchar(hex: Char): Int
    }
}

/**
 * [Nullability Decision] JSONStringer.toString() returns String? (null if no elements written yet in AOSP).
 */
expect open class JSONStringer {
    constructor()

    open fun array(): JSONStringer
    open fun endArray(): JSONStringer
    open fun `object`(): JSONStringer
    open fun endObject(): JSONStringer
    open fun value(value: Any?): JSONStringer
    open fun value(value: Boolean): JSONStringer
    open fun value(value: Double): JSONStringer
    open fun value(value: Long): JSONStringer
    open fun key(name: String): JSONStringer
    override fun toString(): String
}
