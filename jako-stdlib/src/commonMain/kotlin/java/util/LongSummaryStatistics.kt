@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class LongSummaryStatistics() : java.util.function.LongConsumer, java.util.function.IntConsumer {
    override fun accept(value: Int)
    override fun accept(value: Long)
    open fun combine(other: LongSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Long
    val sum: Long
    fun getMin(): Long
    val min: Long
    fun getMax(): Long
    val max: Long
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}
