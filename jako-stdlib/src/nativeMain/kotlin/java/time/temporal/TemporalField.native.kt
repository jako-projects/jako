package java.time.temporal

import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual interface TemporalField {
    actual val isDateBased: Boolean
    actual val isTimeBased: Boolean
    actual fun isDateBased(): Boolean
    actual fun isTimeBased(): Boolean
    actual fun range(): ValueRange
    actual fun isSupportedBy(temporal: TemporalAccessor): Boolean
    actual fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    actual fun getFrom(temporal: TemporalAccessor): Long
}
