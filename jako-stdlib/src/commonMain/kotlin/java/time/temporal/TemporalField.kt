@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

/**
 * A field of date-time, such as month-of-year or hour-of-minute.
 */
expect interface TemporalField {
    val isDateBased: Boolean
    val isTimeBased: Boolean
    fun isDateBased(): Boolean
    fun isTimeBased(): Boolean
    fun range(): ValueRange
    fun isSupportedBy(temporal: TemporalAccessor): Boolean
    fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    fun getFrom(temporal: TemporalAccessor): Long
}
