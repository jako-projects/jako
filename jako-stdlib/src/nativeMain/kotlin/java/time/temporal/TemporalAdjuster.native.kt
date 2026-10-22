package java.time.temporal

import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual fun interface TemporalAdjuster {
    actual fun adjustInto(temporal: Temporal): Temporal
}
