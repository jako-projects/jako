package android.text.format

import android.content.Context
import java.util.Locale

/**
 * [Nullability Decision] `context` is nullable (`Context?`) and return value is non-null `String`,
 * matching AOSP `android.text.format.Formatter`.
 */
actual open class Formatter actual constructor() {
    actual companion object {
        actual fun formatFileSize(context: Context?, sizeBytes: Long): String =
            formatBytesInternal(context, sizeBytes, shortFormat = false)

        actual fun formatShortFileSize(context: Context?, sizeBytes: Long): String =
            formatBytesInternal(context, sizeBytes, shortFormat = true)

        private fun formatBytesInternal(context: Context?, sizeBytes: Long, shortFormat: Boolean): String {
            val isNegative = sizeBytes < 0
            var result = if (isNegative) -sizeBytes.toDouble() else sizeBytes.toDouble()
            val units = arrayOf("B", "kB", "MB", "GB", "TB", "PB")
            var unitIndex = 0
            while (result > 900 && unitIndex < units.size - 1) {
                result /= 1000.0
                unitIndex++
            }
            val locale = context?.resources?.configuration?.locale ?: Locale.US
            val numberStr = when {
                unitIndex == 0 -> String.format(locale, "%.0f", result)
                result < 1 -> String.format(locale, "%.2f", result)
                result < 10 -> String.format(locale, if (shortFormat) "%.1f" else "%.2f", result)
                result < 100 -> String.format(locale, if (shortFormat) "%.0f" else "%.1f", result)
                else -> String.format(locale, "%.0f", result)
            }
            val prefix = if (isNegative) "-" else ""
            return "$prefix$numberStr ${units[unitIndex]}"
        }
    }
}
