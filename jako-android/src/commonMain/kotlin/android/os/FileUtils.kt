@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import java.io.InputStream
import java.io.OutputStream

/**
 * [Nullability Decision] `copy` requires non-null `InputStream` and `OutputStream`, while `closeQuietly` accepts
 * nullable `AutoCloseable?`, matching AOSP `android.os.FileUtils`.
 */
expect object FileUtils {
    fun copy(`in`: InputStream, out: OutputStream): Long
    fun closeQuietly(closeable: AutoCloseable?)
}
