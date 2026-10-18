package java.text

import java.io.Serializable
import java.lang.Cloneable
import java.util.Locale

actual open class DecimalFormatSymbols : Cloneable, Serializable {
    private var zeroDigit: Char = '0'
    private var groupingSeparator: Char = ','
    private var decimalSeparator: Char = '.'
    private var perMill: Char = '\u2030'
    private var percent: Char = '%'
    private var digit: Char = '#'
    private var patternSeparator: Char = ';'
    private var infinity: String = "\u221E"
    private var nan: String = "NaN"
    private var minusSign: Char = '-'
    private var currencySymbol: String = "$"
    private var intlCurrencySymbol: String = "USD"
    private var monetarySeparator: Char = '.'
    private var exponentSeparator: String = "E"

    actual constructor() : this(Locale.getDefault())

    actual constructor(locale: Locale) {
        when (locale.getLanguage()) {
            "de", "fr", "es", "it" -> {
                decimalSeparator = ','
                monetarySeparator = ','
                groupingSeparator = if (locale.getLanguage() == "fr") '\u00A0' else '.'
                currencySymbol = "€"
                intlCurrencySymbol = "EUR"
            }
            "zh" -> {
                currencySymbol = "￥"
                intlCurrencySymbol = "CNY"
            }
            "ja" -> {
                currencySymbol = "￥"
                intlCurrencySymbol = "JPY"
            }
            "en" -> {
                if (locale.getCountry() == "GB") {
                    currencySymbol = "£"
                    intlCurrencySymbol = "GBP"
                }
            }
        }
    }

    actual open fun getZeroDigit(): Char = zeroDigit
    actual open fun setZeroDigit(zeroDigit: Char) { this.zeroDigit = zeroDigit }

    actual open fun getGroupingSeparator(): Char = groupingSeparator
    actual open fun setGroupingSeparator(groupingSeparator: Char) { this.groupingSeparator = groupingSeparator }

    actual open fun getDecimalSeparator(): Char = decimalSeparator
    actual open fun setDecimalSeparator(decimalSeparator: Char) { this.decimalSeparator = decimalSeparator }

    actual open fun getPerMill(): Char = perMill
    actual open fun setPerMill(perMill: Char) { this.perMill = perMill }

    actual open fun getPercent(): Char = percent
    actual open fun setPercent(percent: Char) { this.percent = percent }

    actual open fun getDigit(): Char = digit
    actual open fun setDigit(digit: Char) { this.digit = digit }

    actual open fun getPatternSeparator(): Char = patternSeparator
    actual open fun setPatternSeparator(patternSeparator: Char) { this.patternSeparator = patternSeparator }

    actual open fun getInfinity(): String = infinity
    actual open fun setInfinity(infinity: String) { this.infinity = infinity }

    actual open fun getNaN(): String = nan
    actual open fun setNaN(NaN: String) { this.nan = NaN }

    actual open fun getMinusSign(): Char = minusSign
    actual open fun setMinusSign(minusSign: Char) { this.minusSign = minusSign }

    actual open fun getCurrencySymbol(): String = currencySymbol
    actual open fun setCurrencySymbol(currency: String) { this.currencySymbol = currency }

    actual open fun getInternationalCurrencySymbol(): String = intlCurrencySymbol
    actual open fun setInternationalCurrencySymbol(currencyCode: String) { this.intlCurrencySymbol = currencyCode }

    actual open fun getMonetaryDecimalSeparator(): Char = monetarySeparator
    actual open fun setMonetaryDecimalSeparator(sep: Char) { this.monetarySeparator = sep }

    actual open fun getExponentSeparator(): String = exponentSeparator
    actual open fun setExponentSeparator(exp: String) {
        this.exponentSeparator = exp
    }

    actual open fun clone(): Any {
        val copy = DecimalFormatSymbols()
        copy.zeroDigit = zeroDigit
        copy.groupingSeparator = groupingSeparator
        copy.decimalSeparator = decimalSeparator
        copy.perMill = perMill
        copy.percent = percent
        copy.digit = digit
        copy.patternSeparator = patternSeparator
        copy.infinity = infinity
        copy.nan = nan
        copy.minusSign = minusSign
        copy.currencySymbol = currencySymbol
        copy.intlCurrencySymbol = intlCurrencySymbol
        copy.monetarySeparator = monetarySeparator
        copy.exponentSeparator = exponentSeparator
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecimalFormatSymbols) return false
        return zeroDigit == other.zeroDigit &&
            groupingSeparator == other.groupingSeparator &&
            decimalSeparator == other.decimalSeparator &&
            percent == other.percent &&
            perMill == other.perMill &&
            digit == other.digit &&
            minusSign == other.minusSign &&
            patternSeparator == other.patternSeparator &&
            infinity == other.infinity &&
            nan == other.nan &&
            currencySymbol == other.currencySymbol &&
            intlCurrencySymbol == other.intlCurrencySymbol &&
            monetarySeparator == other.monetarySeparator &&
            exponentSeparator == other.exponentSeparator
    }

    override fun hashCode(): Int {
        var result = zeroDigit.hashCode()
        result = 31 * result + groupingSeparator.hashCode()
        result = 31 * result + decimalSeparator.hashCode()
        return result
    }

    actual companion object {
        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
        actual fun getInstance(): DecimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault())
        actual fun getInstance(locale: Locale): DecimalFormatSymbols = DecimalFormatSymbols(locale)
    }
}

actual open class DateFormatSymbols : Serializable, Cloneable {
    private var eras: Array<String> = arrayOf("BC", "AD")
    private var months: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December", ""
    )
    private var shortMonths: Array<String> = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""
    )
    private var weekdays: Array<String> = arrayOf(
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )
    private var shortWeekdays: Array<String> = arrayOf(
        "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    )
    private var ampms: Array<String> = arrayOf("AM", "PM")
    private var zoneStrings: Array<Array<String>> = arrayOf(
        arrayOf("UTC", "Coordinated Universal Time", "UTC", "Coordinated Universal Time", "UTC")
    )
    private var localPatternChars: String = "GyMdkHmsSEDFwWahKzZ"

    actual constructor() : this(Locale.getDefault())

    actual constructor(locale: Locale)

    actual open fun getEras(): Array<String> = eras.copyOf()
    actual open fun setEras(newEras: Array<String>) { eras = newEras.copyOf() }

    actual open fun getMonths(): Array<String> = months.copyOf()
    actual open fun setMonths(newMonths: Array<String>) { months = newMonths.copyOf() }

    actual open fun getShortMonths(): Array<String> = shortMonths.copyOf()
    actual open fun setShortMonths(newShortMonths: Array<String>) { shortMonths = newShortMonths.copyOf() }

    actual open fun getWeekdays(): Array<String> = weekdays.copyOf()
    actual open fun setWeekdays(newWeekdays: Array<String>) { weekdays = newWeekdays.copyOf() }

    actual open fun getShortWeekdays(): Array<String> = shortWeekdays.copyOf()
    actual open fun setShortWeekdays(newShortWeekdays: Array<String>) { shortWeekdays = newShortWeekdays.copyOf() }

    actual open fun getAmPmStrings(): Array<String> = ampms.copyOf()
    actual open fun setAmPmStrings(newAmpms: Array<String>) { ampms = newAmpms.copyOf() }

    actual open fun getZoneStrings(): Array<Array<String>> =
        Array(zoneStrings.size) { zoneStrings[it].copyOf() }

    actual open fun setZoneStrings(newZoneStrings: Array<Array<String>>) {
        for (row in newZoneStrings) {
            if (row.size < 5) throw IllegalArgumentException("Each zone string row must have at least 5 elements")
        }
        zoneStrings = Array(newZoneStrings.size) { newZoneStrings[it].copyOf() }
    }

    actual open fun getLocalPatternChars(): String = localPatternChars
    actual open fun setLocalPatternChars(newLocalPatternChars: String) {
        localPatternChars = newLocalPatternChars
    }

    actual open fun clone(): Any {
        val copy = DateFormatSymbols()
        copy.eras = eras.copyOf()
        copy.months = months.copyOf()
        copy.shortMonths = shortMonths.copyOf()
        copy.weekdays = weekdays.copyOf()
        copy.shortWeekdays = shortWeekdays.copyOf()
        copy.ampms = ampms.copyOf()
        copy.zoneStrings = Array(zoneStrings.size) { zoneStrings[it].copyOf() }
        copy.localPatternChars = localPatternChars
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateFormatSymbols) return false
        return eras.contentEquals(other.eras) &&
            months.contentEquals(other.months) &&
            shortMonths.contentEquals(other.shortMonths) &&
            weekdays.contentEquals(other.weekdays) &&
            shortWeekdays.contentEquals(other.shortWeekdays) &&
            ampms.contentEquals(other.ampms) &&
            localPatternChars == other.localPatternChars
    }

    override fun hashCode(): Int = months.contentHashCode()

    actual companion object {
        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
        actual fun getInstance(): DateFormatSymbols = DateFormatSymbols(Locale.getDefault())
        actual fun getInstance(locale: Locale): DateFormatSymbols = DateFormatSymbols(locale)
    }
}

actual interface CharacterIterator : Cloneable {
    actual fun first(): Char
    actual fun last(): Char
    actual fun current(): Char
    actual fun next(): Char
    actual fun previous(): Char
    actual fun setIndex(position: Int): Char
    actual fun getBeginIndex(): Int
    actual fun getEndIndex(): Int
    actual fun getIndex(): Int
    actual fun clone(): Any

    actual companion object {
        actual val DONE: Char = '\uFFFF'
    }
}

actual class StringCharacterIterator : CharacterIterator {
    private var text: String
    private var begin: Int
    private var end: Int
    private var pos: Int

    actual constructor(text: String) : this(text, 0)

    actual constructor(text: String, pos: Int) : this(text, 0, text.length, pos)

    actual constructor(text: String, begin: Int, end: Int, pos: Int) {
        if (begin < 0 || begin > end || end > text.length) {
            throw IllegalArgumentException("Invalid substring range")
        }
        if (pos < begin || pos > end) {
            throw IllegalArgumentException("Invalid position")
        }
        this.text = text
        this.begin = begin
        this.end = end
        this.pos = pos
    }

    actual fun setText(text: String) {
        this.text = text
        this.begin = 0
        this.end = text.length
        this.pos = 0
    }

    actual override fun first(): Char {
        pos = begin
        return current()
    }

    actual override fun last(): Char {
        pos = if (end != begin) end - 1 else end
        return current()
    }

    actual override fun setIndex(p: Int): Char {
        if (p < begin || p > end) throw IllegalArgumentException("Invalid index")
        pos = p
        return current()
    }

    actual override fun current(): Char =
        if (pos >= begin && pos < end) text[pos] else CharacterIterator.DONE

    actual override fun next(): Char {
        if (pos < end - 1) {
            pos++
            return text[pos]
        } else {
            pos = end
            return CharacterIterator.DONE
        }
    }

    actual override fun previous(): Char {
        if (pos > begin) {
            pos--
            return text[pos]
        } else {
            return CharacterIterator.DONE
        }
    }

    actual override fun getBeginIndex(): Int = begin

    actual override fun getEndIndex(): Int = end

    actual override fun getIndex(): Int = pos

    actual override fun clone(): Any = StringCharacterIterator(text, begin, end, pos)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringCharacterIterator) return false
        return text == other.text && pos == other.pos && begin == other.begin && end == other.end
    }

    override fun hashCode(): Int = text.hashCode() xor pos xor begin xor end
}

actual abstract class CollationKey protected actual constructor(
    source: String?
) : Comparable<CollationKey> {
    private val sourceStr: String = source ?: throw NullPointerException()

    actual open fun getSourceString(): String = sourceStr

    actual abstract override fun compareTo(other: CollationKey): Int

    actual abstract fun toByteArray(): ByteArray
}

private class NativeCollationKey(
    source: String,
    private val keyBytes: ByteArray
) : CollationKey(source) {
    override fun compareTo(other: CollationKey): Int {
        val otherBytes = (other as? NativeCollationKey)?.keyBytes ?: other.toByteArray()
        val minLen = minOf(keyBytes.size, otherBytes.size)
        for (i in 0 until minLen) {
            val a = keyBytes[i].toInt() and 0xFF
            val b = otherBytes[i].toInt() and 0xFF
            if (a != b) return a.compareTo(b)
        }
        return keyBytes.size.compareTo(otherBytes.size)
    }

    override fun toByteArray(): ByteArray = keyBytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollationKey) return false
        return compareTo(other) == 0
    }

    override fun hashCode(): Int = keyBytes.contentHashCode()
}

actual abstract class Collator protected actual constructor() : Comparator<Any>, Cloneable {
    private var strengthValue: Int = TERTIARY
    private var decompositionValue: Int = CANONICAL_DECOMPOSITION

    actual abstract fun compare(source: String, target: String): Int

    actual open override fun compare(o1: Any, o2: Any): Int =
        compare(o1 as String, o2 as String)

    actual abstract fun getCollationKey(source: String): CollationKey

    actual open fun equals(source: String, target: String): Boolean =
        compare(source, target) == 0

    actual open fun getStrength(): Int = strengthValue

    actual open fun setStrength(newStrength: Int) {
        if (newStrength != PRIMARY && newStrength != SECONDARY && newStrength != TERTIARY && newStrength != IDENTICAL) {
            throw IllegalArgumentException("Incorrect comparison level.")
        }
        strengthValue = newStrength
    }

    actual open fun getDecomposition(): Int = decompositionValue

    actual open fun setDecomposition(decompositionMode: Int) {
        if (decompositionMode != NO_DECOMPOSITION &&
            decompositionMode != CANONICAL_DECOMPOSITION &&
            decompositionMode != FULL_DECOMPOSITION
        ) {
            throw IllegalArgumentException("Wrong decomposition mode.")
        }
        decompositionValue = decompositionMode
    }

    actual open fun clone(): Any {
        val copy = RuleBasedNativeCollator()
        copy.setStrength(strengthValue)
        copy.setDecomposition(decompositionValue)
        return copy
    }

    actual companion object {
        actual val PRIMARY: Int = 0
        actual val SECONDARY: Int = 1
        actual val TERTIARY: Int = 2
        actual val IDENTICAL: Int = 3
        actual val NO_DECOMPOSITION: Int = 0
        actual val CANONICAL_DECOMPOSITION: Int = 1
        actual val FULL_DECOMPOSITION: Int = 2

        actual fun getInstance(): Collator = getInstance(Locale.getDefault())

        actual fun getInstance(desiredLocale: Locale): Collator = RuleBasedNativeCollator()

        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
    }
}

private class RuleBasedNativeCollator : Collator() {
    private fun normalizeInput(s: String): String = when (getDecomposition()) {
        NO_DECOMPOSITION -> s
        CANONICAL_DECOMPOSITION -> Normalizer.normalize(s, Normalizer.Form.NFD)
        else -> Normalizer.normalize(s, Normalizer.Form.NFKD)
    }

    private fun stripCombiningMarks(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        val sb = StringBuilder(nfd.length)
        for (ch in nfd) {
            if (ch.code !in 0x0300..0x036F) {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    override fun compare(source: String, target: String): Int {
        val strength = getStrength()
        val normSource = normalizeInput(source)
        val normTarget = normalizeInput(target)

        // Level 1 (PRIMARY): base characters, case-insensitive, stripped of accents
        val base1 = stripCombiningMarks(normSource).lowercase()
        val base2 = stripCombiningMarks(normTarget).lowercase()
        val cmp1 = base1.compareTo(base2)
        if (cmp1 != 0 || strength == PRIMARY) return cmp1

        // Level 2 (SECONDARY): accents/diacritics, case-insensitive
        val sec1 = normSource.lowercase()
        val sec2 = normTarget.lowercase()
        val cmp2 = sec1.compareTo(sec2)
        if (cmp2 != 0 || strength == SECONDARY) return cmp2

        // Level 3 (TERTIARY): case difference (lowercase < uppercase for same letter)
        val minLen = minOf(normSource.length, normTarget.length)
        for (i in 0 until minLen) {
            val c1 = normSource[i]
            val c2 = normTarget[i]
            if (c1 != c2) {
                val l1 = c1.lowercaseChar()
                val l2 = c2.lowercaseChar()
                if (l1 == l2) {
                    // Lowercase sorts before uppercase in standard collation
                    return if (c1.isLowerCase() && c2.isUpperCase()) -1
                    else if (c1.isUpperCase() && c2.isLowerCase()) 1
                    else c1.compareTo(c2)
                }
                return c1.compareTo(c2)
            }
        }
        val lenCmp = normSource.length.compareTo(normTarget.length)
        if (lenCmp != 0 || strength == TERTIARY) return lenCmp

        // Level 4 (IDENTICAL): raw codepoint comparison
        return source.compareTo(target)
    }

    override fun getCollationKey(source: String): CollationKey {
        val strength = getStrength()
        val norm = normalizeInput(source)
        val base = stripCombiningMarks(norm).lowercase()
        val out = ArrayList<Byte>()

        fun appendUtf8(s: String) {
            for (b in s.encodeToByteArray()) {
                out.add(if (b == 0.toByte()) 1.toByte() else b)
            }
        }

        appendUtf8(base)
        if (strength >= SECONDARY) {
            out.add(0)
            appendUtf8(norm.lowercase())
        }
        if (strength >= TERTIARY) {
            out.add(0)
            for (ch in norm) {
                out.add(if (ch.isUpperCase()) 2.toByte() else 1.toByte())
            }
        }
        if (strength >= IDENTICAL) {
            out.add(0)
            appendUtf8(source)
        }
        return NativeCollationKey(source, out.toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleBasedNativeCollator) return false
        return getStrength() == other.getStrength() && getDecomposition() == other.getDecomposition()
    }

    override fun hashCode(): Int = getStrength() * 31 + getDecomposition()
}

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

private class SimpleBreakIterator(private val mode: Mode) : BreakIterator() {
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

actual open class ChoiceFormat : NumberFormat {
    private var choiceLimits: DoubleArray = DoubleArray(0)
    private var choiceFormats: Array<String> = emptyArray()

    actual constructor(newPattern: String) : super() {
        applyPattern(newPattern)
    }

    actual constructor(limits: DoubleArray, formats: Array<String>) : super() {
        setChoices(limits, formats)
    }

    actual open fun setChoices(limits: DoubleArray, formats: Array<String>) {
        if (limits.size != formats.size) {
            throw IllegalArgumentException("Array and limit arrays must be of the same length.")
        }
        choiceLimits = limits.copyOf()
        choiceFormats = formats.copyOf()
    }

    actual open fun getLimits(): DoubleArray = choiceLimits.copyOf()

    @Suppress("UNCHECKED_CAST")
    actual open fun getFormats(): Array<Any> = Array(choiceFormats.size) { choiceFormats[it] }

    actual open fun applyPattern(newPattern: String) {
        val segments = newPattern.split('|')
        val limits = ArrayList<Double>()
        val formats = ArrayList<String>()
        for (seg in segments) {
            if (seg.isEmpty()) continue
            var sepIdx = -1
            var sepChar = '#'
            for (i in seg.indices) {
                val c = seg[i]
                if (c == '#' || c == '<' || c == '\u2264') {
                    sepIdx = i
                    sepChar = c
                    break
                }
            }
            if (sepIdx < 0) throw IllegalArgumentException("Invalid ChoiceFormat pattern: $newPattern")
            val numStr = seg.substring(0, sepIdx).trim()
            val fmtStr = seg.substring(sepIdx + 1)
            val baseLimit = when (numStr) {
                "\u221E", "Infinity" -> Double.POSITIVE_INFINITY
                "-\u221E", "-Infinity" -> Double.NEGATIVE_INFINITY
                else -> numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid limit: $numStr")
            }
            val limit = if (sepChar == '<') nextDouble(baseLimit) else baseLimit
            if (limits.isNotEmpty() && limit < limits.last()) {
                throw IllegalArgumentException("ChoiceFormat limits must be in ascending order")
            }
            limits.add(limit)
            formats.add(fmtStr)
        }
        choiceLimits = limits.toDoubleArray()
        choiceFormats = formats.toTypedArray()
    }

    actual open fun toPattern(): String {
        val sb = StringBuilder()
        for (i in choiceLimits.indices) {
            if (i > 0) sb.append('|')
            val lim = choiceLimits[i]
            val prev = previousDouble(lim)
            val tryLess = prev.toString()
            val tryHash = lim.toString()
            if (nextDouble(prev) == lim && tryLess.length < tryHash.length) {
                sb.append(tryLess).append('<')
            } else {
                when {
                    lim == Double.POSITIVE_INFINITY -> sb.append('\u221E')
                    lim == Double.NEGATIVE_INFINITY -> sb.append("-\u221E")
                    lim == lim.toLong().toDouble() -> sb.append("${lim.toLong()}.0")
                    else -> sb.append(tryHash)
                }
                sb.append('#')
            }
            sb.append(choiceFormats[i])
        }
        return sb.toString()
    }

    actual override fun format(number: Long, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder =
        format(number.toDouble(), toAppendTo, status)

    actual override fun format(number: Double, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder {
        if (choiceFormats.isEmpty()) return toAppendTo
        var idx = 0
        for (i in choiceLimits.indices) {
            if (!(number >= choiceLimits[i])) {
                break
            }
            idx = i
        }
        toAppendTo.append(choiceFormats[idx])
        return toAppendTo
    }

    /**
     * [Nullability Decision] parse returns Number (Double.NaN when no choice matches, matching JDK 8 ChoiceFormat).
     */
    actual override fun parse(text: String, status: ParsePosition): Number? {
        val start = status.getIndex()
        var furthest = start
        var bestNumber = Double.NaN
        for (i in choiceFormats.indices) {
            val fmt = choiceFormats[i]
            if (text.regionMatches(start, fmt, 0, fmt.length)) {
                val endIdx = start + fmt.length
                if (endIdx > furthest) {
                    furthest = endIdx
                    bestNumber = choiceLimits[i]
                }
            }
        }
        if (furthest > start) {
            status.setIndex(furthest)
        } else {
            status.errorIndex = start
        }
        return bestNumber
    }

    actual companion object {
        private const val SIGN = 0x8000000000000000UL
        private const val POSITIVEINFINITY = 0x7ff0000000000000L

        actual fun nextDouble(d: Double): Double = nextDouble(d, true)

        actual fun previousDouble(d: Double): Double = nextDouble(d, false)

        actual fun nextDouble(d: Double, positive: Boolean): Double {
            if (d.isNaN()) return d
            if (d == 0.0) {
                val smallest = Double.fromBits(1L)
                return if (positive) smallest else -smallest
            }
            val bits = d.toRawBits()
            val magnitude = bits and 0x7fffffffffffffffL
            val isPositive = bits >= 0L
            if (isPositive == positive) {
                if (magnitude == POSITIVEINFINITY) return d
                return Double.fromBits(bits + 1L)
            } else {
                return Double.fromBits(bits - 1L)
            }
        }
    }
}

actual class Bidi {
    private val textChars: CharArray
    private val length: Int
    private val baseLevelValue: Int
    private val charLevels: ByteArray
    private val runStarts: IntArray
    private val runLimits: IntArray
    private val runLevels: IntArray

    actual constructor(paragraph: String, flags: Int) : this(
        paragraph.toCharArray(), 0, null, 0, paragraph.length, flags
    )

    actual constructor(
        text: CharArray,
        textStart: Int,
        embeddings: ByteArray?,
        embStart: Int,
        paragraphLength: Int,
        flags: Int
    ) {
        if (textStart < 0 || paragraphLength < 0 || textStart + paragraphLength > text.size) {
            throw IllegalArgumentException("Invalid text range")
        }
        if (embeddings != null && (embStart < 0 || embStart + paragraphLength > embeddings.size)) {
            throw IllegalArgumentException("Invalid embeddings range")
        }
        this.textChars = text.copyOfRange(textStart, textStart + paragraphLength)
        this.length = paragraphLength
        this.baseLevelValue = resolveBaseLevel(this.textChars, flags)
        this.charLevels = computeCharLevels(this.textChars, this.baseLevelValue)
        val runs = computeRuns(this.charLevels, this.baseLevelValue)
        this.runStarts = runs.first
        this.runLimits = runs.second
        this.runLevels = runs.third
    }

    private constructor(
        subChars: CharArray,
        baseLevel: Int,
        subLevels: ByteArray
    ) {
        this.textChars = subChars
        this.length = subChars.size
        this.baseLevelValue = baseLevel
        this.charLevels = subLevels
        val runs = computeRuns(subLevels, baseLevel)
        this.runStarts = runs.first
        this.runLimits = runs.second
        this.runLevels = runs.third
    }

    actual fun createLineBidi(lineStart: Int, lineLimit: Int): Bidi {
        if (lineStart < 0 || lineLimit < lineStart || lineLimit > length) {
            throw IllegalArgumentException("Invalid line range: $lineStart..$lineLimit")
        }
        return Bidi(
            textChars.copyOfRange(lineStart, lineLimit),
            baseLevelValue,
            charLevels.copyOfRange(lineStart, lineLimit)
        )
    }

    actual fun isMixed(): Boolean = !isLeftToRight() && !isRightToLeft()

    actual fun isLeftToRight(): Boolean =
        baseLevelValue % 2 == 0 && charLevels.all { it.toInt() % 2 == 0 }

    actual fun isRightToLeft(): Boolean =
        baseLevelValue % 2 == 1 && (length == 0 || charLevels.all { it.toInt() % 2 == 1 })

    actual fun getLength(): Int = length

    actual fun baseIsLeftToRight(): Boolean = baseLevelValue % 2 == 0

    actual fun getBaseLevel(): Int = baseLevelValue

    actual fun getLevelAt(offset: Int): Int {
        if (offset < 0 || offset >= length) return baseLevelValue
        return charLevels[offset].toInt()
    }

    actual fun getRunCount(): Int = runLevels.size

    actual fun getRunLevel(run: Int): Int =
        if (run in runLevels.indices) runLevels[run] else baseLevelValue

    actual fun getRunStart(run: Int): Int =
        if (run in runStarts.indices) runStarts[run] else 0

    actual fun getRunLimit(run: Int): Int =
        if (run in runLimits.indices) runLimits[run] else length

    private fun resolveBaseLevel(chars: CharArray, flags: Int): Int {
        return when (flags) {
            DIRECTION_LEFT_TO_RIGHT -> 0
            DIRECTION_RIGHT_TO_LEFT -> 1
            DIRECTION_DEFAULT_LEFT_TO_RIGHT, DIRECTION_DEFAULT_RIGHT_TO_LEFT -> {
                for (ch in chars) {
                    if (isRtlChar(ch)) return 1
                    if (ch.isLetter()) return 0
                }
                if (flags == DIRECTION_DEFAULT_RIGHT_TO_LEFT) 1 else 0
            }
            else -> if (flags and 1 != 0) 1 else 0
        }
    }

    private fun computeCharLevels(chars: CharArray, baseLevel: Int): ByteArray {
        val levels = ByteArray(chars.size) { baseLevel.toByte() }
        var currentStrongLevel = baseLevel
        for (i in chars.indices) {
            val ch = chars[i]
            when {
                isRtlChar(ch) -> {
                    currentStrongLevel = 1
                    levels[i] = 1
                }
                ch.isLetter() -> {
                    currentStrongLevel = 0
                    levels[i] = 0
                }
                ch.isDigit() -> {
                    levels[i] = if (currentStrongLevel == 1) 2 else 0
                }
                else -> {
                    levels[i] = currentStrongLevel.toByte()
                }
            }
        }
        return levels
    }

    private fun computeRuns(levels: ByteArray, baseLevel: Int): Triple<IntArray, IntArray, IntArray> {
        if (levels.isEmpty()) {
            return Triple(intArrayOf(0), intArrayOf(0), intArrayOf(baseLevel))
        }
        val starts = ArrayList<Int>()
        val limits = ArrayList<Int>()
        val runLvls = ArrayList<Int>()
        var runStart = 0
        var currLvl = levels[0].toInt()
        for (i in 1 until levels.size) {
            val l = levels[i].toInt()
            if (l != currLvl) {
                starts.add(runStart)
                limits.add(i)
                runLvls.add(currLvl)
                runStart = i
                currLvl = l
            }
        }
        starts.add(runStart)
        limits.add(levels.size)
        runLvls.add(currLvl)
        return Triple(starts.toIntArray(), limits.toIntArray(), runLvls.toIntArray())
    }

    actual companion object {
        actual val DIRECTION_LEFT_TO_RIGHT: Int = 0
        actual val DIRECTION_RIGHT_TO_LEFT: Int = 1
        actual val DIRECTION_DEFAULT_LEFT_TO_RIGHT: Int = -2
        actual val DIRECTION_DEFAULT_RIGHT_TO_LEFT: Int = -1

        private fun isRtlChar(ch: Char): Boolean {
            val code = ch.code
            return code in 0x0590..0x08FF ||
                code in 0xFB1D..0xFDFF ||
                code in 0xFE70..0xFEFF ||
                code == 0x200F || code == 0x202B || code == 0x202E
        }

        actual fun requiresBidi(text: CharArray, start: Int, limit: Int): Boolean {
            if (start < 0 || limit < start || limit > text.size) {
                throw IllegalArgumentException("Invalid range: $start..$limit")
            }
            for (i in start until limit) {
                if (isRtlChar(text[i])) return true
            }
            return false
        }

        actual fun reorderVisually(
            levels: ByteArray,
            levelStart: Int,
            objects: Array<Any?>,
            objectStart: Int,
            count: Int
        ) {
            if (levelStart < 0 || levelStart + count > levels.size ||
                objectStart < 0 || objectStart + count > objects.size || count < 0
            ) {
                throw IllegalArgumentException("Invalid reorderVisually range")
            }
            if (count == 0) return
            var maxLevel = 0
            var minOddLevel = 128
            for (i in 0 until count) {
                val lvl = levels[levelStart + i].toInt()
                if (lvl < 0) throw IllegalArgumentException("Negative level: $lvl")
                if (lvl > maxLevel) maxLevel = lvl
                if (lvl % 2 == 1 && lvl < minOddLevel) minOddLevel = lvl
            }
            if (minOddLevel > maxLevel) return

            for (lvl in maxLevel downTo minOddLevel) {
                var i = 0
                while (i < count) {
                    while (i < count && levels[levelStart + i].toInt() < lvl) {
                        i++
                    }
                    val startRun = i
                    while (i < count && levels[levelStart + i].toInt() >= lvl) {
                        i++
                    }
                    var lo = startRun
                    var hi = i - 1
                    while (lo < hi) {
                        val tmp = objects[objectStart + lo]
                        objects[objectStart + lo] = objects[objectStart + hi]
                        objects[objectStart + hi] = tmp
                        lo++
                        hi--
                    }
                }
            }
        }
    }
}
