package org.json

import kotlin.math.floor

actual open class JSONException : Exception {
    actual constructor(s: String?) : super(s)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

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

actual open class JSONTokener actual constructor(
    private val inString: String?
) {
    private var pos = 0

    actual open fun nextValue(): Any {
        val c = nextCleanInternal()
        return when (c) {
            (-1).toChar() -> throw syntaxError("End of input")
            '{'.code.toChar() -> readObject()
            '['.code.toChar() -> readArray()
            '\'', '"' -> nextString(c)
            else -> {
                pos--
                readLiteral()
            }
        }
    }

    private fun nextCleanInternal(): Char {
        while (inString != null && pos < inString.length) {
            val c = inString[pos++]
            when (c) {
                '\t', ' ', '\n', '\r' -> continue
                '/' -> {
                    if (pos == inString.length) return c
                    when (inString[pos]) {
                        '*' -> {
                            pos++
                            val commentEnd = inString.indexOf("*/", pos)
                            if (commentEnd == -1) {
                                throw syntaxError("Unterminated comment")
                            }
                            pos = commentEnd + 2
                            continue
                        }
                        '/' -> {
                            pos++
                            skipToEndOfLine()
                            continue
                        }
                        else -> return c
                    }
                }
                '#' -> {
                    skipToEndOfLine()
                    continue
                }
                else -> return c
            }
        }
        return (-1).toChar()
    }

    private fun skipToEndOfLine() {
        if (inString == null) return
        while (pos < inString.length) {
            val c = inString[pos++]
            if (c == '\r' || c == '\n') break
        }
    }

    actual open fun nextString(quote: Char): String {
        val str = inString ?: throw syntaxError("Unterminated string")
        var builder: StringBuilder? = null
        var start = pos
        while (pos < str.length) {
            val c = str[pos++]
            if (c == quote) {
                return if (builder == null) {
                    str.substring(start, pos - 1)
                } else {
                    builder.append(str, start, pos - 1)
                    builder.toString()
                }
            }
            if (c == '\\') {
                if (pos == str.length) throw syntaxError("Unterminated escape sequence")
                if (builder == null) builder = StringBuilder()
                builder.append(str, start, pos - 1)
                builder.append(readEscapeCharacter())
                start = pos
            }
        }
        throw syntaxError("Unterminated string")
    }

    private fun readEscapeCharacter(): Char {
        val str = inString!!
        return when (val escaped = str[pos++]) {
            'u' -> {
                if (pos + 4 > str.length) throw syntaxError("Unterminated escape sequence")
                val hex = str.substring(pos, pos + 4)
                pos += 4
                try {
                    hex.toInt(16).toChar()
                } catch (_: NumberFormatException) {
                    throw syntaxError("Invalid escape sequence: \\u$hex")
                }
            }
            't' -> '\t'
            'b' -> '\b'
            'n' -> '\n'
            'r' -> '\r'
            'f' -> '\u000c'
            '\'', '"', '\\', '/' -> escaped
            else -> escaped
        }
    }

    private fun readLiteral(): Any {
        val literal = nextToInternal("{}[]/\\:,=;# \t\u000c")
        if (literal.isEmpty()) throw syntaxError("Expected literal value")
        if (literal.equals("null", ignoreCase = true)) return JSONObject.NULL
        if (literal.equals("true", ignoreCase = true)) return true
        if (literal.equals("false", ignoreCase = true)) return false

        if ('.' !in literal && 'e' !in literal && 'E' !in literal) {
            var base = 10
            var number = literal
            if (number.startsWith("0x") || number.startsWith("0X")) {
                number = number.substring(2)
                base = 16
            } else if (number.startsWith("0") && number.length > 1) {
                number = number.substring(1)
                base = 8
            }
            val longValue = number.toLongOrNull(base)
            if (longValue != null) {
                return if (longValue <= Int.MAX_VALUE && longValue >= Int.MIN_VALUE) {
                    longValue.toInt()
                } else {
                    longValue
                }
            }
        }
        val doubleVal = literal.toDoubleOrNull()
        if (doubleVal != null && !doubleVal.isInfinite() && !doubleVal.isNaN()) {
            return doubleVal
        }
        return literal
    }

    private fun nextToInternal(excluded: String): String {
        val str = inString ?: return ""
        val start = pos
        while (pos < str.length) {
            val c = str[pos]
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return str.substring(start, pos)
            }
            pos++
        }
        return str.substring(start)
    }

    private fun readObject(): JSONObject {
        val result = JSONObject()
        val first = nextCleanInternal()
        if (first == '}') return result
        if (first != (-1).toChar()) pos--

        while (true) {
            val name = nextValue()
            if (name !is String) {
                throw syntaxError("Names must be strings, but $name is of type ${name::class.simpleName}")
            }
            val separator = nextCleanInternal()
            if (separator != ':' && separator != '=') {
                throw syntaxError("Expected ':' after $name")
            }
            if (inString != null && pos < inString.length && inString[pos] == '>') {
                pos++
            }
            result.put(name, nextValue())

            when (nextCleanInternal()) {
                '}' -> return result
                ';', ',' -> {
                    if (nextCleanInternal() == '}') return result
                    pos--
                }
                else -> throw syntaxError("Unterminated object")
            }
        }
    }

    private fun readArray(): JSONArray {
        val result = JSONArray()
        var hasTrailingSeparator = false
        while (true) {
            when (val c = nextCleanInternal()) {
                (-1).toChar() -> throw syntaxError("Unterminated array")
                ']' -> {
                    if (hasTrailingSeparator) result.put(null as Any?)
                    return result
                }
                ',', ';' -> {
                    result.put(null as Any?)
                    hasTrailingSeparator = true
                }
                else -> {
                    pos--
                    result.put(nextValue())
                    when (nextCleanInternal()) {
                        ']' -> return result
                        ',', ';' -> hasTrailingSeparator = true
                        else -> throw syntaxError("Unterminated array")
                    }
                }
            }
        }
    }

    actual open fun syntaxError(message: String): JSONException {
        return JSONException(message + this)
    }

    override fun toString(): String {
        return " at character $pos of $inString"
    }

    actual open fun more(): Boolean = inString != null && pos < inString.length

    actual open fun next(): Char {
        return if (inString != null && pos < inString.length) inString[pos++] else '\u0000'
    }

    actual open fun next(c: Char): Char {
        val result = next()
        if (result != c) throw syntaxError("Expected $c but was $result")
        return result
    }

    actual open fun nextClean(): Char {
        val nextCleanInt = nextCleanInternal()
        return if (nextCleanInt == (-1).toChar()) '\u0000' else nextCleanInt
    }

    actual open fun next(length: Int): String {
        if (inString == null || pos + length > inString.length) {
            throw syntaxError("$length is out of bounds")
        }
        val result = inString.substring(pos, pos + length)
        pos += length
        return result
    }

    actual open fun nextTo(excluded: String): String = nextToInternal(excluded).trim()

    actual open fun nextTo(excluded: Char): String = nextToInternal(excluded.toString()).trim()

    actual open fun skipPast(thru: String) {
        val str = inString ?: return
        val thruStart = str.indexOf(thru, pos)
        pos = if (thruStart == -1) str.length else thruStart + thru.length
    }

    actual open fun skipTo(to: Char): Char {
        val str = inString ?: return '\u0000'
        val index = str.indexOf(to, pos)
        return if (index != -1) {
            pos = index
            to
        } else {
            '\u0000'
        }
    }

    actual open fun back() {
        if (--pos == -1) pos = 0
    }

    actual companion object {
        actual fun dehexchar(hex: Char): Int = when (hex) {
            in '0'..'9' -> hex - '0'
            in 'A'..'F' -> hex - 'A' + 10
            in 'a'..'f' -> hex - 'a' + 10
            else -> -1
        }
    }
}

actual open class JSONStringer {
    internal enum class Scope {
        EMPTY_ARRAY,
        NONEMPTY_ARRAY,
        EMPTY_OBJECT,
        DANGLING_KEY,
        NONEMPTY_OBJECT,
        NULL
    }

    internal val out = StringBuilder()
    private val stack = ArrayList<Scope>()
    private val indent: String?

    actual constructor() {
        indent = null
    }

    internal constructor(indentSpaces: Int) {
        indent = " ".repeat(maxOf(0, indentSpaces))
    }

    actual open fun array(): JSONStringer = open(Scope.EMPTY_ARRAY, "[")

    actual open fun endArray(): JSONStringer = close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]")

    actual open fun `object`(): JSONStringer = open(Scope.EMPTY_OBJECT, "{")

    actual open fun endObject(): JSONStringer = close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}")

    internal fun open(empty: Scope, openBracket: String): JSONStringer {
        if (stack.isEmpty() && out.isNotEmpty()) {
            throw JSONException("Nesting problem: multiple top-level roots")
        }
        beforeValue()
        stack.add(empty)
        out.append(openBracket)
        return this
    }

    internal fun close(empty: Scope, nonempty: Scope, closeBracket: String): JSONStringer {
        val context = peek()
        if (context != nonempty && context != empty) {
            throw JSONException("Nesting problem")
        }
        stack.removeAt(stack.size - 1)
        if (context == nonempty) {
            newline()
        }
        out.append(closeBracket)
        return this
    }

    private fun peek(): Scope {
        if (stack.isEmpty()) throw JSONException("Nesting problem")
        return stack[stack.size - 1]
    }

    private fun replaceTop(topOfStack: Scope) {
        stack[stack.size - 1] = topOfStack
    }

    actual open fun value(value: Any?): JSONStringer {
        if (stack.isEmpty()) throw JSONException("Nesting problem")
        if (value is JSONArray) {
            value.writeTo(this)
            return this
        } else if (value is JSONObject) {
            value.writeTo(this)
            return this
        }
        beforeValue()
        when {
            value == null || value is Boolean || value === JSONObject.NULL -> out.append(value)
            value is Number -> out.append(JSONObject.numberToString(value))
            else -> string(value.toString())
        }
        return this
    }

    actual open fun value(value: Boolean): JSONStringer {
        if (stack.isEmpty()) throw JSONException("Nesting problem")
        beforeValue()
        out.append(value)
        return this
    }

    actual open fun value(value: Double): JSONStringer {
        if (stack.isEmpty()) throw JSONException("Nesting problem")
        beforeValue()
        out.append(JSONObject.numberToString(value))
        return this
    }

    actual open fun value(value: Long): JSONStringer {
        if (stack.isEmpty()) throw JSONException("Nesting problem")
        beforeValue()
        out.append(value)
        return this
    }

    private fun string(value: String) {
        out.append("\"")
        for (i in value.indices) {
            when (val c = value[i]) {
                '"', '\\', '/' -> out.append('\\').append(c)
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\u000c' -> out.append("\\f")
                else -> {
                    if (c.code <= 0x1F) {
                        out.append("\\u").append(c.code.toString(16).padStart(4, '0'))
                    } else {
                        out.append(c)
                    }
                }
            }
        }
        out.append("\"")
    }

    private fun newline() {
        if (indent == null) return
        out.append("\n")
        for (i in 0 until stack.size) {
            out.append(indent)
        }
    }

    actual open fun key(name: String): JSONStringer {
        beforeKey()
        string(name)
        return this
    }

    private fun beforeKey() {
        val context = peek()
        if (context == Scope.NONEMPTY_OBJECT) {
            out.append(',')
        } else if (context != Scope.EMPTY_OBJECT) {
            throw JSONException("Nesting problem")
        }
        newline()
        replaceTop(Scope.DANGLING_KEY)
    }

    private fun beforeValue() {
        if (stack.isEmpty()) return
        when (val context = peek()) {
            Scope.EMPTY_ARRAY -> {
                replaceTop(Scope.NONEMPTY_ARRAY)
                newline()
            }
            Scope.NONEMPTY_ARRAY -> {
                out.append(',')
                newline()
            }
            Scope.DANGLING_KEY -> {
                out.append(if (indent == null) ":" else ": ")
                replaceTop(Scope.NONEMPTY_OBJECT)
            }
            Scope.NULL -> {}
            else -> throw JSONException("Nesting problem")
        }
    }

    actual override fun toString(): String {
        return if (out.isEmpty()) "" else out.toString()
    }
}
