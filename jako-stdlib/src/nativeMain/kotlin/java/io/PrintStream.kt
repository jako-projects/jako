package java.io

actual open class PrintStream : FilterOutputStream, Appendable, Closeable {
    private val autoFlush: Boolean

    actual constructor(out: OutputStream) : this(out, false)

    actual constructor(out: OutputStream, autoFlush: Boolean) : super(out) {
        this.autoFlush = autoFlush
    }

    actual constructor(fileName: String) : this(FileOutputStream(fileName), false)

    actual constructor(file: File) : this(FileOutputStream(file), false)

    private fun writeString(s: String) {
        val bytes = s.encodeToByteArray()
        out.write(bytes, 0, bytes.size)
        if (autoFlush) out.flush()
    }

    fun print(b: Boolean) = writeString(b.toString())
    fun print(c: Char) = writeString(c.toString())
    fun print(i: Int) = writeString(i.toString())
    fun print(l: Long) = writeString(l.toString())
    fun print(f: Float) = writeString(f.toString())
    fun print(d: Double) = writeString(d.toString())
    fun print(s: CharArray) = writeString(s.concatToString())
    actual open fun print(s: String?) = writeString(s ?: "null")
    fun print(obj: Any?) = writeString(obj?.toString() ?: "null")

    actual open fun println() = writeString("\n")
    fun println(b: Boolean) = writeString("$b\n")
    fun println(c: Char) = writeString("$c\n")
    fun println(i: Int) = writeString("$i\n")
    fun println(l: Long) = writeString("$l\n")
    fun println(f: Float) = writeString("$f\n")
    fun println(d: Double) = writeString("$d\n")
    fun println(s: CharArray) = writeString("${s.concatToString()}\n")
    actual open fun println(s: String?) = writeString("${s ?: "null"}\n")
    fun println(obj: Any?) = writeString("${obj?.toString() ?: "null"}\n")

    actual override fun write(b: Int) {
        out.write(b)
        if (autoFlush && b == '\n'.code) out.flush()
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        if (autoFlush) out.flush()
    }

    actual override fun flush() = out.flush()

    actual override fun close() = out.close()

    actual override fun append(value: CharSequence?): PrintStream {
        print(value?.toString() ?: "null")
        return this
    }

    actual override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): PrintStream {
        val cs = value ?: "null"
        print(cs.subSequence(startIndex, endIndex).toString())
        return this
    }

    actual override fun append(value: Char): PrintStream {
        print(value)
        return this
    }
}
