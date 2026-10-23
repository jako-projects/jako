package org.json

import kotlin.math.floor

actual open class JSONArray {
    internal val values = ArrayList<Any?>()

    actual constructor()

    actual constructor(copyFrom: Collection<*>?) {
        if (copyFrom != null) {
            for (el in copyFrom) {
                put(JSONObject.wrap(el))
            }
        }
    }

    actual constructor(readFrom: JSONTokener) {
        val obj = readFrom.nextValue()
        if (obj is JSONArray) {
            this.values.addAll(obj.values)
        } else {
            throw JSONException("Value $obj of type ${obj::class.simpleName} cannot be converted to JSONArray")
        }
    }

    actual constructor(json: String) : this(JSONTokener(json))

    actual constructor(array: Any?) {
        when (array) {
            is Array<*> -> for (e in array) put(JSONObject.wrap(e))
            is IntArray -> for (e in array) put(JSONObject.wrap(e))
            is LongArray -> for (e in array) put(JSONObject.wrap(e))
            is DoubleArray -> for (e in array) put(JSONObject.wrap(e))
            is FloatArray -> for (e in array) put(JSONObject.wrap(e))
            is BooleanArray -> for (e in array) put(JSONObject.wrap(e))
            is ByteArray -> for (e in array) put(JSONObject.wrap(e))
            is ShortArray -> for (e in array) put(JSONObject.wrap(e))
            is CharArray -> for (e in array) put(JSONObject.wrap(e))
            else -> throw JSONException("Not a primitive array: ${array?.let { it::class }}")
        }
    }

    actual open fun length(): Int = values.size

    actual open fun put(value: Boolean): JSONArray {
        values.add(value)
        return this
    }

    actual open fun put(value: Double): JSONArray {
        values.add(checkDouble(value))
        return this
    }

    actual open fun put(value: Int): JSONArray {
        values.add(value)
        return this
    }

    actual open fun put(value: Long): JSONArray {
        values.add(value)
        return this
    }

    actual open fun put(value: Any?): JSONArray {
        if (value is Number) {
            checkDouble(value.toDouble())
        }
        values.add(value)
        return this
    }

    actual open fun put(index: Int, value: Boolean): JSONArray = put(index, value as Any?)

    actual open fun put(index: Int, value: Double): JSONArray = put(index, value as Any?)

    actual open fun put(index: Int, value: Int): JSONArray = put(index, value as Any?)

    actual open fun put(index: Int, value: Long): JSONArray = put(index, value as Any?)

    actual open fun put(index: Int, value: Any?): JSONArray {
        if (index < 0) throw JSONException("Index $index < 0")
        if (value is Number) {
            checkDouble(value.toDouble())
        }
        while (values.size <= index) {
            values.add(null)
        }
        values[index] = value
        return this
    }

    actual open fun isNull(index: Int): Boolean {
        val value = opt(index)
        return value == null || value === JSONObject.NULL
    }

    actual open fun get(index: Int): Any {
        if (index < 0 || index >= values.size) {
            throw JSONException("Index $index out of range [0..${values.size})")
        }
        return values[index] ?: JSONObject.NULL
    }

    actual open fun opt(index: Int): Any? {
        if (index < 0 || index >= values.size) return null
        return values[index]
    }

    actual open fun remove(index: Int): Any? {
        if (index < 0 || index >= values.size) return null
        return values.removeAt(index)
    }

    actual open fun getBoolean(index: Int): Boolean {
        val obj = get(index)
        return toBooleanOrNull(obj) ?: throw JSONException("Value $obj at $index cannot be converted to boolean")
    }

    actual open fun optBoolean(index: Int): Boolean = optBoolean(index, false)

    actual open fun optBoolean(index: Int, fallback: Boolean): Boolean {
        val obj = opt(index)
        return toBooleanOrNull(obj) ?: fallback
    }

    actual open fun getDouble(index: Int): Double {
        val obj = get(index)
        return toDoubleOrNull(obj) ?: throw JSONException("Value $obj at $index cannot be converted to double")
    }

    actual open fun optDouble(index: Int): Double = optDouble(index, Double.NaN)

    actual open fun optDouble(index: Int, fallback: Double): Double {
        val obj = opt(index)
        return toDoubleOrNull(obj) ?: fallback
    }

    actual open fun getInt(index: Int): Int {
        val obj = get(index)
        return toIntOrNull(obj) ?: throw JSONException("Value $obj at $index cannot be converted to int")
    }

    actual open fun optInt(index: Int): Int = optInt(index, 0)

    actual open fun optInt(index: Int, fallback: Int): Int {
        val obj = opt(index)
        return toIntOrNull(obj) ?: fallback
    }

    actual open fun getLong(index: Int): Long {
        val obj = get(index)
        return toLongOrNull(obj) ?: throw JSONException("Value $obj at $index cannot be converted to long")
    }

    actual open fun optLong(index: Int): Long = optLong(index, 0L)

    actual open fun optLong(index: Int, fallback: Long): Long {
        val obj = opt(index)
        return toLongOrNull(obj) ?: fallback
    }

    actual open fun getString(index: Int): String {
        val obj = get(index)
        return toStringOrNull(obj) ?: throw JSONException("Value $obj at $index cannot be converted to String")
    }

    actual open fun optString(index: Int): String = optString(index, "") ?: ""

    actual open fun optString(index: Int, fallback: String?): String? {
        val obj = opt(index)
        return toStringOrNull(obj) ?: fallback
    }

    actual open fun getJSONArray(index: Int): JSONArray {
        val obj = get(index)
        if (obj is JSONArray) return obj
        throw JSONException("Value $obj at $index cannot be converted to JSONArray")
    }

    actual open fun optJSONArray(index: Int): JSONArray? = opt(index) as? JSONArray

    actual open fun getJSONObject(index: Int): JSONObject {
        val obj = get(index)
        if (obj is JSONObject) return obj
        throw JSONException("Value $obj at $index cannot be converted to JSONObject")
    }

    actual open fun optJSONObject(index: Int): JSONObject? = opt(index) as? JSONObject

    actual open fun toJSONObject(names: JSONArray?): JSONObject? {
        if (names == null || names.length() == 0 || values.isEmpty()) return null
        val result = JSONObject()
        val len = minOf(names.length(), values.size)
        for (i in 0 until len) {
            result.put(names.optString(i), opt(i))
        }
        return result
    }

    actual open fun join(separator: String): String {
        val stringer = JSONStringer()
        stringer.open(JSONStringer.Scope.NULL, "")
        for (i in values.indices) {
            if (i > 0) {
                stringer.out.append(separator)
            }
            stringer.value(values[i])
        }
        stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "")
        return stringer.out.toString()
    }

    actual override fun toString(): String {
        val stringer = JSONStringer()
        writeTo(stringer)
        return stringer.toString()
    }

    actual open fun toString(indentSpaces: Int): String {
        val stringer = JSONStringer(indentSpaces)
        writeTo(stringer)
        return stringer.toString()
    }

    internal fun writeTo(stringer: JSONStringer) {
        stringer.array()
        for (value in values) {
            stringer.value(value)
        }
        stringer.endArray()
    }

    override fun equals(other: Any?): Boolean {
        return other is JSONArray && other.values == this.values
    }

    override fun hashCode(): Int = values.hashCode()
}
