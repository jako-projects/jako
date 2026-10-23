@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Int

/**
 * [Nullability Decision] Java Readable.read(CharBuffer) takes a non-null CharBuffer.
 */
expect fun interface Readable {
    fun read(cb: java.nio.CharBuffer): Int
}
