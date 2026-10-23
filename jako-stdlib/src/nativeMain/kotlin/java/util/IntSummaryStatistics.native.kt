package java.util

import java.util.function.IntConsumer

actual open class IntSummaryStatistics actual constructor() : IntConsumer {
    private var countVal: Long = 0L
    private var sumVal: Long = 0L
    private var minVal: Int = Int.MAX_VALUE
    private var maxVal: Int = Int.MIN_VALUE

    actual override fun accept(value: Int) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: IntSummaryStatistics) {
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

    actual fun getMin(): Int = minVal
    actual val min: Int
        get() = minVal

    actual fun getMax(): Int = maxVal
    actual val max: Int
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal.toDouble() / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "IntSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}
