@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class BufferedReader : Reader {
    constructor(inReader: Reader)
    constructor(inReader: Reader, sz: Int)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    open fun readLine(): String?
    open fun lines(): java.util.stream.Stream<String>
    override fun close()
}
