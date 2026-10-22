package java.time

import java.io.Serializable
import java.lang.System

actual abstract class Clock protected actual constructor() {
    actual val zone: ZoneId get() = getZone()
    actual abstract fun getZone(): ZoneId
    actual abstract fun withZone(zone: ZoneId): Clock
    actual open fun millis(): Long = instant().toEpochMilli()
    actual abstract fun instant(): Instant

    actual companion object {
        actual fun systemUTC(): Clock = SystemClock(ZoneOffset.UTC)
        actual fun systemDefaultZone(): Clock = SystemClock(ZoneId.systemDefault())
        actual fun system(zone: ZoneId): Clock = SystemClock(zone)

        actual fun tickSeconds(zone: ZoneId): Clock = TickClock(system(zone), NANOS_PER_SECOND)
        actual fun tickMinutes(zone: ZoneId): Clock = TickClock(system(zone), NANOS_PER_MINUTE)

        actual fun tick(baseClock: Clock, tickDuration: Duration): Clock {
            if (tickDuration.isNegative()) {
                throw IllegalArgumentException("Tick duration must not be negative")
            }
            val tickNanos = tickDuration.toNanos()
            if (tickNanos <= 1L) return baseClock
            return TickClock(baseClock, tickNanos)
        }

        actual fun fixed(fixedInstant: Instant, zone: ZoneId): Clock = FixedClock(fixedInstant, zone)

        actual fun offset(baseClock: Clock, offsetDuration: Duration): Clock {
            if (offsetDuration.isZero()) return baseClock
            return OffsetClock(baseClock, offsetDuration)
        }
    }
}

internal class SystemClock(private val clockZone: ZoneId) : Clock(), Serializable {
    override fun getZone(): ZoneId = clockZone
    override fun withZone(zone: ZoneId): Clock = if (zone == clockZone) this else SystemClock(zone)
    override fun millis(): Long = System.currentTimeMillis()
    override fun instant(): Instant = Instant.ofEpochMilli(millis())
    override fun equals(other: Any?): Boolean = other is SystemClock && clockZone == other.clockZone
    override fun hashCode(): Int = clockZone.hashCode() + 1
    override fun toString(): String = "SystemClock[$clockZone]"
}

internal class FixedClock(private val fixedInstant: Instant, private val clockZone: ZoneId) : Clock(), Serializable {
    override fun getZone(): ZoneId = clockZone
    override fun withZone(zone: ZoneId): Clock = if (zone == clockZone) this else FixedClock(fixedInstant, zone)
    override fun millis(): Long = fixedInstant.toEpochMilli()
    override fun instant(): Instant = fixedInstant
    override fun equals(other: Any?): Boolean =
        other is FixedClock && fixedInstant == other.fixedInstant && clockZone == other.clockZone
    override fun hashCode(): Int = fixedInstant.hashCode() xor clockZone.hashCode()
    override fun toString(): String = "FixedClock[$fixedInstant,$clockZone]"
}

internal class OffsetClock(private val baseClock: Clock, private val offset: Duration) : Clock(), Serializable {
    override fun getZone(): ZoneId = baseClock.getZone()
    override fun withZone(zone: ZoneId): Clock =
        if (zone == baseClock.getZone()) this else OffsetClock(baseClock.withZone(zone), offset)
    override fun millis(): Long = baseClock.millis() + offset.toMillis()
    override fun instant(): Instant = baseClock.instant().plusSeconds(offset.seconds).plusNanos(offset.nano.toLong())
    override fun equals(other: Any?): Boolean =
        other is OffsetClock && baseClock == other.baseClock && offset == other.offset
    override fun hashCode(): Int = baseClock.hashCode() xor offset.hashCode()
    override fun toString(): String = "OffsetClock[$baseClock,$offset]"
}

internal class TickClock(private val baseClock: Clock, private val tickNanos: Long) : Clock(), Serializable {
    override fun getZone(): ZoneId = baseClock.getZone()
    override fun withZone(zone: ZoneId): Clock =
        if (zone == baseClock.getZone()) this else TickClock(baseClock.withZone(zone), tickNanos)
    override fun millis(): Long {
        val millis = baseClock.millis()
        val tickMillis = tickNanos / 1_000_000L
        return if (tickMillis <= 1L) millis else millis - millis.mod(tickMillis)
    }
    override fun instant(): Instant {
        if ((tickNanos % 1_000_000L) == 0L) {
            val millis = baseClock.millis()
            val tickMillis = tickNanos / 1_000_000L
            return Instant.ofEpochMilli(millis - millis.mod(tickMillis))
        }
        val instant = baseClock.instant()
        val nanos = instant.nano.toLong()
        return instant.minusNanos(nanos.mod(tickNanos))
    }
    override fun equals(other: Any?): Boolean =
        other is TickClock && baseClock == other.baseClock && tickNanos == other.tickNanos
    override fun hashCode(): Int = baseClock.hashCode() xor (tickNanos xor (tickNanos ushr 32)).toInt()
    override fun toString(): String = "TickClock[$baseClock,${Duration.ofNanos(tickNanos)}]"
}
