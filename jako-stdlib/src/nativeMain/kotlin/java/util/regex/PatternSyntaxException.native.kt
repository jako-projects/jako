package java.util.regex

actual open class PatternSyntaxException actual constructor(
    private val desc: String?,
    private val regex: String?,
    actual open val index: Int
) : IllegalArgumentException(formatMessage(desc, regex, index)) {
    actual open fun getIndex(): Int = index

    actual open fun getDescription(): String? = desc
    actual open val description: String? get() = getDescription()

    actual open fun getPattern(): String? = regex
    actual open val pattern: String? get() = getPattern()

    actual override val message: String?
        get() = super.message

    companion object {
        private fun formatMessage(desc: String?, regex: String?, index: Int): String {
            val sb = StringBuilder()
            sb.append(desc ?: "")
            if (index >= 0) {
                sb.append(" near index ").append(index)
            }
            if (regex != null) {
                sb.append('\n').append(regex)
                if (index >= 0 && index <= regex.length) {
                    sb.append('\n')
                    repeat(index) { sb.append(' ') }
                    sb.append('^')
                }
            }
            return sb.toString()
        }
    }
}
