package java.text

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
