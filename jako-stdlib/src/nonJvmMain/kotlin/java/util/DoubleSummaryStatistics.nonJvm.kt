package java.util

import java.util.function.DoubleConsumer

actual open class DoubleSummaryStatistics actual constructor() : DoubleConsumer {
    private var countVal: Long = 0L
    private var sumVal: Double = 0.0
    private var minVal: Double = Double.POSITIVE_INFINITY
    private var maxVal: Double = Double.NEGATIVE_INFINITY

    actual override fun accept(value: Double) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: DoubleSummaryStatistics) {
        countVal += other.countVal
        sumVal += other.sumVal
        minVal = minOf(minVal, other.minVal)
        maxVal = maxOf(maxVal, other.maxVal)
    }

    actual fun getCount(): Long = countVal
    actual val count: Long
        get() = countVal

    actual fun getSum(): Double = sumVal
    actual val sum: Double
        get() = sumVal

    actual fun getMin(): Double = minVal
    actual val min: Double
        get() = minVal

    actual fun getMax(): Double = maxVal
    actual val max: Double
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "DoubleSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}
