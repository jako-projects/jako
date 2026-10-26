package java.time

import java.io.Serializable
import java.util.TimeZone

internal enum class DstRuleKind {
    NONE,
    US, // 2nd Sunday of March 02:00 local std -> 1st Sunday of November 02:00 local dst
    EU, // Last Sunday of March 01:00 UTC -> Last Sunday of October 01:00 UTC
    AU, // 1st Sunday of October 02:00 local std -> 1st Sunday of April 03:00 local dst (Southern Hemisphere)
    NZ  // Last Sunday of September 02:00 local std -> 1st Sunday of April 03:00 local dst (Southern Hemisphere)
}

internal data class ZoneRuleSpec(
    val stdOffset: ZoneOffset,
    val dstOffset: ZoneOffset,
    val ruleKind: DstRuleKind
) {
    val usesDst: Boolean get() = ruleKind != DstRuleKind.NONE && stdOffset != dstOffset

    fun offsetAtInstant(epochSecond: Long): ZoneOffset {
        if (!usesDst) return stdOffset
        val approxYear = LocalDate.ofEpochDay(epochSecond.floorDiv(SECONDS_PER_DAY)).year
        val (springSec, fallSec) = transitionEpochSeconds(approxYear)
        return if (springSec < fallSec) {
            if (epochSecond in springSec until fallSec) dstOffset else stdOffset
        } else {
            // Southern Hemisphere: DST active before Fall Back in April or on/after Spring Forward in Sep/Oct
            if (epochSecond < fallSec || epochSecond >= springSec) dstOffset else stdOffset
        }
    }

    /**
     * Resolves a local date-time in this zone, returning Pair(adjustedLocalDateTime, resolvedOffset).
     * Matches JDK 8 ZonedDateTime.ofLocal(localDateTime, zone, preferredOffset) semantics:
     * - Gap (Spring Forward): pushes localDateTime forward by gap duration (3600s) and uses dstOffset.
     * - Overlap (Fall Back): uses preferredOffset if valid, otherwise earlier offset (dstOffset).
     */
    fun resolveLocal(localDateTime: LocalDateTime, preferredOffset: ZoneOffset? = null): Pair<LocalDateTime, ZoneOffset> {
        if (!usesDst) return localDateTime to stdOffset
        val year = localDateTime.year
        val gapStart = springGapStartLocal(year)
        val gapEnd = gapStart.plusSeconds(3600)
        if (!localDateTime.isBefore(gapStart) && localDateTime.isBefore(gapEnd)) {
            return localDateTime.plusSeconds(3600) to dstOffset
        }
        val overlapStart = fallOverlapStartLocal(year)
        val overlapEnd = overlapStart.plusSeconds(3600)
        if (!localDateTime.isBefore(overlapStart) && localDateTime.isBefore(overlapEnd)) {
            val chosen = if (preferredOffset == dstOffset || preferredOffset == stdOffset) {
                preferredOffset
            } else {
                dstOffset
            }
            return localDateTime to chosen
        }
        val activeOffset = if (gapStart.isBefore(overlapStart)) {
            if (!localDateTime.isBefore(gapEnd) && localDateTime.isBefore(overlapStart)) dstOffset else stdOffset
        } else {
            // Southern Hemisphere: DST is active before April overlap or after Sep/Oct gap
            if (localDateTime.isBefore(overlapStart) || !localDateTime.isBefore(gapEnd)) dstOffset else stdOffset
        }
        return localDateTime to activeOffset
    }

    fun isOverlap(localDateTime: LocalDateTime): Boolean {
        if (!usesDst) return false
        val overlapStart = fallOverlapStartLocal(localDateTime.year)
        val overlapEnd = overlapStart.plusSeconds(3600)
        return !localDateTime.isBefore(overlapStart) && localDateTime.isBefore(overlapEnd)
    }

    private fun transitionEpochSeconds(year: Int): Pair<Long, Long> {
        return when (ruleKind) {
            DstRuleKind.US, DstRuleKind.AU, DstRuleKind.NZ -> {
                val springLocal = springGapStartLocal(year)
                val fallLocal = fallOverlapStartLocal(year).plusSeconds(3600) // End of DST in local DST wall clock
                springLocal.toEpochSecond(stdOffset) to fallLocal.toEpochSecond(dstOffset)
            }
            DstRuleKind.EU -> {
                val march31 = LocalDate.of(year, 3, 31)
                val lastSunMarch = 31 - (march31.dayOfWeek.value % 7)
                val oct31 = LocalDate.of(year, 10, 31)
                val lastSunOct = 31 - (oct31.dayOfWeek.value % 7)
                val springSec = LocalDateTime.of(year, 3, lastSunMarch, 1, 0).toEpochSecond(ZoneOffset.UTC)
                val fallSec = LocalDateTime.of(year, 10, lastSunOct, 1, 0).toEpochSecond(ZoneOffset.UTC)
                springSec to fallSec
            }
            DstRuleKind.NONE -> 0L to 0L
        }
    }

    private fun springGapStartLocal(year: Int): LocalDateTime {
        return when (ruleKind) {
            DstRuleKind.US -> {
                val march1 = LocalDate.of(year, 3, 1)
                val secondSun = 8 + (7 - march1.dayOfWeek.value) % 7
                LocalDateTime.of(year, 3, secondSun, 2, 0)
            }
            DstRuleKind.EU -> {
                val march31 = LocalDate.of(year, 3, 31)
                val lastSunMarch = 31 - (march31.dayOfWeek.value % 7)
                val stdHours = stdOffset.totalSeconds / 3600
                LocalDateTime.of(year, 3, lastSunMarch, 1 + stdHours, 0)
            }
            DstRuleKind.AU -> {
                val oct1 = LocalDate.of(year, 10, 1)
                val firstSun = 1 + (7 - oct1.dayOfWeek.value) % 7
                LocalDateTime.of(year, 10, firstSun, 2, 0)
            }
            DstRuleKind.NZ -> {
                val sep30 = LocalDate.of(year, 9, 30)
                val lastSunSep = 30 - (sep30.dayOfWeek.value % 7)
                LocalDateTime.of(year, 9, lastSunSep, 2, 0)
            }
            DstRuleKind.NONE -> LocalDateTime.MIN
        }
    }

    private fun fallOverlapStartLocal(year: Int): LocalDateTime {
        return when (ruleKind) {
            DstRuleKind.US -> {
                val nov1 = LocalDate.of(year, 11, 1)
                val firstSun = 1 + (7 - nov1.dayOfWeek.value) % 7
                LocalDateTime.of(year, 11, firstSun, 1, 0)
            }
            DstRuleKind.EU -> {
                val oct31 = LocalDate.of(year, 10, 31)
                val lastSunOct = 31 - (oct31.dayOfWeek.value % 7)
                val stdHours = stdOffset.totalSeconds / 3600
                LocalDateTime.of(year, 10, lastSunOct, 1 + stdHours, 0)
            }
            DstRuleKind.AU, DstRuleKind.NZ -> {
                val apr1 = LocalDate.of(year, 4, 1)
                val firstSun = 1 + (7 - apr1.dayOfWeek.value) % 7
                LocalDateTime.of(year, 4, firstSun, 2, 0)
            }
            DstRuleKind.NONE -> LocalDateTime.MIN
        }
    }
}

actual abstract class ZoneId : Serializable {
    actual abstract val id: String
    actual abstract fun getId(): String

    internal abstract fun offsetAt(instant: Instant): ZoneOffset
    internal abstract fun offsetAt(localDateTime: LocalDateTime): ZoneOffset
    internal open fun resolveLocal(localDateTime: LocalDateTime, preferredOffset: ZoneOffset? = null): Pair<LocalDateTime, ZoneOffset> =
        localDateTime to offsetAt(localDateTime)
    internal open fun earlierOffsetAtOverlap(localDateTime: LocalDateTime, current: ZoneOffset): ZoneOffset = current
    internal open fun laterOffsetAtOverlap(localDateTime: LocalDateTime, current: ZoneOffset): ZoneOffset = current

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZoneId) return false
        return id == other.id
    }

    actual override fun hashCode(): Int = id.hashCode()

    actual override fun toString(): String = id

    actual companion object {
        private val AVAILABLE: Set<String> = setOf(
            "UTC", "GMT", "UT", "Z", "Etc/UTC", "Etc/GMT",
            "Asia/Shanghai", "Asia/Chongqing", "Asia/Hong_Kong", "PRC",
            "Asia/Tokyo",
            "Europe/London", "Europe/Paris", "Europe/Berlin",
            "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles",
            "Australia/Sydney", "Pacific/Auckland"
        )

        actual fun of(zoneId: String): ZoneId {
            // [Nullability Decision] zoneId is non-null, matching JDK 8 ZoneId.of(String)
            if (zoneId.isEmpty()) {
                throw DateTimeException("Invalid ID for ZoneId, empty string")
            }
            if (zoneId.length == 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
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
                return ZoneRegion(prefix, ZoneRuleSpec(ZoneOffset.UTC, ZoneOffset.UTC, DstRuleKind.NONE))
            }
            val next = zoneId[prefixLength]
            if (next != '+' && next != '-') {
                return regionOf(zoneId)
            }
            val offset = ZoneOffset.of(zoneId.substring(prefixLength))
            val spec = ZoneRuleSpec(offset, offset, DstRuleKind.NONE)
            return if (offset.totalSeconds == 0) {
                ZoneRegion(prefix, spec)
            } else {
                ZoneRegion(prefix + offset.id, spec)
            }
        }

        internal fun regionOf(zoneId: String): ZoneId {
            val spec = namedRuleSpec(zoneId)
                ?: throw DateTimeException("Unknown time-zone ID: $zoneId")
            return ZoneRegion(zoneId, spec)
        }

        internal fun namedRuleSpec(id: String): ZoneRuleSpec? {
            return when (id) {
                "UTC", "GMT", "UT", "Etc/UTC", "Etc/GMT" ->
                    ZoneRuleSpec(ZoneOffset.UTC, ZoneOffset.UTC, DstRuleKind.NONE)
                "Asia/Shanghai", "Asia/Chongqing", "Asia/Hong_Kong", "PRC", "CTT" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(8), ZoneOffset.ofHours(8), DstRuleKind.NONE)
                "Asia/Tokyo", "JST" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(9), ZoneOffset.ofHours(9), DstRuleKind.NONE)
                "America/New_York" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-5), ZoneOffset.ofHours(-4), DstRuleKind.US)
                "America/Chicago" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-6), ZoneOffset.ofHours(-5), DstRuleKind.US)
                "America/Denver" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-7), ZoneOffset.ofHours(-6), DstRuleKind.US)
                "America/Los_Angeles" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-8), ZoneOffset.ofHours(-7), DstRuleKind.US)
                "EST" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-5), ZoneOffset.ofHours(-5), DstRuleKind.NONE)
                "CST" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-6), ZoneOffset.ofHours(-6), DstRuleKind.NONE)
                "MST" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-7), ZoneOffset.ofHours(-7), DstRuleKind.NONE)
                "PST" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(-8), ZoneOffset.ofHours(-8), DstRuleKind.NONE)
                "Europe/London" ->
                    ZoneRuleSpec(ZoneOffset.UTC, ZoneOffset.ofHours(1), DstRuleKind.EU)
                "Europe/Paris", "Europe/Berlin" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(1), ZoneOffset.ofHours(2), DstRuleKind.EU)
                "CET" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(1), ZoneOffset.ofHours(1), DstRuleKind.NONE)
                "Australia/Sydney" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(10), ZoneOffset.ofHours(11), DstRuleKind.AU)
                "Pacific/Auckland" ->
                    ZoneRuleSpec(ZoneOffset.ofHours(12), ZoneOffset.ofHours(13), DstRuleKind.NZ)
                else -> null
            }
        }
    }
}

internal class ZoneRegion(
    regionId: String,
    private val ruleSpec: ZoneRuleSpec
) : ZoneId() {
    override val id: String = regionId
    override fun getId(): String = id
    override fun offsetAt(instant: Instant): ZoneOffset = ruleSpec.offsetAtInstant(instant.epochSecond)
    override fun offsetAt(localDateTime: LocalDateTime): ZoneOffset = ruleSpec.resolveLocal(localDateTime, null).second
    override fun resolveLocal(localDateTime: LocalDateTime, preferredOffset: ZoneOffset?): Pair<LocalDateTime, ZoneOffset> =
        ruleSpec.resolveLocal(localDateTime, preferredOffset)
    override fun earlierOffsetAtOverlap(localDateTime: LocalDateTime, current: ZoneOffset): ZoneOffset =
        if (ruleSpec.isOverlap(localDateTime)) ruleSpec.dstOffset else current
    override fun laterOffsetAtOverlap(localDateTime: LocalDateTime, current: ZoneOffset): ZoneOffset =
        if (ruleSpec.isOverlap(localDateTime)) ruleSpec.stdOffset else current
}

