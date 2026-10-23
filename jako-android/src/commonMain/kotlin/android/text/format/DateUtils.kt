@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.text.format

/**
 * [Nullability Decision] `formatElapsedTime` returns non-null `String` and accepts optional `StringBuilder?` recycle buffer,
 * matching AOSP `android.text.format.DateUtils`.
 */
expect open class DateUtils() {
    companion object {
        val SECOND_IN_MILLIS: Long
        val MINUTE_IN_MILLIS: Long
        val HOUR_IN_MILLIS: Long
        val DAY_IN_MILLIS: Long
        val WEEK_IN_MILLIS: Long
        val YEAR_IN_MILLIS: Long

        fun formatElapsedTime(elapsedSeconds: Long): String
        fun formatElapsedTime(recycle: StringBuilder?, elapsedSeconds: Long): String
        fun isToday(`when`: Long): Boolean
    }
}
