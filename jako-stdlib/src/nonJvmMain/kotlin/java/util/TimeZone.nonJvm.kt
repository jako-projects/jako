package java.util

import java.io.Serializable
import java.lang.Cloneable
import java.time.ZoneId

actual open class TimeZone : Serializable, Cloneable {
    private val zoneId: String
    private val rawOffsetMillis: Int

    constructor(id: String, rawOffsetMillis: Int) {
        this.zoneId = id
        this.rawOffsetMillis = rawOffsetMillis
    }

    constructor() : this("UTC", 0)

    actual open fun getID(): String = zoneId
    actual open fun getRawOffset(): Int = rawOffsetMillis
    actual open fun getOffset(date: Long): Int {
        val spec = ZoneId.namedRuleSpec(zoneId)
        if (spec != null) {
            return spec.offsetAtInstant(date.floorDiv(1000L)).totalSeconds * 1000
        }
        return rawOffsetMillis
    }
    actual open fun getDisplayName(): String = zoneId
    actual open fun useDaylightTime(): Boolean {
        val spec = ZoneId.namedRuleSpec(zoneId)
        return spec?.usesDst ?: false
    }
    actual open fun inDaylightTime(date: Date): Boolean {
        val spec = ZoneId.namedRuleSpec(zoneId) ?: return false
        if (!spec.usesDst) return false
        return spec.offsetAtInstant(date.getTime().floorDiv(1000L)) == spec.dstOffset
    }

    actual open val id: String get() = getID()
    actual open val rawOffset: Int get() = getRawOffset()
    actual open val displayName: String get() = getDisplayName()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeZone) return false
        return zoneId == other.zoneId && rawOffsetMillis == other.rawOffsetMillis
    }

    actual override fun hashCode(): Int = zoneId.hashCode() xor rawOffsetMillis

    actual override fun toString(): String = "TimeZone[id=\"$zoneId\",offset=$rawOffsetMillis]"

    actual companion object {
        actual val SHORT: Int = 0
        actual val LONG: Int = 1

        private var defaultZone: TimeZone = TimeZone("UTC", 0)

        actual fun getDefault(): TimeZone = defaultZone

        actual fun setDefault(zone: TimeZone) {
            defaultZone = zone
        }

        actual fun getTimeZone(id: String): TimeZone {
            // JDK keeps only the exact id "UTC". Other casings and "UTC±..." are unknown
            // and fall back to GMT offset 0. Custom offsets must start with "GMT+" or
            // "GMT-" and are normalized to "GMT±HH:MM".
            val spec = ZoneId.namedRuleSpec(id)
            if (spec != null) {
                return TimeZone(id, spec.stdOffset.totalSeconds * 1000)
            }
            return parseCustomGmtZone(id) ?: TimeZone("GMT", 0)
        }

        /**
         * JDK 8 `TimeZone.parseCustomTimeZone`: only an id that starts with "GMT+" or
         * "GMT-" is a custom zone. The stored id is always `GMT±HH:MM`.
         */
        private fun parseCustomGmtZone(id: String): TimeZone? {
            if (id.length < 5 || !id.startsWith("GMT")) {
                return null
            }
            val negative = when (id[3]) {
                '-' -> true
                '+' -> false
                else -> return null
            }
            var index = 4
            var hours = 0
            var num = 0
            var colonCount = 0
            var len = 0
            while (index < id.length) {
                val c = id[index]
                index++
                if (c == ':') {
                    if (colonCount > 0 || len > 2) {
                        return null
                    }
                    hours = num
                    colonCount++
                    num = 0
                    len = 0
                    continue
                }
                if (c < '0' || c > '9') {
                    return null
                }
                num = num * 10 + (c - '0')
                len++
            }
            if (colonCount == 0) {
                if (len <= 2) {
                    hours = num
                    num = 0
                } else if (len <= 4) {
                    hours = num / 100
                    num %= 100
                } else {
                    return null
                }
            } else if (len != 2) {
                return null
            }
            if (hours > 23 || num > 59) {
                return null
            }
            val minutesFromUtc = hours * 60 + num
            val offset = (if (negative) -minutesFromUtc else minutesFromUtc) * 60 * 1000
            return TimeZone(normalizedCustomGmtId(negative, minutesFromUtc), offset)
        }

        private fun normalizedCustomGmtId(negative: Boolean, minutesFromUtc: Int): String {
            val sign = if (negative) '-' else '+'
            val hours = minutesFromUtc / 60
            val minutes = minutesFromUtc % 60
            return "GMT$sign${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
        }

        actual fun getAvailableIDs(): Array<String> {
            return arrayOf(
                "UTC", "GMT", "Asia/Shanghai", "Asia/Tokyo", "Europe/London",
                "Europe/Paris", "Europe/Berlin", "America/New_York", "America/Chicago",
                "America/Denver", "America/Los_Angeles", "Australia/Sydney", "Pacific/Auckland"
            )
        }

        actual fun getAvailableIDs(rawOffset: Int): Array<String> {
            return getAvailableIDs().filter { getTimeZone(it).getRawOffset() == rawOffset }.toTypedArray()
        }
    }
}

