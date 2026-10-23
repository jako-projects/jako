package org.json

import kotlin.math.floor

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
