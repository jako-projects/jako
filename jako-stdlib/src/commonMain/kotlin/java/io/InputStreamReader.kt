@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class InputStreamReader : Reader {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, charsetName: String)
    constructor(inStream: InputStream, cs: java.nio.charset.Charset)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun close()
}
