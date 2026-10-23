package java.text

import java.lang.Cloneable

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
