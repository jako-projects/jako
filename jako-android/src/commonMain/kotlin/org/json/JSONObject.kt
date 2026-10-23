@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.json

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
