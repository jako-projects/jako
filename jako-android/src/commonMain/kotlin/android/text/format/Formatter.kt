@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.text.format

import android.content.Context

/**
 * [Nullability Decision] `context` is nullable (`Context?`) and return value is non-null `String`
 * (returning `""` only if `context` is optional or formatting negative/positive sizes), matching AOSP `Formatter`.
 */
expect open class Formatter() {
    companion object {
        fun formatFileSize(context: Context?, sizeBytes: Long): String
        fun formatShortFileSize(context: Context?, sizeBytes: Long): String
    }
}
