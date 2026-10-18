package java.util.regex

import java.io.Serializable

actual class Matcher(var pattern: Pattern, private var fullInput: CharSequence = "") {
    private var result: MatchResult? = null
    private var matchStartOffset: Int = 0
    private var regionStart: Int = 0
    private var regionEnd: Int = fullInput.length
    private var lastFindIndex: Int = 0

    actual var input: CharSequence
        get() = fullInput.subSequence(regionStart, regionEnd)
        set(value) {
            fullInput = value
            regionStart = 0
            regionEnd = value.length
            lastFindIndex = 0
            matchStartOffset = 0
            result = null
        }

    actual fun reset(input: CharSequence): Matcher {
        this.fullInput = input
        this.regionStart = 0
        this.regionEnd = input.length
        this.lastFindIndex = 0
        this.matchStartOffset = 0
        this.result = null
        return this
    }

    actual fun region(start: Int, end: Int): Matcher {
        require(start >= 0 && start <= end && end <= fullInput.length) {
            "Invalid region boundaries: start=$start, end=$end, length=${fullInput.length}"
        }
        regionStart = start
        regionEnd = end
        lastFindIndex = start
        matchStartOffset = 0
        result = null
        return this
    }

    actual fun usePattern(newPattern: Pattern): Matcher {
        this.pattern = newPattern
        this.result = null
        return this
    }

    actual fun matches(): Boolean {
        val regionInput = fullInput.subSequence(regionStart, regionEnd)
        val match = pattern.regex.matchEntire(regionInput)
        result = match
        matchStartOffset = regionStart
        return match != null
    }

    actual fun lookingAt(): Boolean {
        val regionInput = fullInput.subSequence(regionStart, regionEnd)
        val match = pattern.regex.find(regionInput)
        return if (match != null && match.range.start == 0) {
            result = match
            matchStartOffset = regionStart
            lastFindIndex = regionStart + match.range.endInclusive + 1
            true
        } else {
            result = null
            false
        }
    }

    actual fun find(): Boolean {
        if (lastFindIndex > regionEnd) {
            result = null
            return false
        }
        val regionInput = fullInput.subSequence(lastFindIndex, regionEnd)
        val match = pattern.regex.find(regionInput)
        return if (match != null) {
            result = match
            matchStartOffset = lastFindIndex
            lastFindIndex = lastFindIndex + match.range.endInclusive + 1
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
        val actualStart = regionStart + start
        require(actualStart >= regionStart && actualStart <= regionEnd) {
            "Invalid find start: $start"
        }
        lastFindIndex = actualStart
        return find()
    }

    actual fun group(): String = result?.value ?: ""

    actual fun group(index: Int): String {
        val res = result ?: throw IllegalStateException("No match found")
        if (index < 0 || index >= res.groupValues.size) {
            throw IndexOutOfBoundsException("No group $index")
        }
        return res.groupValues[index]
    }

    actual fun groupCount(): Int = (result?.groupValues?.size ?: 1) - 1

    actual fun start(): Int {
        val res = result ?: throw IllegalStateException("No match found")
        return matchStartOffset + res.range.start
    }

    actual fun start(group: Int): Int {
        if (group == 0) return start()
        val res = result ?: throw IllegalStateException("No match found")
        if (group < 0 || group >= res.groupValues.size) {
            throw IndexOutOfBoundsException("No group $group")
        }
        val groupStr = res.groupValues[group]
        val mainStr = res.value
        val idx = mainStr.indexOf(groupStr)
        return if (idx >= 0) matchStartOffset + res.range.start + idx else -1
    }

    actual fun end(): Int {
        val res = result ?: throw IllegalStateException("No match found")
        return matchStartOffset + res.range.endInclusive + 1
    }

    actual fun replaceAll(replacement: String): String {
        val regionInput = fullInput.subSequence(regionStart, regionEnd).toString()
        return pattern.regex.replace(regionInput, replacement)
    }

    actual fun replaceFirst(replacement: String): String {
        val regionInput = fullInput.subSequence(regionStart, regionEnd).toString()
        return pattern.regex.replaceFirst(regionInput, replacement)
    }
}

actual class Pattern constructor(val patternStr: String, val regex: Regex, val flags: Int = 0) : Serializable {
    actual fun pattern(): String = patternStr
    actual fun flags(): Int = flags
    actual fun matcher(input: CharSequence): Matcher = Matcher(this, input)

    actual fun split(input: CharSequence): Array<String> = split(input, 0)

    actual fun split(input: CharSequence, limit: Int): Array<String> {
        val list = if (limit > 0) {
            regex.split(input, limit)
        } else {
            regex.split(input, 0)
        }
        if (limit == 0 && list.size > 1) {
            var end = list.size
            while (end > 0 && list[end - 1].isEmpty()) {
                end--
            }
            return list.subList(0, end).toTypedArray()
        }
        return list.toTypedArray()
    }

    actual companion object {
        actual const val UNIX_LINES = 1
        actual const val CASE_INSENSITIVE = 2
        actual const val COMMENTS = 4
        actual const val MULTILINE = 8
        actual const val LITERAL = 16
        actual const val DOTALL = 32
        actual const val UNICODE_CASE = 64
        actual const val CANON_EQ = 128
        actual const val UNICODE_CHARACTER_CLASS = 256

        actual fun compile(pattern: String): Pattern = compile(pattern, 0)

        actual fun compile(pattern: String, flags: Int): Pattern {
            val options = mutableSetOf<RegexOption>()
            if (flags and CASE_INSENSITIVE != 0) options.add(RegexOption.IGNORE_CASE)
            if (flags and MULTILINE != 0) options.add(RegexOption.MULTILINE)
            if (flags and DOTALL != 0) options.add(RegexOption.DOT_MATCHES_ALL)
            if (flags and COMMENTS != 0) options.add(RegexOption.COMMENTS)
            if (flags and LITERAL != 0) options.add(RegexOption.LITERAL)

            return Pattern(pattern, Regex(pattern, options), flags)
        }

        actual fun matches(regex: String, input: CharSequence): Boolean {
            return compile(regex).matcher(input).matches()
        }

        actual fun quote(s: String): String = Regex.escape(s)
    }
}
