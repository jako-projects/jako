@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.json

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
