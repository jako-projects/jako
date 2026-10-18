@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.io.Closeable
import java.io.IOException
import java.io.Reader
import java.io.Writer

expect open class MalformedJsonException : IOException {
    constructor(message: String?)
}

expect enum class JsonToken {
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

/**
 * [Nullability Decision] JsonReader methods return non-null tokens, names, and values (throwing IOException/IllegalStateException on mismatch),
 * matching AOSP android.util.JsonReader.
 */
expect open class JsonReader : Closeable {
    constructor(inReader: Reader)

    open var isLenient: Boolean
    open fun setLenient(lenient: Boolean)
    open fun isLenient(): Boolean

    open fun beginArray()
    open fun endArray()
    open fun beginObject()
    open fun endObject()
    open fun hasNext(): Boolean
    open fun peek(): JsonToken
    open fun nextName(): String
    open fun nextString(): String
    open fun nextBoolean(): Boolean
    open fun nextNull()
    open fun nextDouble(): Double
    open fun nextLong(): Long
    open fun nextInt(): Int
    override fun close()
    open fun skipValue()
}

/**
 * [Nullability Decision] JsonWriter.value(String?) and value(Number?) accept nullable values (delegating to nullValue()),
 * matching AOSP android.util.JsonWriter.
 */
expect open class JsonWriter : Closeable {
    constructor(out: Writer)

    open var isLenient: Boolean
    open fun setLenient(lenient: Boolean)
    open fun isLenient(): Boolean
    open fun setIndent(indent: String)

    open fun beginArray(): JsonWriter
    open fun endArray(): JsonWriter
    open fun beginObject(): JsonWriter
    open fun endObject(): JsonWriter
    open fun name(name: String): JsonWriter
    open fun value(value: String?): JsonWriter
    open fun nullValue(): JsonWriter
    open fun value(value: Boolean): JsonWriter
    open fun value(value: Double): JsonWriter
    open fun value(value: Long): JsonWriter
    open fun value(value: Number?): JsonWriter
    open fun flush()
    override fun close()
}
