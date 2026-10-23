package java.util.regex

internal class ImmutableMatchResult(
    private val matchedText: String,
    private val matchStart: Int,
    private val matchEnd: Int,
    private val groupTexts: List<String?>,
    private val groupStarts: IntArray,
    private val groupEnds: IntArray
) : MatchResult {
    override fun start(): Int = matchStart
    override fun start(group: Int): Int {
        if (group < 0 || group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return groupStarts[group]
    }
    override fun end(): Int = matchEnd
    override fun end(group: Int): Int {
        if (group < 0 || group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return groupEnds[group]
    }
    override fun group(): String = matchedText
    override fun group(group: Int): String? {
        if (group < 0 || group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return groupTexts[group]
    }
    override fun groupCount(): Int = maxOf(0, groupTexts.size - 1)
}

actual class Matcher(private var currentPattern: Pattern, private var fullInput: CharSequence = "") : MatchResult {
    private var result: kotlin.text.MatchResult? = null
    private var matchStartOffset: Int = 0
    private var regStart: Int = 0
    private var regEnd: Int = fullInput.length
    private var lastFindIndex: Int = 0

    actual fun pattern(): Pattern = currentPattern

    actual fun toMatchResult(): MatchResult {
        val res = result ?: throw IllegalStateException("No match found")
        val count = groupCount()
        val texts = List(count + 1) { g -> group(g) }
        val starts = IntArray(count + 1) { g -> start(g) }
        val ends = IntArray(count + 1) { g -> end(g) }
        return ImmutableMatchResult(res.value, start(), end(), texts, starts, ends)
    }

    actual fun reset(): Matcher {
        this.regStart = 0
        this.regEnd = fullInput.length
        this.lastFindIndex = 0
        this.matchStartOffset = 0
        this.result = null
        return this
    }

    actual fun reset(input: CharSequence): Matcher {
        this.fullInput = input
        return reset()
    }

    actual fun region(start: Int, end: Int): Matcher {
        if (start < 0 || start > fullInput.length) throw IndexOutOfBoundsException("start=$start")
        if (end < 0 || end > fullInput.length) throw IndexOutOfBoundsException("end=$end")
        if (start > end) throw IndexOutOfBoundsException("start=$start > end=$end")
        regStart = start
        regEnd = end
        lastFindIndex = start
        matchStartOffset = 0
        result = null
        return this
    }

    actual fun regionStart(): Int = regStart

    actual fun regionEnd(): Int = regEnd

    actual fun usePattern(newPattern: Pattern): Matcher {
        this.currentPattern = newPattern
        this.result = null
        return this
    }

    actual fun matches(): Boolean {
        val regionInput = fullInput.subSequence(regStart, regEnd)
        val match = currentPattern.regex.matchEntire(regionInput)
        result = match
        matchStartOffset = regStart
        return match != null
    }

    actual fun lookingAt(): Boolean {
        val regionInput = fullInput.subSequence(regStart, regEnd)
        val match = currentPattern.regex.find(regionInput)
        return if (match != null && match.range.first == 0) {
            result = match
            matchStartOffset = regStart
            lastFindIndex = regStart + match.range.last + 1
            true
        } else {
            result = null
            false
        }
    }

    actual fun find(): Boolean {
        if (lastFindIndex > regEnd) {
            result = null
            return false
        }
        val regionInput = fullInput.subSequence(lastFindIndex, regEnd)
        val match = currentPattern.regex.find(regionInput)
        return if (match != null) {
            result = match
            matchStartOffset = lastFindIndex
            lastFindIndex = lastFindIndex + match.range.last + 1
            if (match.range.isEmpty()) {
                lastFindIndex += 1
            }
            true
        } else {
            result = null
            false
        }
    }

    actual fun find(start: Int): Boolean {
        if (start < 0 || start > fullInput.length) {
            throw IndexOutOfBoundsException("Illegal start index: $start")
        }
        reset()
        lastFindIndex = start
        return find()
    }

    actual override fun group(): String {
        val res = result ?: throw IllegalStateException("No match found")
        return res.value
    }

    actual override fun group(group: Int): String? {
        val res = result ?: throw IllegalStateException("No match found")
        if (group < 0 || group > groupCount()) {
            throw IndexOutOfBoundsException("No group $group")
        }
        return res.groups[group]?.value
    }

    actual fun group(name: String): String? {
        val res = result ?: throw IllegalStateException("No match found")
        val idx = currentPattern.namedGroups[name]
            ?: throw IllegalArgumentException("No group with name <$name>")
        return group(idx)
    }

    actual override fun groupCount(): Int = currentPattern.capturingGroupCount

    actual override fun start(): Int {
        val res = result ?: throw IllegalStateException("No match found")
        return matchStartOffset + res.range.first
    }

    actual override fun start(group: Int): Int {
        if (group == 0) return start()
        val res = result ?: throw IllegalStateException("No match found")
        if (group < 0 || group > groupCount()) {
            throw IndexOutOfBoundsException("No group $group")
        }
        val grp = res.groups[group] ?: return -1
        val groupStr = grp.value
        val mainStr = res.value
        val idx = mainStr.indexOf(groupStr)
        return if (idx >= 0) matchStartOffset + res.range.first + idx else -1
    }

    actual override fun end(): Int {
        val res = result ?: throw IllegalStateException("No match found")
        return matchStartOffset + res.range.last + 1
    }

    actual override fun end(group: Int): Int {
        if (group == 0) return end()
        val s = start(group)
        if (s < 0) return -1
        val grp = result?.groups?.get(group) ?: return -1
        return s + grp.value.length
    }

    actual fun replaceAll(replacement: String): String {
        reset()
        val regionInput = fullInput.subSequence(regStart, regEnd).toString()
        return currentPattern.regex.replace(regionInput, replacement)
    }

    actual fun replaceFirst(replacement: String): String {
        reset()
        val regionInput = fullInput.subSequence(regStart, regEnd).toString()
        return currentPattern.regex.replaceFirst(regionInput, replacement)
    }

    actual companion object {
        actual fun quoteReplacement(s: String): String = Regex.escapeReplacement(s)
    }
}
