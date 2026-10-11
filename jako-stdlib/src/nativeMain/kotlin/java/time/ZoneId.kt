package java.time

import java.io.Serializable
import java.util.TimeZone

actual abstract class ZoneId : Serializable {
    actual abstract val id: String
    actual abstract fun getId(): String

    internal abstract fun offsetAt(instant: Instant): ZoneOffset
    internal abstract fun offsetAt(localDateTime: LocalDateTime): ZoneOffset

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return id == (other as ZoneId).id
    }

    actual override fun hashCode(): Int = id.hashCode()

    actual override fun toString(): String = id

    actual companion object {
        private val AVAILABLE: Set<String> = setOf(
            "UTC", "GMT", "UT", "Z", "Etc/UTC", "Etc/GMT",
            "Asia/Shanghai", "Asia/Chongqing", "Asia/Hong_Kong", "PRC",
            "Asia/Tokyo",
            "Europe/London", "Europe/Paris", "Europe/Berlin",
            "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles"
        )

        actual fun of(zoneId: String): ZoneId {
            // [Nullability Decision] zoneId is non-null, matching JDK 8 ZoneId.of(String)
            if (zoneId.length <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
                return ZoneOffset.of(zoneId)
            }
            if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
                return ofWithPrefix(zoneId, 3)
            }
            if (zoneId.startsWith("UT")) {
                return ofWithPrefix(zoneId, 2)
            }
            return regionOf(zoneId)
        }

        actual fun systemDefault(): ZoneId = of(TimeZone.getDefault().getID())

        actual fun getAvailableZoneIds(): Set<String> = AVAILABLE

        private fun ofWithPrefix(zoneId: String, prefixLength: Int): ZoneId {
            val prefix = zoneId.substring(0, prefixLength)
            if (zoneId.length == prefixLength) {
                return ZoneRegion(prefix, ZoneOffset.UTC)
            }
            val next = zoneId[prefixLength]
            if (next != '+' && next != '-') {
                return regionOf(zoneId)
            }
            val offset = ZoneOffset.of(zoneId.substring(prefixLength))
            return if (offset.totalSeconds == 0) {
                ZoneRegion(prefix, offset)
            } else {
                ZoneRegion(prefix + offset.id, offset)
            }
        }

        internal fun regionOf(zoneId: String): ZoneId {
            val offset = namedOffset(zoneId)
                ?: throw DateTimeException("Unknown time-zone ID: $zoneId")
            return ZoneRegion(zoneId, offset)
        }

        internal fun namedOffset(id: String): ZoneOffset? {
            return when (id) {
                "UTC", "GMT", "UT", "Etc/UTC", "Etc/GMT" -> ZoneOffset.UTC
                "Asia/Shanghai", "Asia/Chongqing", "Asia/Hong_Kong", "PRC", "CTT" -> ZoneOffset.ofHours(8)
                "Asia/Tokyo", "JST" -> ZoneOffset.ofHours(9)
                "America/New_York", "EST" -> ZoneOffset.ofHours(-5)
                "America/Chicago", "CST" -> ZoneOffset.ofHours(-6)
                "America/Denver", "MST" -> ZoneOffset.ofHours(-7)
                "America/Los_Angeles", "PST" -> ZoneOffset.ofHours(-8)
                "Europe/London" -> ZoneOffset.UTC
                "Europe/Paris", "Europe/Berlin", "CET" -> ZoneOffset.ofHours(1)
                else -> null
            }
        }
    }
}

internal class ZoneRegion(
    regionId: String,
    private val offset: ZoneOffset
) : ZoneId() {
    override val id: String = regionId
    override fun getId(): String = id
    override fun offsetAt(instant: Instant): ZoneOffset = offset
    override fun offsetAt(localDateTime: LocalDateTime): ZoneOffset = offset

    override fun hashCode(): Int = id.hashCode() xor offset.hashCode()
}
