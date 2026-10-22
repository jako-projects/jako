@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

expect open class Inflater {
    constructor()
    constructor(nowrap: Boolean)

    fun setInput(b: ByteArray)
    fun setInput(b: ByteArray, off: Int, len: Int)
    fun setDictionary(b: ByteArray)
    fun setDictionary(b: ByteArray, off: Int, len: Int)
    fun getRemaining(): Int
    val remaining: Int
    fun needsInput(): Boolean
    fun needsDictionary(): Boolean
    fun finished(): Boolean
    fun inflate(b: ByteArray): Int
    fun inflate(b: ByteArray, off: Int, len: Int): Int
    fun getAdler(): Int
    val adler: Int
    fun getTotalIn(): Int
    val totalIn: Int
    fun getTotalOut(): Int
    val totalOut: Int
    fun getBytesRead(): Long
    val bytesRead: Long
    fun getBytesWritten(): Long
    val bytesWritten: Long
    fun reset()
    fun end()
}
