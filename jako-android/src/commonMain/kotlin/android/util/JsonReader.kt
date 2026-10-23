@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.io.Closeable
import java.io.IOException
import java.io.Reader

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
