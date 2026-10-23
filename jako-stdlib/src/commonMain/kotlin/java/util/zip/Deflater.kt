@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

expect open class Deflater {
    constructor()
    constructor(level: Int)
    constructor(level: Int, nowrap: Boolean)

    fun setInput(b: ByteArray)
    fun setInput(b: ByteArray, off: Int, len: Int)
    fun setDictionary(b: ByteArray)
    fun setDictionary(b: ByteArray, off: Int, len: Int)
    fun setStrategy(strategy: Int)
    fun setLevel(level: Int)
    fun needsInput(): Boolean
    fun finish()
    fun finished(): Boolean
    fun deflate(b: ByteArray): Int
    fun deflate(b: ByteArray, off: Int, len: Int): Int
    fun deflate(b: ByteArray, off: Int, len: Int, flush: Int): Int
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

    companion object {
        val DEFLATED: Int
        val NO_COMPRESSION: Int
        val BEST_SPEED: Int
        val BEST_COMPRESSION: Int
        val DEFAULT_COMPRESSION: Int
        val FILTERED: Int
        val HUFFMAN_ONLY: Int
        val DEFAULT_STRATEGY: Int
        val NO_FLUSH: Int
        val SYNC_FLUSH: Int
        val FULL_FLUSH: Int
    }
}
