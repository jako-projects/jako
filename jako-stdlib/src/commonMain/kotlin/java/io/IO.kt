@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface Closeable : AutoCloseable {
    override fun close()
}

expect interface Flushable {
    fun flush()
}

public expect interface Serializable

expect fun interface FileFilter {
    fun accept(pathname: File): Boolean
}

expect fun interface FilenameFilter {
    fun accept(dir: File, name: String): Boolean
}

expect open class FileReader : InputStreamReader {
    constructor(file: File)
    constructor(fileName: String)
}

expect open class FileWriter : OutputStreamWriter {
    constructor(file: File)
    constructor(fileName: String)
    constructor(fileName: String, append: Boolean)
    constructor(file: File, append: Boolean)
}

expect open class PrintStream : FilterOutputStream, Appendable, Closeable {
    constructor(out: OutputStream)
    constructor(out: OutputStream, autoFlush: Boolean)
    constructor(file: File)
    constructor(fileName: String)
    fun print(s: String?)
    fun println(s: String?)
    fun println()
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
    override fun append(value: CharSequence?): PrintStream
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): PrintStream
    override fun append(value: Char): PrintStream
}

expect open class PrintWriter : Writer {
    constructor(out: OutputStream)
    constructor(out: Writer)
    constructor(file: File)
    constructor(fileName: String)
    fun print(s: String?)
    fun println(s: String?)
    fun println()
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class IOException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class FileNotFoundException : IOException {
    constructor()
    constructor(message: String?)
}

expect open class EOFException : IOException {
    constructor()
    constructor(message: String?)
}

expect open class InterruptedIOException : IOException {
    constructor()
    constructor(message: String?)
}

expect open class SyncFailedException : IOException {
    constructor(message: String?)
}

expect open class File : Serializable, Comparable<File> {
    constructor(pathname: String)
    constructor(parent: String?, child: String)
    constructor(parent: File?, child: String)

    open fun getName(): String
    open fun getParent(): String?
    open fun getParentFile(): File?
    open fun getPath(): String
    open fun isAbsolute(): Boolean
    open fun getAbsolutePath(): String
    open fun getAbsoluteFile(): File
    open fun exists(): Boolean
    open fun isDirectory(): Boolean
    open fun isFile(): Boolean
    open fun length(): Long
    open fun delete(): Boolean
    open fun mkdir(): Boolean
    open fun mkdirs(): Boolean
    open fun list(): Array<String>?
    open fun listFiles(): Array<File>?
    /** [Nullability Decision] Java File.list(FilenameFilter) filter is nullable; null accepts all names. */
    open fun list(filter: FilenameFilter?): Array<String>?
    /** [Nullability Decision] Java File.listFiles(FileFilter) filter is nullable; null accepts all files. */
    open fun listFiles(filter: FileFilter?): Array<File>?
    /** [Nullability Decision] Java File.listFiles(FilenameFilter) filter is nullable; null accepts all files. */
    open fun listFiles(filter: FilenameFilter?): Array<File>?
    open fun lastModified(): Long
    open fun setLastModified(time: Long): Boolean
    /** [Nullability Decision] Java File.renameTo(File) dest is non-null. */
    open fun renameTo(dest: File): Boolean
    open fun canRead(): Boolean
    open fun canWrite(): Boolean
    open fun canExecute(): Boolean
    open fun getCanonicalPath(): String
    open fun getCanonicalFile(): File
    open fun createNewFile(): Boolean
    open fun isHidden(): Boolean
    override fun compareTo(other: File): Int

    open val name: String
    open val parent: String?
    open val parentFile: File?
    open val path: String
    open val isAbsolute: Boolean
    open val absolutePath: String
    open val absoluteFile: File
    open val canonicalPath: String
    open val canonicalFile: File
    open val isDirectory: Boolean
    open val isFile: Boolean
    open val isHidden: Boolean

    companion object {
        val separatorChar: Char
        val separator: String
        val pathSeparatorChar: Char
        val pathSeparator: String

        /** [Nullability Decision] prefix must be >= 3 chars; suffix is nullable (defaults to ".tmp"); directory is nullable (defaults to java.io.tmpdir). */
        fun createTempFile(prefix: String, suffix: String?, directory: File?): File
        fun createTempFile(prefix: String, suffix: String?): File
    }
}

expect abstract class InputStream : Closeable {
    constructor()
    abstract fun read(): Int
    open fun read(b: ByteArray): Int
    open fun read(b: ByteArray, off: Int, len: Int): Int
    open fun skip(n: Long): Long
    open fun available(): Int
    override fun close()
    open fun mark(readlimit: Int)
    open fun reset()
    open fun markSupported(): Boolean
}

expect abstract class OutputStream : Closeable, Flushable {
    constructor()
    abstract fun write(b: Int)
    open fun write(b: ByteArray)
    open fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class ByteArrayInputStream : InputStream {
    constructor(buf: ByteArray)
    constructor(buf: ByteArray, offset: Int, length: Int)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun skip(n: Long): Long
}

expect open class ByteArrayOutputStream : OutputStream {
    constructor()
    constructor(size: Int)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun toByteArray(): ByteArray
    open fun reset()
    open fun size(): Int
}

expect open class BufferedInputStream : FilterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, size: Int)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
}

expect open class BufferedOutputStream : FilterOutputStream {
    constructor(out: OutputStream)
    constructor(out: OutputStream, size: Int)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
}

expect open class FileInputStream : InputStream {
    constructor(file: File)
    constructor(name: String)
    constructor(fd: FileDescriptor)
    fun getFD(): FileDescriptor
    val fd: FileDescriptor
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()
}

expect open class FileOutputStream : OutputStream {
    constructor(file: File)
    constructor(name: String)
    constructor(name: String, append: Boolean)
    constructor(file: File, append: Boolean)
    constructor(fd: FileDescriptor)
    fun getFD(): FileDescriptor
    val fd: FileDescriptor
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect abstract class Reader : Closeable {
    protected constructor()
    open fun read(): Int
    open fun read(cbuf: CharArray): Int
    abstract fun read(cbuf: CharArray, off: Int, len: Int): Int
    open fun skip(n: Long): Long
    open fun ready(): Boolean
    open fun markSupported(): Boolean
    open fun mark(readAheadLimit: Int)
    open fun reset()
    abstract override fun close()
}

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

expect open class InputStreamReader : Reader {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, charsetName: String)
    constructor(inStream: InputStream, cs: java.nio.charset.Charset)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun close()
}

expect open class OutputStreamWriter : Writer {
    constructor(out: OutputStream)
    constructor(out: OutputStream, charsetName: String)
    constructor(out: OutputStream, cs: java.nio.charset.Charset)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class BufferedReader : Reader {
    constructor(inReader: Reader)
    constructor(inReader: Reader, sz: Int)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    open fun readLine(): String?
    override fun close()
}

expect open class BufferedWriter : Writer {
    constructor(out: Writer)
    constructor(out: Writer, sz: Int)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    open fun newLine()
    override fun flush()
    override fun close()
}

expect open class StringReader : Reader {
    constructor(s: String)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun close()
}

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

expect open class FilterInputStream : InputStream {
    protected constructor(inStream: InputStream)
    override fun read(): Int
    override fun read(b: ByteArray): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    override fun available(): Int
    override fun close()
    override fun mark(readlimit: Int)
    override fun reset()
    override fun markSupported(): Boolean
}

expect open class FilterOutputStream : OutputStream {
    constructor(out: OutputStream)
    override fun write(b: Int)
    override fun write(b: ByteArray)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class FilterReader : Reader {
    protected constructor(inReader: Reader)
    override fun read(): Int
    override fun read(cbuf: CharArray): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    override fun markSupported(): Boolean
    override fun mark(readAheadLimit: Int)
    override fun reset()
    override fun close()
}

expect open class FilterWriter : Writer {
    protected constructor(out: Writer)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class PushbackInputStream : FilterInputStream {
    constructor(inStream: InputStream, size: Int)
    constructor(inStream: InputStream)
    open fun unread(b: Int)
    open fun unread(b: ByteArray, off: Int, len: Int)
    open fun unread(b: ByteArray)
}

expect open class PushbackReader : FilterReader {
    constructor(inReader: Reader, size: Int)
    constructor(inReader: Reader)
    open fun unread(c: Int)
    open fun unread(cbuf: CharArray, off: Int, len: Int)
    open fun unread(cbuf: CharArray)
}

expect open class SequenceInputStream : InputStream {
    constructor(e: java.util.Enumeration<out InputStream>)
    constructor(s1: InputStream, s2: InputStream)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun close()
}

expect open class CharArrayReader : Reader {
    constructor(buf: CharArray)
    constructor(buf: CharArray, offset: Int, length: Int)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun close()
}

expect open class CharArrayWriter : Writer {
    constructor()
    constructor(initialSize: Int)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    open fun writeTo(out: Writer)
    open fun toCharArray(): CharArray
    open fun reset()
    open fun size(): Int
    override fun toString(): String
    override fun flush()
    override fun close()
}

expect open class PipedInputStream : InputStream {
    constructor(src: PipedOutputStream)
    constructor(src: PipedOutputStream, pipeSize: Int)
    constructor()
    constructor(pipeSize: Int)
    open fun connect(src: PipedOutputStream)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun close()
}

expect open class PipedOutputStream : OutputStream {
    constructor(snk: PipedInputStream)
    constructor()
    open fun connect(snk: PipedInputStream)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class PipedReader : Reader {
    constructor(src: PipedWriter)
    constructor(src: PipedWriter, pipeSize: Int)
    constructor()
    constructor(pipeSize: Int)
    open fun connect(src: PipedWriter)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun ready(): Boolean
    override fun close()
}

expect open class PipedWriter : Writer {
    constructor(snk: PipedReader)
    constructor()
    open fun connect(snk: PipedReader)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect open class LineNumberReader : BufferedReader {
    constructor(inReader: Reader)
    constructor(inReader: Reader, sz: Int)
    open fun setLineNumber(lineNumber: Int)
    open fun getLineNumber(): Int
    open val lineNumber: Int
}

expect open class RandomAccessFile : Closeable {
    constructor(file: File, mode: String)
    constructor(name: String, mode: String)
    open fun read(): Int
    open fun read(b: ByteArray, off: Int, len: Int): Int
    open fun read(b: ByteArray): Int
    open fun write(b: Int)
    open fun write(b: ByteArray)
    open fun write(b: ByteArray, off: Int, len: Int)
    open fun getFilePointer(): Long
    open fun seek(pos: Long)
    open fun length(): Long
    open fun setLength(newLength: Long)
    override fun close()
    open val filePointer: Long
}

expect open class ObjectInputStream : InputStream {
    constructor(inStream: InputStream)
    protected constructor()
    open fun readObject(): Any?
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()
}

expect open class ObjectOutputStream : OutputStream {
    constructor(out: OutputStream)
    protected constructor()
    open fun writeObject(obj: Any?)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect interface DataInput {
    fun readFully(b: ByteArray)
    fun readFully(b: ByteArray, off: Int, len: Int)
    fun skipBytes(n: Int): Int
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readUnsignedByte(): Int
    fun readShort(): Short
    fun readUnsignedShort(): Int
    fun readChar(): Char
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readUTF(): String
}

expect interface DataOutput {
    fun write(b: Int)
    fun write(b: ByteArray)
    fun write(b: ByteArray, off: Int, len: Int)
    fun writeBoolean(v: Boolean)
    fun writeByte(v: Int)
    fun writeShort(v: Int)
    fun writeChar(v: Int)
    fun writeInt(v: Int)
    fun writeLong(v: Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)
    fun writeBytes(s: String)
    fun writeChars(s: String)
    fun writeUTF(s: String)
}

expect open class DataInputStream(inStream: InputStream) : FilterInputStream, DataInput {
    override fun read(b: ByteArray): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun readFully(b: ByteArray)
    override fun readFully(b: ByteArray, off: Int, len: Int)
    override fun skipBytes(n: Int): Int
    override fun readBoolean(): Boolean
    override fun readByte(): Byte
    override fun readUnsignedByte(): Int
    override fun readShort(): Short
    override fun readUnsignedShort(): Int
    override fun readChar(): Char
    override fun readInt(): Int
    override fun readLong(): Long
    override fun readFloat(): Float
    override fun readDouble(): Double
    override fun readUTF(): String
}

expect open class DataOutputStream(out: OutputStream) : FilterOutputStream, DataOutput {
    fun size(): Int
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun writeBoolean(v: Boolean)
    override fun writeByte(v: Int)
    override fun writeShort(v: Int)
    override fun writeChar(v: Int)
    override fun writeInt(v: Int)
    override fun writeLong(v: Long)
    override fun writeFloat(v: Float)
    override fun writeDouble(v: Double)
    override fun writeBytes(s: String)
    override fun writeChars(s: String)
    override fun writeUTF(s: String)
}


