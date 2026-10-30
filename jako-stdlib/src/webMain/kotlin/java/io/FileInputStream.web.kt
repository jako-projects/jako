package java.io

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.requireWebSupport

private const val FIS_UNSUPPORTED_REASON =
    "Browser sandbox does not allow synchronous local file system access"

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = FIS_UNSUPPORTED_REASON)
actual open class FileInputStream : InputStream {
    actual val fd: FileDescriptor
    private var closed = false

    actual constructor(name: String) {
        requireWebSupport("java.io.FileInputStream.<init>(String)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FIS_UNSUPPORTED_REASON)
        val handle = NodeBridge.open(name, "r")
        if (handle < 0) {
            throw FileNotFoundException("File not found: $name (${NodeBridge.lastError()})")
        }
        this.fd = FileDescriptor(handle)
    }

    actual constructor(file: File) : this(file.getPath())

    actual constructor(fd: FileDescriptor) {
        requireWebSupport("java.io.FileInputStream.<init>(FileDescriptor)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FIS_UNSUPPORTED_REASON)
        this.fd = fd
    }

    actual fun getFD(): FileDescriptor = fd

    actual override fun read(): Int {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        val res = NodeBridge.read(fd.fd, 1, -1.0)
        if (res.isEmpty() || res[0] == '0') return -1
        if (res[0] == 'E') throw IOException("Error reading file: ${res.substring(1)}")
        return if (res.length > 1) (res[1].code and 0xFF) else -1
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        val res = NodeBridge.read(fd.fd, len, -1.0)
        if (res.isEmpty() || res[0] == '0') return -1
        if (res[0] == 'E') throw IOException("Error reading file: ${res.substring(1)}")
        val count = res.length - 1
        for (i in 0 until count) {
            b[off + i] = (res[i + 1].code and 0xFF).toByte()
        }
        return count
    }

    override fun skip(n: Long): Long {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        if (n <= 0L) return 0L
        val cur = NodeBridge.lseek(fd.fd, 0.0, 1).toLong()
        if (cur < 0L) throw IOException("Seek failed")
        val newPos = NodeBridge.lseek(fd.fd, n.toDouble(), 1).toLong()
        if (newPos < 0L) throw IOException("Seek failed")
        return newPos - cur
    }

    override fun available(): Int {
        if (closed || !fd.valid()) return 0
        val cur = NodeBridge.lseek(fd.fd, 0.0, 1).toLong()
        val end = NodeBridge.fstatSize(fd.fd).toLong()
        if (cur < 0L || end < 0L) return 0
        val rem = end - cur
        return if (rem <= 0L) 0 else if (rem > Int.MAX_VALUE) Int.MAX_VALUE else rem.toInt()
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
