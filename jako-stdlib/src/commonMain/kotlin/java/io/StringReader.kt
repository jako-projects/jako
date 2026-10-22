@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class StringReader : Reader {
    constructor(s: String)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun close()
}
