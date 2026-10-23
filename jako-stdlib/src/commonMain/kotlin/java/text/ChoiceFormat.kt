@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect open class ChoiceFormat : NumberFormat {
    constructor(newPattern: String)
    constructor(limits: DoubleArray, formats: Array<String>)

    open fun applyPattern(newPattern: String)
    open fun toPattern(): String
    open fun setChoices(limits: DoubleArray, formats: Array<String>)
    open fun getLimits(): DoubleArray
    open fun getFormats(): Array<Any>
    override fun format(number: Long, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder
    override fun format(number: Double, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder
    override fun parse(text: String, status: ParsePosition): Number?

    companion object {
        fun nextDouble(d: Double): Double
        fun previousDouble(d: Double): Double
        fun nextDouble(d: Double, positive: Boolean): Double
    }
}
