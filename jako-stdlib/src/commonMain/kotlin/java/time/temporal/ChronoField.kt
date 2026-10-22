@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

/**
 * A standard set of fields.
 */
expect enum class ChronoField : TemporalField {
    NANO_OF_SECOND,
    NANO_OF_DAY,
    MICRO_OF_SECOND,
    MICRO_OF_DAY,
    MILLI_OF_SECOND,
    MILLI_OF_DAY,
    SECOND_OF_MINUTE,
    SECOND_OF_DAY,
    MINUTE_OF_HOUR,
    MINUTE_OF_DAY,
    HOUR_OF_AMPM,
    CLOCK_HOUR_OF_AMPM,
    HOUR_OF_DAY,
    CLOCK_HOUR_OF_DAY,
    AMPM_OF_DAY,
    DAY_OF_WEEK,
    ALIGNED_DAY_OF_WEEK_IN_MONTH,
    ALIGNED_DAY_OF_WEEK_IN_YEAR,
    DAY_OF_MONTH,
    DAY_OF_YEAR,
    EPOCH_DAY,
    ALIGNED_WEEK_OF_MONTH,
    ALIGNED_WEEK_OF_YEAR,
    MONTH_OF_YEAR,
    PROLEPTIC_MONTH,
    YEAR_OF_ERA,
    YEAR,
    ERA,
    INSTANT_SECONDS,
    OFFSET_SECONDS;

    override val isDateBased: Boolean
    override val isTimeBased: Boolean
    override fun isDateBased(): Boolean
    override fun isTimeBased(): Boolean
    override fun range(): ValueRange
    override fun isSupportedBy(temporal: TemporalAccessor): Boolean
    override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    override fun getFrom(temporal: TemporalAccessor): Long
    fun checkValidValue(value: Long): Long
    fun checkValidIntValue(value: Long): Int
}
