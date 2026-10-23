@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect class Bidi {
    constructor(paragraph: String, flags: Int)
    constructor(
        text: CharArray,
        textStart: Int,
        embeddings: ByteArray?,
        embStart: Int,
        paragraphLength: Int,
        flags: Int
    )

    fun createLineBidi(lineStart: Int, lineLimit: Int): Bidi
    fun isMixed(): Boolean
    fun isLeftToRight(): Boolean
    fun isRightToLeft(): Boolean
    fun getLength(): Int
    fun baseIsLeftToRight(): Boolean
    fun getBaseLevel(): Int
    fun getLevelAt(offset: Int): Int
    fun getRunCount(): Int
    fun getRunLevel(run: Int): Int
    fun getRunStart(run: Int): Int
    fun getRunLimit(run: Int): Int

    companion object {
        val DIRECTION_LEFT_TO_RIGHT: Int
        val DIRECTION_RIGHT_TO_LEFT: Int
        val DIRECTION_DEFAULT_LEFT_TO_RIGHT: Int
        val DIRECTION_DEFAULT_RIGHT_TO_LEFT: Int

        fun requiresBidi(text: CharArray, start: Int, limit: Int): Boolean
        fun reorderVisually(
            levels: ByteArray,
            levelStart: Int,
            objects: Array<Any?>,
            objectStart: Int,
            count: Int
        )
    }
}
