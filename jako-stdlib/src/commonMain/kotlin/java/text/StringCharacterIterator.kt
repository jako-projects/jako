@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect class StringCharacterIterator : CharacterIterator {
    constructor(text: String)
    constructor(text: String, pos: Int)
    constructor(text: String, begin: Int, end: Int, pos: Int)

    fun setText(text: String)
    override fun first(): Char
    override fun last(): Char
    override fun setIndex(p: Int): Char
    override fun current(): Char
    override fun next(): Char
    override fun previous(): Char
    override fun getBeginIndex(): Int
    override fun getEndIndex(): Int
    override fun getIndex(): Int
    override fun clone(): Any
}
