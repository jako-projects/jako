@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

/**
 * A clock providing access to the current instant, date and time using a time-zone.
 */
expect abstract class Clock {
    protected constructor()

    val zone: ZoneId
    abstract fun getZone(): ZoneId
    abstract fun withZone(zone: ZoneId): Clock
    open fun millis(): Long
    abstract fun instant(): Instant

    companion object {
        fun systemUTC(): Clock
        fun systemDefaultZone(): Clock
        fun system(zone: ZoneId): Clock
        fun tickSeconds(zone: ZoneId): Clock
        fun tickMinutes(zone: ZoneId): Clock
        fun tick(baseClock: Clock, tickDuration: Duration): Clock
        fun fixed(fixedInstant: Instant, zone: ZoneId): Clock
        fun offset(baseClock: Clock, offsetDuration: Duration): Clock
    }
}
