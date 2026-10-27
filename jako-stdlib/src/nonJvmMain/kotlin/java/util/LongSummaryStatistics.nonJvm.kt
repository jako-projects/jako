package java.util

import java.util.function.IntConsumer
import java.util.function.LongConsumer

actual open class LongSummaryStatistics actual constructor() : LongConsumer, IntConsumer {
    private var countVal: Long = 0L
    private var sumVal: Long = 0L
    private var minVal: Long = Long.MAX_VALUE
    private var maxVal: Long = Long.MIN_VALUE

    actual override fun accept(value: Int) {
        accept(value.toLong())
    }

    actual override fun accept(value: Long) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: LongSummaryStatistics) {
        countVal += other.countVal
        sumVal += other.sumVal
        minVal = minOf(minVal, other.minVal)
        maxVal = maxOf(maxVal, other.maxVal)
    }

    actual fun getCount(): Long = countVal
    actual val count: Long
        get() = countVal

    actual fun getSum(): Long = sumVal
    actual val sum: Long
        get() = sumVal

    actual fun getMin(): Long = minVal
    actual val min: Long
        get() = minVal

    actual fun getMax(): Long = maxVal
    actual val max: Long
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal.toDouble() / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "LongSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}
