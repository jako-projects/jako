@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class DoubleSummaryStatistics() : java.util.function.DoubleConsumer {
    override fun accept(value: Double)
    open fun combine(other: DoubleSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Double
    val sum: Double
    fun getMin(): Double
    val min: Double
    fun getMax(): Double
    val max: Double
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}
