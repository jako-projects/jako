@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.io.Closeable
import java.io.Writer

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
