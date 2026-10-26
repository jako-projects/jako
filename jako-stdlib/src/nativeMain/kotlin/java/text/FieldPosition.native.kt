package java.text

actual open class FieldPosition actual constructor(actual open val field: Int) {
    private var beginIdx: Int = 0
    private var endIdx: Int = 0

    actual open fun getField(): Int = field
    actual open fun getBeginIndex(): Int = beginIdx
    actual open fun setBeginIndex(bi: Int) { beginIdx = bi }
    actual open fun getEndIndex(): Int = endIdx
    actual open fun setEndIndex(ei: Int) { endIdx = ei }

    actual open var beginIndex: Int
        get() = getBeginIndex()
        set(value) = setBeginIndex(value)
    actual open var endIndex: Int
        get() = getEndIndex()
        set(value) = setEndIndex(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldPosition) return false
        return field == other.field && beginIdx == other.beginIdx && endIdx == other.endIdx
    }

    override fun hashCode(): Int {
        return (field shl 24) or (beginIdx shl 16) or endIdx
    }

    override fun toString(): String {
        return "java.text.FieldPosition[field=$field,attribute=null,beginIndex=$beginIdx,endIndex=$endIdx]"
    }
}
