package android.util

import java.io.Closeable
import java.io.EOFException
import java.io.IOException
import java.io.Reader
import java.io.Writer

actual open class MalformedJsonException actual constructor(
    message: String?
) : IOException(message)

actual enum class JsonToken {
    BEGIN_ARRAY,
    END_ARRAY,
    BEGIN_OBJECT,
    END_OBJECT,
    NAME,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    END_DOCUMENT
}

actual open class JsonReader actual constructor(
    private val inReader: Reader
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

    private var lenient = false
    private val buffer = CharArray(1024)
    private var pos = 0
    private var limit = 0
    private val stack = ArrayList<Scope>().apply { add(Scope.EMPTY_DOCUMENT) }
    private var token: JsonToken? = null
    private var name: String? = null
    private var value: String? = null
    private var skipping = false

    actual open var isLenient: Boolean
        get() = lenient
        set(value) { lenient = value }

    actual open fun setLenient(lenient: Boolean) {
        this.lenient = lenient
    }

    actual open fun isLenient(): Boolean = lenient

    actual open fun beginArray() {
        expect(JsonToken.BEGIN_ARRAY)
    }

    actual open fun endArray() {
        expect(JsonToken.END_ARRAY)
    }

    actual open fun beginObject() {
        expect(JsonToken.BEGIN_OBJECT)
    }

    actual open fun endObject() {
        expect(JsonToken.END_OBJECT)
    }

    private fun expect(expected: JsonToken) {
        peek()
        if (token != expected) {
            throw IllegalStateException("Expected $expected but was ${peek()}")
        }
        advance()
    }

    actual open fun hasNext(): Boolean {
        peek()
        return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY
    }

    actual open fun peek(): JsonToken {
        token?.let { return it }
        when (peekStack()) {
            Scope.EMPTY_DOCUMENT -> {
                replaceTop(Scope.NONEMPTY_DOCUMENT)
                val firstToken = nextValue()
                if (!lenient && token != JsonToken.BEGIN_ARRAY && token != JsonToken.BEGIN_OBJECT) {
                    throw IOException("Expected JSON document to start with '[' or '{' but was $token")
                }
                return firstToken
            }
            Scope.EMPTY_ARRAY -> return nextInArray(true)
            Scope.NONEMPTY_ARRAY -> return nextInArray(false)
            Scope.EMPTY_OBJECT -> return nextInObject(true)
            Scope.DANGLING_NAME -> return objectValue()
            Scope.NONEMPTY_OBJECT -> return nextInObject(false)
            Scope.NONEMPTY_DOCUMENT -> {
                try {
                    val t = nextValue()
                    if (lenient) return t
                    throw syntaxError("Expected EOF")
                } catch (_: EOFException) {
                    token = JsonToken.END_DOCUMENT
                    return JsonToken.END_DOCUMENT
                }
            }
            Scope.CLOSED -> throw IllegalStateException("JsonReader is closed")
        }
    }

    private fun advance(): JsonToken {
        peek()
        val result = token!!
        token = null
        value = null
        name = null
        return result
    }

    actual open fun nextName(): String {
        peek()
        if (token != JsonToken.NAME) {
            throw IllegalStateException("Expected a name but was ${peek()}")
        }
        val result = name!!
        advance()
        return result
    }

    actual open fun nextString(): String {
        peek()
        if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
            throw IllegalStateException("Expected a string but was ${peek()}")
        }
        val result = value!!
        advance()
        return result
    }

    actual open fun nextBoolean(): Boolean {
        peek()
        if (token != JsonToken.BOOLEAN) {
            throw IllegalStateException("Expected a boolean but was $token")
        }
        val result = value == "true"
        advance()
        return result
    }

    actual open fun nextNull() {
        peek()
        if (token != JsonToken.NULL) {
            throw IllegalStateException("Expected null but was $token")
        }
        advance()
    }

    actual open fun nextDouble(): Double {
        peek()
        if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
            throw IllegalStateException("Expected a double but was $token")
        }
        val result = value!!.toDouble()
        if (!lenient && (result.isNaN() || result.isInfinite())) {
            throw MalformedJsonException("JSON forbids NaN and infinities: $result")
        }
        advance()
        return result
    }

    actual open fun nextLong(): Long {
        peek()
        if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
            throw IllegalStateException("Expected a long but was $token")
        }
        val result = try {
            value!!.toLong()
        } catch (_: NumberFormatException) {
            val asDouble = value!!.toDouble()
            val asLong = asDouble.toLong()
            if (asLong.toDouble() != asDouble) {
                throw NumberFormatException(value)
            }
            asLong
        }
        advance()
        return result
    }

    actual open fun nextInt(): Int {
        peek()
        if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
            throw IllegalStateException("Expected an int but was $token")
        }
        val result = try {
            value!!.toInt()
        } catch (_: NumberFormatException) {
            val asDouble = value!!.toDouble()
            val asInt = asDouble.toInt()
            if (asInt.toDouble() != asDouble) {
                throw NumberFormatException(value)
            }
            asInt
        }
        advance()
        return result
    }

    actual override fun close() {
        value = null
        token = null
        stack.clear()
        stack.add(Scope.CLOSED)
        inReader.close()
    }

    actual open fun skipValue() {
        skipping = true
        try {
            var count = 0
            do {
                val t = advance()
                if (t == JsonToken.BEGIN_ARRAY || t == JsonToken.BEGIN_OBJECT) {
                    count++
                } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                    count--
                }
            } while (count != 0)
        } finally {
            skipping = false
        }
    }

    private fun peekStack(): Scope = stack[stack.size - 1]

    private fun pop(): Scope = stack.removeAt(stack.size - 1)

    private fun push(newTop: Scope) {
        stack.add(newTop)
    }

    private fun replaceTop(newTop: Scope) {
        stack[stack.size - 1] = newTop
    }

    private fun nextInArray(firstElement: Boolean): JsonToken {
        if (firstElement) {
            replaceTop(Scope.NONEMPTY_ARRAY)
        } else {
            when (nextNonWhitespace()) {
                ']'.code -> {
                    pop()
                    token = JsonToken.END_ARRAY
                    return JsonToken.END_ARRAY
                }
                ';'.code -> checkLenient()
                ','.code -> {}
                else -> throw syntaxError("Unterminated array")
            }
        }
        return when (val c = nextNonWhitespace()) {
            ']'.code -> {
                if (firstElement) {
                    pop()
                    token = JsonToken.END_ARRAY
                    return JsonToken.END_ARRAY
                }
                checkLenient()
                pos--
                value = "null"
                token = JsonToken.NULL
                JsonToken.NULL
            }
            ';'.code, ','.code -> {
                checkLenient()
                pos--
                value = "null"
                token = JsonToken.NULL
                JsonToken.NULL
            }
            else -> {
                pos--
                nextValue()
            }
        }
    }

    private fun nextInObject(firstElement: Boolean): JsonToken {
        if (firstElement) {
            if (nextNonWhitespace() == '}'.code) {
                pop()
                token = JsonToken.END_OBJECT
                return JsonToken.END_OBJECT
            }
            pos--
        } else {
            when (nextNonWhitespace()) {
                '}'.code -> {
                    pop()
                    token = JsonToken.END_OBJECT
                    return JsonToken.END_OBJECT
                }
                ';'.code, ','.code -> {}
                else -> throw syntaxError("Unterminated object")
            }
        }
        val quote = nextNonWhitespace()
        name = when (quote) {
            '\''.code -> {
                checkLenient()
                nextString('\'')
            }
            '"'.code -> nextString('"')
            else -> {
                checkLenient()
                pos--
                val n = nextLiteral()
                if (n.isEmpty()) throw syntaxError("Expected name")
                n
            }
        }
        replaceTop(Scope.DANGLING_NAME)
        token = JsonToken.NAME
        return JsonToken.NAME
    }

    private fun objectValue(): JsonToken {
        when (nextNonWhitespace()) {
            ':'.code -> {}
            '='.code -> {
                checkLenient()
                if ((pos < limit || fillBuffer(1)) && buffer[pos] == '>') {
                    pos++
                }
            }
            else -> throw syntaxError("Expected ':'")
        }
        replaceTop(Scope.NONEMPTY_OBJECT)
        return nextValue()
    }

    private fun nextValue(): JsonToken {
        return when (val c = nextNonWhitespace()) {
            '{'.code -> {
                push(Scope.EMPTY_OBJECT)
                token = JsonToken.BEGIN_OBJECT
                JsonToken.BEGIN_OBJECT
            }
            '['.code -> {
                push(Scope.EMPTY_ARRAY)
                token = JsonToken.BEGIN_ARRAY
                JsonToken.BEGIN_ARRAY
            }
            '\''.code -> {
                checkLenient()
                value = nextString('\'')
                token = JsonToken.STRING
                JsonToken.STRING
            }
            '"'.code -> {
                value = nextString('"')
                token = JsonToken.STRING
                JsonToken.STRING
            }
            else -> {
                pos--
                readLiteral()
            }
        }
    }

    private fun fillBuffer(minimum: Int): Boolean {
        if (pos != 0) {
            limit -= pos
            buffer.copyInto(buffer, destinationOffset = 0, startIndex = pos, endIndex = pos + limit)
            pos = 0
        }
        while (limit < minimum) {
            val total = inReader.read(buffer, limit, buffer.size - limit)
            if (total == -1) return false
            limit += total
        }
        return true
    }

    private fun nextNonWhitespace(): Int {
        while (pos < limit || fillBuffer(1)) {
            val c = buffer[pos++]
            when (c) {
                '\t', ' ', '\n', '\r' -> continue
                '/' -> {
                    if (pos == limit && !fillBuffer(1)) return c.code
                    checkLenient()
                    when (buffer[pos]) {
                        '*' -> {
                            pos++
                            if (!skipTo("*/")) throw syntaxError("Unterminated comment")
                            pos += 2
                            continue
                        }
                        '/' -> {
                            pos++
                            skipToEndOfLine()
                            continue
                        }
                        else -> return c.code
                    }
                }
                '#' -> {
                    checkLenient()
                    skipToEndOfLine()
                    continue
                }
                else -> return c.code
            }
        }
        throw EOFException("End of input")
    }

    private fun checkLenient() {
        if (!lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON")
        }
    }

    private fun skipToEndOfLine() {
        while (pos < limit || fillBuffer(1)) {
            val c = buffer[pos++]
            if (c == '\r' || c == '\n') break
        }
    }

    private fun skipTo(to: String): Boolean {
        outer@ while (pos + to.length <= limit || fillBuffer(to.length)) {
            for (c in to.indices) {
                if (buffer[pos + c] != to[c]) {
                    pos++
                    continue@outer
                }
            }
            return true
        }
        return false
    }

    private fun nextString(quote: Char): String {
        var builder: StringBuilder? = null
        do {
            val start = pos
            while (pos < limit) {
                val c = buffer[pos++]
                if (c == quote) {
                    return if (skipping) {
                        "skipped!"
                    } else if (builder == null) {
                        buffer.concatToString(start, pos - 1)
                    } else {
                        builder.appendRange(buffer, start, pos - 1)
                        builder.toString()
                    }
                } else if (c == '\\') {
                    if (builder == null) builder = StringBuilder()
                    builder.appendRange(buffer, start, pos - 1)
                    builder.append(readEscapeCharacter())
                    return builder.append(nextString(quote)).toString()
                }
            }
            if (builder == null) builder = StringBuilder()
            builder.appendRange(buffer, start, pos)
        } while (fillBuffer(1))
        throw syntaxError("Unterminated string")
    }

    private fun nextLiteral(): String {
        var builder: StringBuilder? = null
        do {
            val start = pos
            while (pos < limit) {
                when (val c = buffer[pos]) {
                    '/', '\\', ';', '#', '=' -> {
                        checkLenient()
                        if (builder == null) return buffer.concatToString(start, pos)
                        builder.appendRange(buffer, start, pos)
                        return builder.toString()
                    }
                    '{', '}', '[', ']', ':', ',', ' ', '\t', '\u000c', '\r', '\n' -> {
                        if (builder == null) return buffer.concatToString(start, pos)
                        builder.appendRange(buffer, start, pos)
                        return builder.toString()
                    }
                    else -> pos++
                }
            }
            if (builder == null) builder = StringBuilder()
            builder.appendRange(buffer, start, pos)
        } while (fillBuffer(1))
        return builder.toString()
    }

    private fun readEscapeCharacter(): Char {
        if (pos == limit && !fillBuffer(1)) {
            throw syntaxError("Unterminated escape sequence")
        }
        return when (val escaped = buffer[pos++]) {
            'u' -> {
                if (pos + 4 > limit && !fillBuffer(4)) {
                    throw syntaxError("Unterminated escape sequence")
                }
                val hex = buffer.concatToString(pos, pos + 4)
                pos += 4
                hex.toInt(16).toChar()
            }
            't' -> '\t'
            'b' -> '\b'
            'n' -> '\n'
            'r' -> '\r'
            'f' -> '\u000c'
            '\'', '"', '\\' -> escaped
            else -> escaped
        }
    }

    private fun readLiteral(): JsonToken {
        val lit = nextLiteral()
        if (lit.isEmpty()) throw syntaxError("Expected literal value")
        value = lit
        token = decodeLiteral(lit)
        if (token == JsonToken.STRING) {
            checkLenient()
        }
        return token!!
    }

    private fun decodeLiteral(lit: String): JsonToken {
        if (lit.equals("null", ignoreCase = true)) return JsonToken.NULL
        if (lit.equals("true", ignoreCase = true)) {
            value = "true"
            return JsonToken.BOOLEAN
        }
        if (lit.equals("false", ignoreCase = true)) {
            value = "false"
            return JsonToken.BOOLEAN
        }
        val first = lit[0]
        if (first == '-' || first in '0'..'9') {
            if (lit.toLongOrNull() != null || lit.toDoubleOrNull() != null) {
                return JsonToken.NUMBER
            }
        }
        return JsonToken.STRING
    }

    private fun syntaxError(message: String): IOException {
        throw MalformedJsonException(message)
    }
}

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
