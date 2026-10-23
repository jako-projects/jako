package android.util

import java.io.Closeable
import java.io.IOException
import java.io.Writer

actual open class JsonWriter actual constructor(
    private val out: Writer
) : Closeable {
    private enum class Scope {
        EMPTY_ARRAY,
        NONEMPTY_ARRAY,
        EMPTY_OBJECT,
        DANGLING_NAME,
        NONEMPTY_OBJECT,
        EMPTY_DOCUMENT,
        NONEMPTY_DOCUMENT,
        CLOSED
    }

    private val stack = ArrayList<Scope>().apply { add(Scope.EMPTY_DOCUMENT) }
    private var indent: String? = null
    private var separator = ":"
    private var lenient = false

    actual open var isLenient: Boolean
        get() = lenient
        set(value) { lenient = value }

    actual open fun setLenient(lenient: Boolean) {
        this.lenient = lenient
    }

    actual open fun isLenient(): Boolean = lenient

    actual open fun setIndent(indent: String) {
        if (indent.isEmpty()) {
            this.indent = null
            this.separator = ":"
        } else {
            this.indent = indent
            this.separator = ": "
        }
    }

    actual open fun beginArray(): JsonWriter = open(Scope.EMPTY_ARRAY, "[")

    actual open fun endArray(): JsonWriter = close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]")

    actual open fun beginObject(): JsonWriter = open(Scope.EMPTY_OBJECT, "{")

    actual open fun endObject(): JsonWriter = close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}")

    private fun open(empty: Scope, openBracket: String): JsonWriter {
        beforeValue(true)
        stack.add(empty)
        out.write(openBracket)
        return this
    }

    private fun close(empty: Scope, nonempty: Scope, closeBracket: String): JsonWriter {
        val context = peek()
        if (context != nonempty && context != empty) {
            throw IllegalStateException("Nesting problem: $stack")
        }
        stack.removeAt(stack.size - 1)
        if (context == nonempty) {
            newline()
        }
        out.write(closeBracket)
        return this
    }

    private fun peek(): Scope = stack[stack.size - 1]

    private fun replaceTop(topOfStack: Scope) {
        stack[stack.size - 1] = topOfStack
    }

    actual open fun name(name: String): JsonWriter {
        beforeName()
        string(name)
        return this
    }

    actual open fun value(value: String?): JsonWriter {
        if (value == null) return nullValue()
        beforeValue(false)
        string(value)
        return this
    }

    actual open fun nullValue(): JsonWriter {
        beforeValue(false)
        out.write("null")
        return this
    }

    actual open fun value(value: Boolean): JsonWriter {
        beforeValue(false)
        out.write(if (value) "true" else "false")
        return this
    }

    actual open fun value(value: Double): JsonWriter {
        if (!lenient && (value.isNaN() || value.isInfinite())) {
            throw IllegalArgumentException("Numeric values must be finite, but was $value")
        }
        beforeValue(false)
        out.write(value.toString())
        return this
    }

    actual open fun value(value: Long): JsonWriter {
        beforeValue(false)
        out.write(value.toString())
        return this
    }

    actual open fun value(value: Number?): JsonWriter {
        if (value == null) return nullValue()
        val string = value.toString()
        if (!lenient && (string == "-Infinity" || string == "Infinity" || string == "NaN")) {
            throw IllegalArgumentException("Numeric values must be finite, but was $value")
        }
        beforeValue(false)
        out.write(string)
        return this
    }

    actual open fun flush() {
        out.flush()
    }

    actual override fun close() {
        out.close()
        val size = stack.size
        if (size > 1 || (size == 1 && stack[0] != Scope.NONEMPTY_DOCUMENT)) {
            throw IOException("Incomplete document")
        }
        stack.clear()
    }

    private fun string(value: String) {
        out.write("\"")
        for (i in value.indices) {
            when (val c = value[i]) {
                '"', '\\' -> {
                    out.write("\\")
                    out.write(c.code)
                }
                '\t' -> out.write("\\t")
                '\b' -> out.write("\\b")
                '\n' -> out.write("\\n")
                '\r' -> out.write("\\r")
                '\u000c' -> out.write("\\f")
                '\u2028' -> out.write("\\u2028")
                '\u2029' -> out.write("\\u2029")
                else -> {
                    if (c.code <= 0x1F) {
                        out.write("\\u" + c.code.toString(16).padStart(4, '0'))
                    } else {
                        out.write(c.code)
                    }
                }
            }
        }
        out.write("\"")
    }

    private fun newline() {
        val ind = indent ?: return
        out.write("\n")
        for (i in 1 until stack.size) {
            out.write(ind)
        }
    }

    private fun beforeName() {
        val context = peek()
        if (context == Scope.NONEMPTY_OBJECT) {
            out.write(','.code)
        } else if (context != Scope.EMPTY_OBJECT) {
            throw IllegalStateException("Nesting problem: $stack")
        }
        newline()
        replaceTop(Scope.DANGLING_NAME)
    }

    private fun beforeValue(root: Boolean) {
        when (peek()) {
            Scope.EMPTY_DOCUMENT -> {
                if (!lenient && !root) {
                    throw IllegalStateException("JSON must start with an array or an object.")
                }
                replaceTop(Scope.NONEMPTY_DOCUMENT)
            }
            Scope.EMPTY_ARRAY -> {
                replaceTop(Scope.NONEMPTY_ARRAY)
                newline()
            }
            Scope.NONEMPTY_ARRAY -> {
                out.write(','.code)
                newline()
            }
            Scope.DANGLING_NAME -> {
                out.write(separator)
                replaceTop(Scope.NONEMPTY_OBJECT)
            }
            Scope.NONEMPTY_DOCUMENT -> throw IllegalStateException("JSON must have only one top-level value.")
            else -> throw IllegalStateException("Nesting problem: $stack")
        }
    }
}
