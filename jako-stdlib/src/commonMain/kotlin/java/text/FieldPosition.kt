@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect open class FieldPosition {
    constructor(field: Int)
    open fun getField(): Int
    open fun getBeginIndex(): Int
    open fun setBeginIndex(bi: Int)
    open fun getEndIndex(): Int
    open fun setEndIndex(ei: Int)
    open val field: Int
    open var beginIndex: Int
    open var endIndex: Int
}
