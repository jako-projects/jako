@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IntSummaryStatistics() : java.util.function.IntConsumer {
    override fun accept(value: Int)
    open fun combine(other: IntSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Long
    val sum: Long
    fun getMin(): Int
    val min: Int
    fun getMax(): Int
    val max: Int
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}
