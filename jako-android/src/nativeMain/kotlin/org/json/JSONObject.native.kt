package org.json

import kotlin.math.floor

internal fun checkDouble(d: Double): Double {
    if (d.isInfinite() || d.isNaN()) {
        throw JSONException("Forbidden numeric value: $d")
    }
    return d
}

internal fun toBooleanOrNull(value: Any?): Boolean? = when (value) {
    is Boolean -> value
    is String -> when {
        value.equals("true", ignoreCase = true) -> true
        value.equals("false", ignoreCase = true) -> false
        else -> null
    }
    else -> null
}

internal fun toDoubleOrNull(value: Any?): Double? = when (value) {
    is Double -> value
    is Number -> value.toDouble()
    is String -> value.toDoubleOrNull()
    else -> null
}

internal fun toLongOrNull(value: Any?): Long? = when (value) {
    is Long -> value
    is Number -> value.toLong()
    is String -> value.toLongOrNull() ?: value.toDoubleOrNull()?.toLong()
    else -> null
}

internal fun toIntOrNull(value: Any?): Int? = when (value) {
    is Int -> value
    is Number -> value.toInt()
    is String -> value.toIntOrNull() ?: value.toDoubleOrNull()?.toInt()
    else -> null
}

internal fun toStringOrNull(value: Any?): String? = when {
    value == null || value === JSONObject.NULL -> null
    value is Boolean || value is Number || value is String || value is JSONObject || value is JSONArray -> value.toString()
    else -> value.toString()
}

actual open class JSONObject {
    internal val nameValuePairs = LinkedHashMap<String, Any>()

    actual constructor()

    actual constructor(copyFrom: Map<*, *>) {
        for ((k, v) in copyFrom) {
            if (k == null) throw NullPointerException("key == null")
            val wrapped = wrap(v)
            if (wrapped != null) {
                nameValuePairs[k.toString()] = wrapped
            }
        }
    }

    actual constructor(readFrom: JSONTokener) {
        val obj = readFrom.nextValue()
        if (obj is JSONObject) {
            this.nameValuePairs.putAll(obj.nameValuePairs)
        } else {
            throw JSONException("Value $obj of type ${obj::class.simpleName} cannot be converted to JSONObject")
        }
    }

    actual constructor(json: String) : this(JSONTokener(json))

    actual constructor(copyFrom: JSONObject, names: Array<String>) {
        for (name in names) {
            val value = copyFrom.opt(name)
            if (value != null) {
                nameValuePairs[name] = value
            }
        }
    }

    actual open fun length(): Int = nameValuePairs.size

    actual open fun put(name: String, value: Boolean): JSONObject {
        nameValuePairs[name] = value
        return this
    }

    actual open fun put(name: String, value: Double): JSONObject {
        nameValuePairs[name] = checkDouble(value)
        return this
    }

    actual open fun put(name: String, value: Int): JSONObject {
        nameValuePairs[name] = value
        return this
    }

    actual open fun put(name: String, value: Long): JSONObject {
        nameValuePairs[name] = value
        return this
    }

    actual open fun put(name: String, value: Any?): JSONObject {
        if (value == null) {
            nameValuePairs.remove(name)
            return this
        }
        if (value is Number) {
            checkDouble(value.toDouble())
        }
        nameValuePairs[name] = value
        return this
    }

    actual open fun putOpt(name: String?, value: Any?): JSONObject {
        if (name == null || value == null) return this
        return put(name, value)
    }

    actual open fun accumulate(name: String, value: Any?): JSONObject {
        if (value == null) throw NullPointerException("value == null")
        val current = nameValuePairs[name]
        if (current == null) {
            return put(name, value)
        }
        if (value is Number) {
            checkDouble(value.toDouble())
        }
        if (current is JSONArray) {
            current.put(value)
        } else {
            val array = JSONArray()
            array.put(current)
            array.put(value)
            nameValuePairs[name] = array
        }
        return this
    }

    actual open fun remove(name: String?): Any? {
        if (name == null) return null
        return nameValuePairs.remove(name)
    }

    actual open fun isNull(name: String?): Boolean {
        if (name == null) return true
        val value = nameValuePairs[name]
        return value == null || value === NULL
    }

    actual open fun has(name: String?): Boolean {
        if (name == null) return false
        return nameValuePairs.containsKey(name)
    }

    actual open fun get(name: String): Any {
        return nameValuePairs[name] ?: throw JSONException("No value for $name")
    }

    actual open fun opt(name: String?): Any? {
        if (name == null) return null
        return nameValuePairs[name]
    }

    actual open fun getBoolean(name: String): Boolean {
        val obj = get(name)
        return toBooleanOrNull(obj) ?: throw JSONException("Value $obj at $name cannot be converted to boolean")
    }

    actual open fun optBoolean(name: String?): Boolean = optBoolean(name, false)

    actual open fun optBoolean(name: String?, fallback: Boolean): Boolean {
        val obj = opt(name)
        return toBooleanOrNull(obj) ?: fallback
    }

    actual open fun getDouble(name: String): Double {
        val obj = get(name)
        return toDoubleOrNull(obj) ?: throw JSONException("Value $obj at $name cannot be converted to double")
    }

    actual open fun optDouble(name: String?): Double = optDouble(name, Double.NaN)

    actual open fun optDouble(name: String?, fallback: Double): Double {
        val obj = opt(name)
        return toDoubleOrNull(obj) ?: fallback
    }

    actual open fun getInt(name: String): Int {
        val obj = get(name)
        return toIntOrNull(obj) ?: throw JSONException("Value $obj at $name cannot be converted to int")
    }

    actual open fun optInt(name: String?): Int = optInt(name, 0)

    actual open fun optInt(name: String?, fallback: Int): Int {
        val obj = opt(name)
        return toIntOrNull(obj) ?: fallback
    }

    actual open fun getLong(name: String): Long {
        val obj = get(name)
        return toLongOrNull(obj) ?: throw JSONException("Value $obj at $name cannot be converted to long")
    }

    actual open fun optLong(name: String?): Long = optLong(name, 0L)

    actual open fun optLong(name: String?, fallback: Long): Long {
        val obj = opt(name)
        return toLongOrNull(obj) ?: fallback
    }

    actual open fun getString(name: String): String {
        val obj = get(name)
        return toStringOrNull(obj) ?: throw JSONException("Value $obj at $name of type null cannot be converted to String")
    }

    actual open fun optString(name: String?): String = optString(name, "") ?: ""

    actual open fun optString(name: String?, fallback: String?): String? {
        val obj = opt(name)
        return toStringOrNull(obj) ?: fallback
    }

    actual open fun getJSONArray(name: String): JSONArray {
        val obj = get(name)
        if (obj is JSONArray) return obj
        throw JSONException("Value $obj at $name cannot be converted to JSONArray")
    }

    actual open fun optJSONArray(name: String?): JSONArray? {
        return opt(name) as? JSONArray
    }

    actual open fun getJSONObject(name: String): JSONObject {
        val obj = get(name)
        if (obj is JSONObject) return obj
        throw JSONException("Value $obj at $name cannot be converted to JSONObject")
    }

    actual open fun optJSONObject(name: String?): JSONObject? {
        return opt(name) as? JSONObject
    }

    actual open fun toJSONArray(names: JSONArray?): JSONArray? {
        if (names == null || names.length() == 0) return null
        val result = JSONArray()
        for (i in 0 until names.length()) {
            val name = names.optString(i, null)
            result.put(opt(name))
        }
        return result
    }

    actual open fun keys(): MutableIterator<String> = nameValuePairs.keys.iterator()

    actual open fun names(): JSONArray? {
        if (nameValuePairs.isEmpty()) return null
        return JSONArray(ArrayList(nameValuePairs.keys))
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
        stringer.`object`()
        for ((k, v) in nameValuePairs) {
            stringer.key(k).value(v)
        }
        stringer.endObject()
    }

    actual companion object {
        actual val NULL: Any = object {
            override fun equals(other: Any?): Boolean = other === this || other == null
            override fun hashCode(): Int = 0
            override fun toString(): String = "null"
        }

        actual fun quote(data: String?): String {
            if (data == null) return "\"\""
            val out = JSONStringer()
            out.open(JSONStringer.Scope.EMPTY_ARRAY, "")
            out.value(data)
            out.close(JSONStringer.Scope.EMPTY_ARRAY, JSONStringer.Scope.NONEMPTY_ARRAY, "")
            return out.toString()
        }

        actual fun wrap(o: Any?): Any? {
            if (o == null) return NULL
            if (o is JSONArray || o is JSONObject || o === NULL) return o
            try {
                when (o) {
                    is Collection<*> -> return JSONArray(o)
                    is Array<*> -> return JSONArray(o)
                    is IntArray, is LongArray, is DoubleArray, is FloatArray, is BooleanArray, is ByteArray, is ShortArray, is CharArray -> return JSONArray(o)
                    is Map<*, *> -> return JSONObject(o)
                    is Boolean, is Byte, is Char, is Double, is Float, is Int, is Long, is Short, is String -> return o
                    else -> return o.toString()
                }
            } catch (_: Exception) {
                return null
            }
        }

        actual fun numberToString(number: Number): String {
            val d = number.toDouble()
            checkDouble(d)
            if (number is Double || number is Float) {
                if (d == -0.0) return "-0"
                val longVal = d.toLong()
                if (d == longVal.toDouble() && d >= Long.MIN_VALUE.toDouble() && d <= Long.MAX_VALUE.toDouble()) {
                    return longVal.toString()
                }
            }
            return number.toString()
        }
    }
}
