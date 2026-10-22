@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect abstract class Writer : Closeable, Flushable, Appendable {
    protected constructor()
    open fun write(c: Int)
    open fun write(cbuf: CharArray)
    abstract fun write(cbuf: CharArray, off: Int, len: Int)
    open fun write(str: String)
    open fun write(str: String, off: Int, len: Int)
    abstract override fun flush()
    abstract override fun close()
    override fun append(value: CharSequence?): Writer
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Writer
    override fun append(value: Char): Writer
}
