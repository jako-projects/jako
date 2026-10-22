package java.text

import java.lang.Cloneable
import java.util.Locale

actual abstract class BreakIterator protected actual constructor() : Cloneable {
    actual open fun clone(): Any = this

    actual abstract fun first(): Int
    actual abstract fun last(): Int
    actual abstract fun next(n: Int): Int
    actual abstract fun next(): Int
    actual abstract fun previous(): Int
    actual abstract fun following(offset: Int): Int
    actual abstract fun preceding(offset: Int): Int

    actual open fun isBoundary(offset: Int): Boolean {
        if (offset == getText().getBeginIndex()) return true
        val foll = following(offset - 1)
        return foll == offset
    }

    actual abstract fun current(): Int
    actual abstract fun getText(): CharacterIterator

    actual open fun setText(newText: String) {
        setText(StringCharacterIterator(newText))
    }

    actual abstract fun setText(newText: CharacterIterator)

    actual companion object {
        actual val DONE: Int = -1

        actual fun getWordInstance(): BreakIterator = getWordInstance(Locale.getDefault())
        actual fun getWordInstance(locale: Locale): BreakIterator =
            SimpleBreakIterator(SimpleBreakIterator.Mode.WORD)

        actual fun getLineInstance(): BreakIterator = getLineInstance(Locale.getDefault())
        actual fun getLineInstance(locale: Locale): BreakIterator =
            SimpleBreakIterator(SimpleBreakIterator.Mode.LINE)

        actual fun getCharacterInstance(): BreakIterator = getCharacterInstance(Locale.getDefault())
        actual fun getCharacterInstance(locale: Locale): BreakIterator =
            SimpleBreakIterator(SimpleBreakIterator.Mode.CHARACTER)

        actual fun getSentenceInstance(): BreakIterator = getSentenceInstance(Locale.getDefault())
        actual fun getSentenceInstance(locale: Locale): BreakIterator =
            SimpleBreakIterator(SimpleBreakIterator.Mode.SENTENCE)

        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
    }
}

internal class SimpleBreakIterator(private val mode: Mode) : BreakIterator() {
    enum class Mode { CHARACTER, WORD, LINE, SENTENCE }

    private var textIter: CharacterIterator = StringCharacterIterator("")
    private var boundaries: IntArray = intArrayOf(0)
    private var boundaryIdx: Int = 0
    private var currentPos: Int = 0

    override fun clone(): Any {
        val copy = SimpleBreakIterator(mode)
        copy.textIter = textIter.clone() as CharacterIterator
        copy.boundaries = boundaries.copyOf()
        copy.boundaryIdx = boundaryIdx
        copy.currentPos = currentPos
        return copy
    }

    override fun getText(): CharacterIterator = textIter

    override fun setText(newText: CharacterIterator) {
        textIter = newText
        boundaries = computeBoundaries(newText)
        boundaryIdx = 0
        currentPos = boundaries[0]
        textIter.setIndex(currentPos)
    }

    override fun first(): Int {
        boundaryIdx = 0
        currentPos = boundaries[0]
        textIter.setIndex(currentPos)
        return currentPos
    }

    override fun last(): Int {
        boundaryIdx = boundaries.lastIndex
        currentPos = boundaries[boundaryIdx]
        textIter.setIndex(currentPos)
        return currentPos
    }

    override fun next(): Int = next(1)

    override fun next(n: Int): Int {
        if (n == 0) return currentPos
        val targetIdx = boundaryIdx + n
        if (targetIdx < 0) {
            first()
            return DONE
        }
        if (targetIdx > boundaries.lastIndex) {
            last()
            return DONE
        }
        boundaryIdx = targetIdx
        currentPos = boundaries[boundaryIdx]
        textIter.setIndex(currentPos)
        return currentPos
    }

    override fun previous(): Int {
        if (boundaryIdx <= 0) {
            first()
            return DONE
        }
        boundaryIdx--
        currentPos = boundaries[boundaryIdx]
        textIter.setIndex(currentPos)
        return currentPos
    }

    override fun following(offset: Int): Int {
        val begin = textIter.getBeginIndex()
        val end = textIter.getEndIndex()
        if (offset < begin || offset > end) {
            throw IllegalArgumentException("offset out of bounds: $offset")
        }
        for (i in boundaries.indices) {
            if (boundaries[i] > offset) {
                boundaryIdx = i
                currentPos = boundaries[i]
                textIter.setIndex(currentPos)
                return currentPos
            }
        }
        last()
        return DONE
    }

    override fun preceding(offset: Int): Int {
        val begin = textIter.getBeginIndex()
        val end = textIter.getEndIndex()
        if (offset < begin || offset > end) {
            throw IllegalArgumentException("offset out of bounds: $offset")
        }
        for (i in boundaries.lastIndex downTo 0) {
            if (boundaries[i] < offset) {
                boundaryIdx = i
                currentPos = boundaries[i]
                textIter.setIndex(currentPos)
                return currentPos
            }
        }
        first()
        return DONE
    }

    override fun isBoundary(offset: Int): Boolean {
        val begin = textIter.getBeginIndex()
        val end = textIter.getEndIndex()
        if (offset < begin || offset > end) {
            throw IllegalArgumentException("offset out of bounds: $offset")
        }
        if (offset == begin) {
            first()
            return true
        }
        return following(offset - 1) == offset
    }

    override fun current(): Int = currentPos

    private fun computeBoundaries(iter: CharacterIterator): IntArray {
        val begin = iter.getBeginIndex()
        val end = iter.getEndIndex()
        if (begin == end) return intArrayOf(begin)
        val sb = StringBuilder(end - begin)
        var c = iter.first()
        while (c != CharacterIterator.DONE) {
            sb.append(c)
            c = iter.next()
        }
        val str = sb.toString()
        val result = ArrayList<Int>()
        result.add(begin)

        when (mode) {
            Mode.CHARACTER -> {
                var i = 0
                while (i < str.length) {
                    val ch = str[i]
                    i += if (ch.isHighSurrogate() && i + 1 < str.length && str[i + 1].isLowSurrogate()) 2 else 1
                    while (i < str.length && str[i].code in 0x0300..0x036F) {
                        i++
                    }
                    result.add(begin + i)
                }
            }
            Mode.WORD -> {
                var i = 0
                while (i < str.length) {
                    val ch = str[i]
                    when {
                        ch.isLetterOrDigit() || ch == '_' -> {
                            i++
                            while (i < str.length) {
                                val nextCh = str[i]
                                if (nextCh.isLetterOrDigit() || nextCh == '_') {
                                    i++
                                } else if ((nextCh == '\'' || nextCh == '.') && i + 1 < str.length && str[i + 1].isLetterOrDigit()) {
                                    i += 2
                                } else {
                                    break
                                }
                            }
                        }
                        ch == ' ' || ch == '\t' -> {
                            while (i < str.length && (str[i] == ' ' || str[i] == '\t')) i++
                        }
                        ch == '\r' -> {
                            i += if (i + 1 < str.length && str[i + 1] == '\n') 2 else 1
                        }
                        else -> i++
                    }
                    result.add(begin + i)
                }
            }
            Mode.LINE -> {
                var i = 0
                while (i < str.length) {
                    while (i < str.length && !str[i].isWhitespace() && str[i] != '-') {
                        i++
                    }
                    if (i < str.length && str[i] == '-') {
                        i++
                    } else {
                        while (i < str.length && str[i].isWhitespace()) {
                            i++
                        }
                    }
                    result.add(begin + i)
                }
            }
            Mode.SENTENCE -> {
                var i = 0
                while (i < str.length) {
                    while (i < str.length && str[i] != '.' && str[i] != '!' && str[i] != '?') {
                        i++
                    }
                    while (i < str.length && (str[i] == '.' || str[i] == '!' || str[i] == '?')) {
                        i++
                    }
                    while (i < str.length && (str[i] == '"' || str[i] == '\'' || str[i] == ')' || str[i] == ']')) {
                        i++
                    }
                    while (i < str.length && str[i].isWhitespace()) {
                        i++
                    }
                    result.add(begin + i)
                }
            }
        }
        if (result.last() != end) {
            result.add(end)
        }
        return result.toIntArray()
    }
}
