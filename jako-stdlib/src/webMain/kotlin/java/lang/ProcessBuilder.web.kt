package java.lang

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.requireWebSupport
import com.bennyhuo.jako.toLatin1ByteArray
import com.bennyhuo.jako.toLatin1String
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

@WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
actual class ProcessBuilder {
    private var cmdList: MutableList<String>
    private val envMap: MutableMap<String, String> = LinkedHashMap()
    private var workDir: File? = null
    private var redirectErr: Boolean = false
    private var inheritIoFlag: Boolean = false

    init {
        val defaultKeys = listOf(
            "PATH", "HOME", "USER", "LOGNAME", "SHELL", "TMPDIR", "TEMP", "TMP",
            "LANG", "LC_ALL", "LC_CTYPE", "TERM", "PWD", "SYSTEMROOT", "COMSPEC"
        )
        for (k in defaultKeys) {
            val v = System.getenv(k)
            if (v != null) {
                envMap[k] = v
            }
        }
    }

    actual constructor(vararg command: String) {
        cmdList = ArrayList(command.size)
        for (c in command) cmdList.add(c)
    }

    actual constructor(command: MutableList<String>) {
        cmdList = command
    }

    actual fun command(command: MutableList<String>): ProcessBuilder {
        this.cmdList = command
        return this
    }

    actual fun command(vararg command: String): ProcessBuilder {
        val list = ArrayList<String>(command.size)
        for (c in command) list.add(c)
        this.cmdList = list
        return this
    }

    actual fun command(): MutableList<String> = cmdList

    actual fun environment(): MutableMap<String, String> = envMap

    actual fun directory(): File? = workDir

    actual fun directory(directory: File?): ProcessBuilder {
        this.workDir = directory
        return this
    }

    actual fun redirectErrorStream(): Boolean = redirectErr

    actual fun redirectErrorStream(redirectErrorStream: Boolean): ProcessBuilder {
        this.redirectErr = redirectErrorStream
        return this
    }

    actual fun inheritIO(): ProcessBuilder {
        this.inheritIoFlag = true
        return this
    }

    actual fun start(): Process {
        requireWebSupport(
            api = "java.lang.ProcessBuilder.start()",
            WebRuntime.NODE_JS,
            reason = "Spawning OS child processes requires Node.js child_process.spawnSync"
        )
        if (cmdList.isEmpty()) {
            throw IndexOutOfBoundsException("Empty command")
        }
        val prog = cmdList[0]
        if (prog.isEmpty()) {
            throw IOException("Cannot run program \"\": No such file or directory")
        }
        val dir = workDir
        if (dir != null && (!dir.exists() || !dir.isDirectory())) {
            throw IOException("Cannot run program \"$prog\" (in directory \"${dir.path}\"): No such file or directory")
        }
        val cwdPath = dir?.absolutePath ?: System.getProperty("user.dir") ?: "."
        val pathEnv = envMap["PATH"] ?: System.getenv("PATH") ?: ""
        if (!NodeBridge.canResolveExecutable(prog, cwdPath, pathEnv)) {
            throw IOException("Cannot run program \"$prog\": error=2, No such file or directory")
        }
        return WebProcessImpl(
            cmd = prog,
            args = if (cmdList.size > 1) cmdList.subList(1, cmdList.size).toList() else emptyList(),
            cwd = cwdPath,
            env = LinkedHashMap(envMap),
            redirectErrorStream = redirectErr
        )
    }
}

internal class WebProcessImpl(
    private val cmd: String,
    private val args: List<String>,
    private val cwd: String,
    private val env: Map<String, String>,
    private val redirectErrorStream: Boolean
) : Process() {
    private val stdinBuffer = ByteArrayOutputStream()
    private var stdinClosed = false
    private var executed = false
    private var destroyed = false
    private var exitCode = 0
    private var stdoutBytes: ByteArray = ByteArray(0)
    private var stderrBytes: ByteArray = ByteArray(0)

    private val stdinStream = object : OutputStream() {
        override fun write(b: Int) {
            if (stdinClosed) throw IOException("Stream closed")
            stdinBuffer.write(b)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            if (off < 0 || len < 0 || len > b.size - off) {
                throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
            }
            if (stdinClosed) throw IOException("Stream closed")
            if (len > 0) stdinBuffer.write(b, off, len)
        }

        override fun close() {
            stdinClosed = true
        }
    }

    private val stdoutStream = object : InputStream() {
        private var pos = 0
        private var closed = false

        override fun read(): Int {
            if (closed) throw IOException("Stream closed")
            ensureExecuted(0)
            if (pos >= stdoutBytes.size) return -1
            return stdoutBytes[pos++].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (off < 0 || len < 0 || len > b.size - off) {
                throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
            }
            if (closed) throw IOException("Stream closed")
            if (len == 0) return 0
            ensureExecuted(0)
            if (pos >= stdoutBytes.size) return -1
            val count = minOf(len, stdoutBytes.size - pos)
            stdoutBytes.copyInto(b, off, pos, pos + count)
            pos += count
            return count
        }

        override fun available(): Int {
            if (closed || !executed) return 0
            return (stdoutBytes.size - pos).coerceAtLeast(0)
        }

        override fun close() {
            closed = true
        }
    }

    private val stderrStream = object : InputStream() {
        private var pos = 0
        private var closed = false

        override fun read(): Int {
            if (closed) throw IOException("Stream closed")
            ensureExecuted(0)
            if (pos >= stderrBytes.size) return -1
            return stderrBytes[pos++].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (off < 0 || len < 0 || len > b.size - off) {
                throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
            }
            if (closed) throw IOException("Stream closed")
            if (len == 0) return 0
            ensureExecuted(0)
            if (pos >= stderrBytes.size) return -1
            val count = minOf(len, stderrBytes.size - pos)
            stderrBytes.copyInto(b, off, pos, pos + count)
            pos += count
            return count
        }

        override fun available(): Int {
            if (closed || !executed) return 0
            return (stderrBytes.size - pos).coerceAtLeast(0)
        }

        override fun close() {
            closed = true
        }
    }

    private fun ensureExecuted(timeoutMs: Int): Boolean {
        if (executed) return true
        if (destroyed) {
            executed = true
            return true
        }
        val argsJoined = args.joinToString("\u0000")
        val envJoined = env.entries.joinToString("\u0000") { "${it.key}=${it.value}" }
        val stdinLatin1 = stdinBuffer.toByteArray().toLatin1String()
        val raw = NodeBridge.spawnSync(
            cmd = cmd,
            argsJoined = argsJoined,
            cwd = cwd,
            envJoined = envJoined,
            stdinLatin1 = stdinLatin1,
            redirectErrorStream = redirectErrorStream,
            timeoutMs = timeoutMs
        )
        if (raw == "TIMEOUT") {
            return false
        }
        if (raw.startsWith("ERR|")) {
            throw IOException("Process execution failed: ${raw.substring(4)}")
        }
        if (raw.startsWith("OK|")) {
            val p1 = raw.indexOf('|', 3)
            val p2 = raw.indexOf('|', p1 + 1)
            val p3 = raw.indexOf('|', p2 + 1)
            exitCode = raw.substring(3, p1).toIntOrNull() ?: 0
            val outLen = raw.substring(p1 + 1, p2).toIntOrNull() ?: 0
            val errLen = raw.substring(p2 + 1, p3).toIntOrNull() ?: 0
            val payloadStart = p3 + 1
            val outStr = raw.substring(payloadStart, payloadStart + outLen)
            val errStr = raw.substring(payloadStart + outLen, payloadStart + outLen + errLen)
            stdoutBytes = outStr.toLatin1ByteArray()
            stderrBytes = errStr.toLatin1ByteArray()
            executed = true
            return true
        }
        executed = true
        return true
    }

    override fun getOutputStream(): OutputStream = stdinStream
    override fun getInputStream(): InputStream = stdoutStream
    override fun getErrorStream(): InputStream = stderrStream

    override fun waitFor(): Int {
        ensureExecuted(0)
        return exitCode
    }

    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
        if (executed || destroyed) return true
        val ms = unit.toMillis(timeout).coerceAtLeast(1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return ensureExecuted(ms)
    }

    override fun exitValue(): Int {
        if (!executed && !destroyed) {
            throw IllegalThreadStateException("process hasn't exited")
        }
        return exitCode
    }

    override fun destroy() {
        if (!executed) {
            destroyed = true
            executed = true
            exitCode = 143
        }
    }

    override fun destroyForcibly(): Process {
        destroy()
        return this
    }

    override fun isAlive(): Boolean = !executed && !destroyed
}
