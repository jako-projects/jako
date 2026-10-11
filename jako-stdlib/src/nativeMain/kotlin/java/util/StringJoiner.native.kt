package java.util

actual class StringJoiner actual constructor(
    delimiter: CharSequence,
    prefix: CharSequence,
    suffix: CharSequence
) {
    private val prefixStr: String = prefix.toString()
    private val delimiterStr: String = delimiter.toString()
    private val suffixStr: String = suffix.toString()
    private var emptyValueStr: String = "$prefixStr$suffixStr"
    private var value: StringBuilder? = null

    actual constructor(delimiter: CharSequence) : this(delimiter, "", "")

    actual fun setEmptyValue(emptyValue: CharSequence): StringJoiner {
        this.emptyValueStr = emptyValue.toString()
        return this
    }

    actual fun add(newElement: CharSequence?): StringJoiner {
        val elt = newElement?.toString() ?: "null"
        prepareBuilder().append(elt)
        return this
    }

    private fun prepareBuilder(): StringBuilder {
        val current = value
        if (current != null) {
            current.append(delimiterStr)
            return current
        } else {
            val created = StringBuilder(prefixStr)
            value = created
            return created
        }
    }

    actual fun merge(other: StringJoiner): StringJoiner {
        val otherVal = other.value
        if (otherVal != null) {
            val length = otherVal.length
            val sub = otherVal.subSequence(other.prefixStr.length, length)
            prepareBuilder().append(sub)
        }
        return this
    }

    actual fun length(): Int {
        val current = value
        return if (current != null) {
            current.length + suffixStr.length
        } else {
            emptyValueStr.length
        }
    }

    actual override fun toString(): String {
        val current = value
        return if (current != null) {
            current.toString() + suffixStr
        } else {
            emptyValueStr
        }
    }
}
