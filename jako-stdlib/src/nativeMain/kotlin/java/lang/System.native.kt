@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.UnsafeNumber::class)

package java.lang

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import kotlin.system.getTimeMillis
import kotlin.system.getTimeNanos
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.EOF
import platform.posix.FILE
import platform.posix.fflush
import platform.posix.fgetc
import platform.posix.fputc
import platform.posix.fread
import platform.posix.fwrite
import platform.posix.getenv
import platform.posix.stderr
import platform.posix.stdin
import platform.posix.stdout
import kotlinx.cinterop.toKString

actual object System {
    private var stdIn: InputStream = StdInputStream(stdin)
    private var stdOut: PrintStream = PrintStream(StdOutputStream(stdout), true)
    private var stdErr: PrintStream = PrintStream(StdOutputStream(stderr), true)

    actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()

    actual fun currentTimeMillis(): Long = nativeCurrentTimeMillis()

    actual fun nanoTime(): Long = getTimeNanos()

    actual fun identityHashCode(x: Any?): Int = x?.hashCode() ?: 0

    actual fun getProperty(key: String): String? = when (key) {
        "line.separator" -> nativeLineSeparator()
        "file.separator" -> nativeFileSeparator()
        "path.separator" -> nativePathSeparator()
        "user.name" -> nativeUserName()
        "user.home" -> nativeUserHome()
        "user.dir" -> nativeUserDir()
        "java.io.tmpdir" -> nativeTmpDir()
        else -> null
    }

    actual fun getProperty(key: String, default: String): String = getProperty(key) ?: default

    actual fun lineSeparator(): String = getProperty("line.separator") ?: "\n"

    actual val `in`: InputStream get() = stdIn
    actual val out: PrintStream get() = stdOut
    actual val err: PrintStream get() = stdErr

    actual fun setIn(inStream: InputStream?) {
        stdIn = inStream ?: ByteArrayInputStream(ByteArray(0))
    }

    actual fun setOut(out: PrintStream?) {
        stdOut = out ?: PrintStream(StdOutputStream(stdout), true)
    }

    actual fun setErr(err: PrintStream?) {
        stdErr = err ?: PrintStream(StdOutputStream(stderr), true)
    }

    actual fun arraycopy(src: Any, srcPos: Int, dest: Any, destPos: Int, length: Int) {
        when {
            src is ByteArray && dest is ByteArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is ShortArray && dest is ShortArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is CharArray && dest is CharArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is IntArray && dest is IntArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is LongArray && dest is LongArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is FloatArray && dest is FloatArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is DoubleArray && dest is DoubleArray -> src.copyInto(dest, destPos, srcPos, srcPos + length)
            src is Array<*> && dest is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                (src as Array<Any?>).copyInto(dest as Array<Any?>, destPos, srcPos, srcPos + length)
            }
        }
    }
}

internal expect fun nativeLineSeparator(): String
internal expect fun nativeFileSeparator(): String
internal expect fun nativePathSeparator(): String
internal expect fun nativeCurrentTimeMillis(): Long
internal expect fun nativeTmpDir(): String
internal expect fun nativeUserName(): String
internal expect fun nativeUserHome(): String
internal expect fun nativeUserDir(): String

private class StdOutputStream(
    private val file: CPointer<FILE>?
) : OutputStream() {
    override fun write(b: Int) {
        val f = file ?: throw java.io.IOException("Stream closed")
        if (fputc(b, f) == EOF) throw java.io.IOException("Error writing to stdout/stderr")
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val f = file ?: throw java.io.IOException("Stream closed")
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        b.usePinned { pinned ->
            val written: Int = fwrite(pinned.addressOf(off), 1.convert(), len.convert(), f).convert()
            if (written < len) throw java.io.IOException("Error writing to stdout/stderr")
        }
    }

    override fun flush() {
        val f = file ?: return
        fflush(f)
    }

    override fun close() {
        flush()
    }
}

private class StdInputStream(
    private val file: CPointer<FILE>?
) : InputStream() {
    override fun read(): Int {
        val f = file ?: return -1
        val c = fgetc(f)
        return if (c == EOF) -1 else c
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val f = file ?: return -1
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        b.usePinned { pinned ->
            val n: Int = fread(pinned.addressOf(off), 1.convert(), len.convert(), f).convert()
            return if (n == 0) -1 else n
        }
    }
}
