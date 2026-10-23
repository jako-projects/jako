@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect enum class TimeUnit {
    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;

    fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long
    fun toNanos(duration: Long): Long
    fun toMicros(duration: Long): Long
    fun toMillis(duration: Long): Long
    fun toSeconds(duration: Long): Long
    fun toMinutes(duration: Long): Long
    fun toHours(duration: Long): Long
    fun toDays(duration: Long): Long
}
