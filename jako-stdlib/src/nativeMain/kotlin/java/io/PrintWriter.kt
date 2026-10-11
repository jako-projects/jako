package java.io

actual open class PrintWriter : Writer {
    private val outWriter: Writer
    private val autoFlush: Boolean

    actual constructor(out: Writer) : this(out, false)

    constructor(out: Writer, autoFlush: Boolean) : super() {
        this.outWriter = out
        this.autoFlush = autoFlush
    }

    actual constructor(out: OutputStream) : this(OutputStreamWriter(out), false)

    actual constructor(fileName: String) : this(FileWriter(fileName), false)

    actual constructor(file: File) : this(FileWriter(file), false)

    fun print(b: Boolean) = outWriter.write(b.toString())
    fun print(c: Char) = outWriter.write(c.toString())
    fun print(i: Int) = outWriter.write(i.toString())
    fun print(l: Long) = outWriter.write(l.toString())
    fun print(f: Float) = outWriter.write(f.toString())
    fun print(d: Double) = outWriter.write(d.toString())
    fun print(s: CharArray) = outWriter.write(s)
    actual open fun print(s: String?) = outWriter.write(s ?: "null")
    fun print(obj: Any?) = outWriter.write(obj?.toString() ?: "null")

    actual open fun println() {
        outWriter.write("\n")
        if (autoFlush) outWriter.flush()
    }

    fun println(b: Boolean) { print(b); println() }
    fun println(c: Char) { print(c); println() }
    fun println(i: Int) { print(i); println() }
    fun println(l: Long) { print(l); println() }
    fun println(f: Float) { print(f); println() }
    fun println(d: Double) { print(d); println() }
    fun println(s: CharArray) { print(s); println() }
    actual open fun println(s: String?) { print(s); println() }
    fun println(obj: Any?) { print(obj); println() }

    actual override fun write(c: Int) {
        outWriter.write(c)
        if (autoFlush && c == '\n'.code) outWriter.flush()
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        outWriter.write(cbuf, off, len)
        if (autoFlush) outWriter.flush()
    }

    actual override fun write(str: String, off: Int, len: Int) {
        outWriter.write(str, off, len)
        if (autoFlush) outWriter.flush()
    }

    actual override fun flush() = outWriter.flush()

    actual override fun close() = outWriter.close()
}
