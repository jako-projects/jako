@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class StringWriter : Writer {
    constructor()
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String)
    override fun write(str: String, off: Int, len: Int)
    override fun append(value: CharSequence?): StringWriter
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): StringWriter
    override fun append(value: Char): StringWriter
    override fun toString(): String
    override fun flush()
    override fun close()
}
