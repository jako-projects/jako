@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

/**
 * Marker for types that can be formatted/parsed via [java.time.format.DateTimeFormatter].
 * JDK 8 [TemporalAccessor] exposes field queries; Native only needs the type relationship.
 */
expect interface TemporalAccessor

/**
 * Amount of time, such as [java.time.Period].
 * [Nullability Decision] interface methods are omitted; Period.plus/minus accept TemporalAmount.
 */
expect interface TemporalAmount
