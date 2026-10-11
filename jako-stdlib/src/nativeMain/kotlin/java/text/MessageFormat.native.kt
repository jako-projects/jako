package java.text

import java.util.Locale

actual open class MessageFormat : Format {
    private var pattern: String = ""

    actual constructor(pattern: String) : super() {
        applyPattern(pattern)
    }

    actual constructor(pattern: String, locale: Locale) : this(pattern)

    actual open fun applyPattern(pattern: String) {
        this.pattern = pattern
    }

    actual open fun toPattern(): String = pattern

    override fun format(obj: Any?): String {
        val args: Array<out Any?> = when (obj) {
            is Array<*> -> obj
            else -> arrayOf(obj)
        }
        return formatPattern(pattern, args)
    }

    actual companion object {
        actual fun format(pattern: String, vararg arguments: Any?): String {
            return formatPattern(pattern, arguments)
        }
    }
}

private fun formatPattern(pattern: String, arguments: Array<out Any?>): String {
    val out = StringBuilder()
    var i = 0
    while (i < pattern.length) {
        val ch = pattern[i]
        if (ch == '\'') {
            if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                out.append('\'')
                i += 2
            } else {
                i++
                while (i < pattern.length && pattern[i] != '\'') {
                    out.append(pattern[i])
                    i++
                }
                if (i < pattern.length) i++
            }
            continue
        }
        if (ch == '{') {
            val end = pattern.indexOf('}', i + 1)
            if (end < 0) {
                out.append(ch)
                i++
                continue
            }
            val body = pattern.substring(i + 1, end)
            val indexPart = body.substringBefore(',')
            val index = indexPart.trim().toIntOrNull()
            if (index == null) {
                out.append(ch)
                i++
                continue
            }
            if (index < 0 || index >= arguments.size) {
                out.append('{').append(index).append('}')
            } else {
                val value = arguments[index]
                out.append(value?.toString() ?: "null")
            }
            i = end + 1
            continue
        }
        out.append(ch)
        i++
    }
    return out.toString()
}
