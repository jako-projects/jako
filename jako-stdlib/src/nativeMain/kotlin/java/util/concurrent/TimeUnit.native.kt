package java.util.concurrent

actual enum class TimeUnit {
    NANOSECONDS {
        override fun toNanos(duration: Long): Long = duration
        override fun toMicros(duration: Long): Long = duration / (C1 / C0)
        override fun toMillis(duration: Long): Long = duration / (C2 / C0)
        override fun toSeconds(duration: Long): Long = duration / (C3 / C0)
        override fun toMinutes(duration: Long): Long = duration / (C4 / C0)
        override fun toHours(duration: Long): Long = duration / (C5 / C0)
        override fun toDays(duration: Long): Long = duration / (C6 / C0)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toNanos(sourceDuration)
    },
    MICROSECONDS {
        override fun toNanos(duration: Long): Long = x(duration, C1 / C0, MAX / (C1 / C0))
        override fun toMicros(duration: Long): Long = duration
        override fun toMillis(duration: Long): Long = duration / (C2 / C1)
        override fun toSeconds(duration: Long): Long = duration / (C3 / C1)
        override fun toMinutes(duration: Long): Long = duration / (C4 / C1)
        override fun toHours(duration: Long): Long = duration / (C5 / C1)
        override fun toDays(duration: Long): Long = duration / (C6 / C1)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toMicros(sourceDuration)
    },
    MILLISECONDS {
        override fun toNanos(duration: Long): Long = x(duration, C2 / C0, MAX / (C2 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C2 / C1, MAX / (C2 / C1))
        override fun toMillis(duration: Long): Long = duration
        override fun toSeconds(duration: Long): Long = duration / (C3 / C2)
        override fun toMinutes(duration: Long): Long = duration / (C4 / C2)
        override fun toHours(duration: Long): Long = duration / (C5 / C2)
        override fun toDays(duration: Long): Long = duration / (C6 / C2)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toMillis(sourceDuration)
    },
    SECONDS {
        override fun toNanos(duration: Long): Long = x(duration, C3 / C0, MAX / (C3 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C3 / C1, MAX / (C3 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C3 / C2, MAX / (C3 / C2))
        override fun toSeconds(duration: Long): Long = duration
        override fun toMinutes(duration: Long): Long = duration / (C4 / C3)
        override fun toHours(duration: Long): Long = duration / (C5 / C3)
        override fun toDays(duration: Long): Long = duration / (C6 / C3)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toSeconds(sourceDuration)
    },
    MINUTES {
        override fun toNanos(duration: Long): Long = x(duration, C4 / C0, MAX / (C4 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C4 / C1, MAX / (C4 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C4 / C2, MAX / (C4 / C2))
        override fun toSeconds(duration: Long): Long = x(duration, C4 / C3, MAX / (C4 / C3))
        override fun toMinutes(duration: Long): Long = duration
        override fun toHours(duration: Long): Long = duration / (C5 / C4)
        override fun toDays(duration: Long): Long = duration / (C6 / C4)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toMinutes(sourceDuration)
    },
    HOURS {
        override fun toNanos(duration: Long): Long = x(duration, C5 / C0, MAX / (C5 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C5 / C1, MAX / (C5 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C5 / C2, MAX / (C5 / C2))
        override fun toSeconds(duration: Long): Long = x(duration, C5 / C3, MAX / (C5 / C3))
        override fun toMinutes(duration: Long): Long = x(duration, C5 / C4, MAX / (C5 / C4))
        override fun toHours(duration: Long): Long = duration
        override fun toDays(duration: Long): Long = duration / (C6 / C5)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toHours(sourceDuration)
    },
    DAYS {
        override fun toNanos(duration: Long): Long = x(duration, C6 / C0, MAX / (C6 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C6 / C1, MAX / (C6 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C6 / C2, MAX / (C6 / C2))
        override fun toSeconds(duration: Long): Long = x(duration, C6 / C3, MAX / (C6 / C3))
        override fun toMinutes(duration: Long): Long = x(duration, C6 / C4, MAX / (C6 / C4))
        override fun toHours(duration: Long): Long = x(duration, C6 / C5, MAX / (C6 / C5))
        override fun toDays(duration: Long): Long = duration
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toDays(sourceDuration)
    };

    actual open fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.convert(sourceDuration, this)
    actual open fun toNanos(duration: Long): Long = duration
    actual open fun toMicros(duration: Long): Long = duration
    actual open fun toMillis(duration: Long): Long = duration
    actual open fun toSeconds(duration: Long): Long = duration
    actual open fun toMinutes(duration: Long): Long = duration
    actual open fun toHours(duration: Long): Long = duration
    actual open fun toDays(duration: Long): Long = duration
}

private const val C0: Long = 1L
private const val C1: Long = 1000L
private const val C2: Long = C1 * 1000L
private const val C3: Long = C2 * 1000L
private const val C4: Long = C3 * 60L
private const val C5: Long = C4 * 60L
private const val C6: Long = C5 * 24L
private const val MAX: Long = Long.MAX_VALUE

private fun x(d: Long, m: Long, over: Long): Long {
    if (d > over) return Long.MAX_VALUE
    if (d < -over) return Long.MIN_VALUE
    return d * m
}
