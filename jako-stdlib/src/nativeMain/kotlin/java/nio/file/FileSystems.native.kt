package java.nio.file

import java.lang.System

internal object DefaultFileSystem : FileSystem() {
    override fun getPath(first: String, vararg more: String): Path = Paths.get(first, *more)

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        val pos = syntaxAndPattern.indexOf(':')
        if (pos <= 0) {
            throw IllegalArgumentException("Pattern must be of the form 'syntax:pattern'")
        }
        val syntax = syntaxAndPattern.substring(0, pos)
        val input = syntaxAndPattern.substring(pos + 1)
        val expr = when {
            syntax.equals("glob", ignoreCase = true) -> globToRegex(input)
            syntax.equals("regex", ignoreCase = true) -> input
            else -> throw UnsupportedOperationException("Syntax '$syntax' not recognized")
        }
        val regex = Regex(expr)
        return PathMatcher { path ->
            regex.matches(path.toString().replace('\\', '/'))
        }
    }

    override fun getSeparator(): String = System.getProperty("file.separator", "/")

    override fun isOpen(): Boolean = true

    override fun close() {
        throw UnsupportedOperationException()
    }
}

internal fun globToRegex(globPattern: String): String {
    val sb = StringBuilder("^")
    var inGroup = 0
    var inClass = false
    var firstIndexInClass = -1
    var i = 0
    val len = globPattern.length
    while (i < len) {
        val c = globPattern[i++]
        when (c) {
            '\\' -> {
                if (i == len) throw IllegalArgumentException("No character to escape at end of pattern")
                val next = globPattern[i++]
                if (next == '\\' || next == '/' || isRegexMeta(next) || isGlobMeta(next)) {
                    sb.append('\\')
                }
                sb.append(next)
            }
            '/' -> sb.append('/')
            '[' -> {
                if (inClass) {
                    sb.append("\\[")
                } else {
                    sb.append('[')
                    inClass = true
                    firstIndexInClass = i
                    if (i < len && globPattern[i] == '^') {
                        sb.append("\\^")
                        i++
                    } else if (i < len && globPattern[i] == '!') {
                        sb.append('^')
                        i++
                    }
                    if (i < len && globPattern[i] == '-') {
                        sb.append('-')
                        i++
                    }
                }
            }
            ']' -> {
                if (inClass && i > firstIndexInClass + 1) {
                    sb.append(']')
                    inClass = false
                } else if (inClass) {
                    sb.append("\\]")
                } else {
                    sb.append(']')
                }
            }
            '{' -> {
                if (inClass) {
                    sb.append("{")
                } else {
                    inGroup++
                    sb.append("(?:")
                }
            }
            '}' -> {
                if (inClass) {
                    sb.append("}")
                } else if (inGroup > 0) {
                    inGroup--
                    sb.append(")")
                } else {
                    sb.append("}")
                }
            }
            ',' -> {
                if (inGroup > 0 && !inClass) {
                    sb.append('|')
                } else {
                    sb.append(',')
                }
            }
            '*' -> {
                if (inClass) {
                    sb.append('*')
                } else if (i < len && globPattern[i] == '*') {
                    i++
                    sb.append(".*")
                } else {
                    sb.append("[^/]*")
                }
            }
            '?' -> {
                if (inClass) sb.append('?') else sb.append("[^/]")
            }
            else -> {
                if (!inClass && isRegexMeta(c)) {
                    sb.append('\\')
                }
                sb.append(c)
            }
        }
    }
    if (inClass) throw IllegalArgumentException("Unclosed character class in glob: $globPattern")
    if (inGroup > 0) throw IllegalArgumentException("Unclosed group in glob: $globPattern")
    sb.append('$')
    return sb.toString()
}

internal fun isRegexMeta(c: Char): Boolean = c in ".^$+{[]|()"

internal fun isGlobMeta(c: Char): Boolean = c in "\\*?[{"

actual class FileSystems {
    actual companion object {
        actual fun getDefault(): FileSystem = DefaultFileSystem
    }
}
