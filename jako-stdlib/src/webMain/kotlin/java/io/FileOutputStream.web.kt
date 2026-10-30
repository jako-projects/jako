package java.io

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.requireWebSupport
import com.bennyhuo.jako.toLatin1String

private const val FOS_UNSUPPORTED_REASON =
    "Browser sandbox does not allow synchronous local file system access"

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = FOS_UNSUPPORTED_REASON)
actual open class FileOutputStream : OutputStream {
    actual val fd: FileDescriptor
    private var closed = false

    actual constructor(name: String) : this(name, false)

    actual constructor(name: String, append: Boolean) {
        requireWebSupport("java.io.FileOutputStream.<init>(String, Boolean)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FOS_UNSUPPORTED_REASON)
        val mode = if (append) "a" else "w"
        val handle = NodeBridge.open(name, mode)
        if (handle < 0) {
            throw FileNotFoundException("Cannot open file for writing: $name (${NodeBridge.lastError()})")
        }
        this.fd = FileDescriptor(handle)
    }

    actual constructor(file: File) : this(file.getPath(), false)

    actual constructor(file: File, append: Boolean) : this(file.getPath(), append)

    actual constructor(fd: FileDescriptor) {
        requireWebSupport("java.io.FileOutputStream.<init>(FileDescriptor)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FOS_UNSUPPORTED_REASON)
        this.fd = fd
    }

    actual fun getFD(): FileDescriptor = fd

    actual override fun write(b: Int) {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        val ch = (b and 0xFF).toChar().toString()
        val written = NodeBridge.write(fd.fd, ch, -1.0)
        if (written < 1) {
            throw IOException("Error writing to file: ${NodeBridge.lastError()}")
        }
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        val latin1 = b.toLatin1String(off, len)
        val written = NodeBridge.write(fd.fd, latin1, -1.0)
        if (written < len) {
            throw IOException("Error writing to file: ${NodeBridge.lastError()}")
        }
    }

    actual override fun flush() {
        // Node fs.writeSync writes directly to OS file descriptor
    }

    actual override fun close() {
        if (!closed) {
            closed = true
            if (fd.valid()) {
                val handle = fd.fd
                fd.fd = -1
                NodeBridge.close(handle)
            }
        }
    }
}
