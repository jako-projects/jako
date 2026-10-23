package java.text

actual class ParsePosition actual constructor(actual var index: Int) {
    actual constructor() : this(0)
    actual fun getIndex(): Int = index
    actual fun setIndex(index: Int) { this.index = index }
    actual fun getErrorIndex(): Int = errorIndex
    actual var errorIndex: Int = -1
}
