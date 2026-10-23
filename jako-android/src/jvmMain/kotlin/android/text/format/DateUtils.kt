package android.text.format

import java.util.Calendar
import java.util.Locale

open class DateUtils {
    companion object {
        const val SECOND_IN_MILLIS: Long = 1000L
        const val MINUTE_IN_MILLIS: Long = SECOND_IN_MILLIS * 60L
        const val HOUR_IN_MILLIS: Long = MINUTE_IN_MILLIS * 60L
        const val DAY_IN_MILLIS: Long = HOUR_IN_MILLIS * 24L
        const val WEEK_IN_MILLIS: Long = DAY_IN_MILLIS * 7L
        const val YEAR_IN_MILLIS: Long = WEEK_IN_MILLIS * 52L

        @JvmStatic
        fun formatElapsedTime(elapsedSeconds: Long): String =
            formatElapsedTime(null, elapsedSeconds)

        @JvmStatic
        fun formatElapsedTime(recycle: StringBuilder?, elapsedSeconds: Long): String {
            var remaining = if (elapsedSeconds < 0) 0L else elapsedSeconds
            var hours = 0L
            var minutes = 0L
            if (remaining >= 3600L) {
                hours = remaining / 3600L
                remaining -= hours * 3600L
            }
            if (remaining >= 60L) {
                minutes = remaining / 60L
                remaining -= minutes * 60L
            }
            val seconds = remaining

            val sb = recycle ?: StringBuilder(8)
            sb.setLength(0)
            val formatted = if (hours > 0L) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
            sb.append(formatted)
            return sb.toString()
        }

        @JvmStatic
        fun isToday(`when`: Long): Boolean {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance()
            target.timeInMillis = `when`
            return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == target.get(Calendar.DAY_OF_MONTH)
        }
    }
}
