@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class StringJoiner {
    constructor(delimiter: CharSequence)
    constructor(delimiter: CharSequence, prefix: CharSequence, suffix: CharSequence)

    fun setEmptyValue(emptyValue: CharSequence): StringJoiner
    fun add(newElement: CharSequence?): StringJoiner
    fun merge(other: StringJoiner): StringJoiner
    fun length(): Int
    override fun toString(): String
}
