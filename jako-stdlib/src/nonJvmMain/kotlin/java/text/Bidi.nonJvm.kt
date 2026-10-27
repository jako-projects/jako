package java.text

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
