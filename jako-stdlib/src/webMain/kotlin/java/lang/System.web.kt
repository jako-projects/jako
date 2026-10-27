package java.lang

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.toLatin1String
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

actual object System {
    private var stdIn: InputStream = ByteArrayInputStream(ByteArray(0))
    private var stdOut: PrintStream = PrintStream(NodeStdOutputStream(isErr = false), true)
    private var stdErr: PrintStream = PrintStream(NodeStdOutputStream(isErr = true), true)

    actual fun getenv(name: String): String? = NodeBridge.getenv(name)

    actual fun currentTimeMillis(): Long = NodeBridge.currentTimeMillis().toLong()

    actual fun nanoTime(): Long = NodeBridge.nanoTime().toLong()

    actual fun identityHashCode(x: Any?): Int = x?.hashCode() ?: 0

    actual fun getProperty(key: String): String? = when (key) {
        "line.separator" -> NodeBridge.lineSeparator()
        "file.separator" -> NodeBridge.fileSeparator()
        "path.separator" -> NodeBridge.pathSeparator()
        "os.name" -> NodeBridge.osName()
        "os.arch" -> NodeBridge.osArch()
        "os.version" -> NodeBridge.osVersion()
        "user.name" -> NodeBridge.userName()
        "user.home" -> NodeBridge.userHome()
        "user.dir" -> NodeBridge.userDir()
        "java.io.tmpdir" -> NodeBridge.tmpDir()
        "jako.process.pid" -> NodeBridge.pid().toString()
        "jako.process.uid" -> NodeBridge.uid().toString()
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
        stdOut = out ?: PrintStream(NodeStdOutputStream(isErr = false), true)
    }

    actual fun setErr(err: PrintStream?) {
        stdErr = err ?: PrintStream(NodeStdOutputStream(isErr = true), true)
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

private class NodeStdOutputStream(
    private val isErr: Boolean
) : OutputStream() {
    override fun write(b: Int) {
        val str = (b and 0xFF).toChar().toString()
        if (isErr) NodeBridge.writeStderr(str) else NodeBridge.writeStdout(str)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        val str = b.toLatin1String(off, len)
        if (isErr) NodeBridge.writeStderr(str) else NodeBridge.writeStdout(str)
    }

    override fun flush() {}

    override fun close() {}
}
