@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.lang.Cloneable

expect interface CharacterIterator : Cloneable {
    fun first(): Char
    fun last(): Char
    fun current(): Char
    fun next(): Char
    fun previous(): Char
    fun setIndex(position: Int): Char
    fun getBeginIndex(): Int
    fun getEndIndex(): Int
    fun getIndex(): Int
    fun clone(): Any

    companion object {
        val DONE: Char
    }
}
