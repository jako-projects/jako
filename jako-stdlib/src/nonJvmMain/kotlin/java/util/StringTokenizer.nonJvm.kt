package java.util

actual open class StringTokenizer : Enumeration<Any> {
    private var currentPosition: Int = 0
    private var newPosition: Int = -1
    private val maxPosition: Int
    private val str: String
    private var delimiters: String
    private val retDelims: Boolean
    private var delimsChanged: Boolean = false

    actual constructor(str: String, delim: String, returnDelims: Boolean) {
        this.str = str
        this.delimiters = delim
        this.retDelims = returnDelims
        this.maxPosition = str.length
    }

    actual constructor(str: String, delim: String) : this(str, delim, false)

    actual constructor(str: String) : this(str, " \t\n\r\u000c", false)

    private fun isDelimiter(codePoint: Char): Boolean {
        return delimiters.indexOf(codePoint) >= 0
    }

    private fun skipDelimiters(startPos: Int): Int {
        var position = startPos
        while (!retDelims && position < maxPosition) {
            if (!isDelimiter(str[position])) break
            position++
        }
        return position
    }

    private fun scanToken(startPos: Int): Int {
        var position = startPos
        while (position < maxPosition) {
            if (isDelimiter(str[position])) {
                if (position == startPos && retDelims) {
                    position++
                }
                break
            }
            position++
        }
        return position
    }

    actual open fun hasMoreTokens(): Boolean {
        newPosition = skipDelimiters(currentPosition)
        return newPosition < maxPosition
    }

    actual open fun nextToken(): String {
        currentPosition = if (newPosition >= 0 && !delimsChanged) newPosition else skipDelimiters(currentPosition)
        delimsChanged = false
        newPosition = -1

        if (currentPosition >= maxPosition) {
            throw NoSuchElementException()
        }

        val start = currentPosition
        currentPosition = scanToken(currentPosition)
        return str.substring(start, currentPosition)
    }

    actual open fun nextToken(delim: String): String {
        delimiters = delim
        delimsChanged = true
        return nextToken()
    }

    actual override fun hasMoreElements(): Boolean = hasMoreTokens()

    actual override fun nextElement(): Any = nextToken()

    actual open fun countTokens(): Int {
        var count = 0
        var currpos = currentPosition
        while (currpos < maxPosition) {
            currpos = skipDelimiters(currpos)
            if (currpos >= maxPosition) break
            currpos = scanToken(currpos)
            count++
        }
        return count
    }
}
