@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package java.lang

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal expect fun nativeCreatePipe(): IntArray?

internal expect fun nativeWriteFd(fd: Int, buf: ByteArray, off: Int, len: Int): Int

internal expect fun nativeCloseFd(fd: Int)

internal expect fun nativeKillProcess(pid: Int, forcibly: Boolean)

internal expect fun nativeDecodeWaitStatus(rawStatus: Int): Int

internal expect fun nativeIsWindows(): Boolean

internal expect fun nativeCanExecute(path: String): Boolean

internal expect fun nativeRunSystemCommand(cmd: String): Int

internal val processCounter = AtomicLong(0L)

actual class ProcessBuilder {
    private var cmdList: MutableList<String>
    private val envMap: MutableMap<String, String> = LinkedHashMap()
    private val initialEnvKeys: Set<String>
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
        initialEnvKeys = envMap.keys.toSet()
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

        if (!nativeIsWindows()) {
            val resolvedExecutable = resolvePosixExecutable(prog, dir, envMap["PATH"] ?: System.getenv("PATH"))
                ?: throw IOException("Cannot run program \"$prog\": error=2, No such file or directory")
            return startPosixProcess(resolvedExecutable, cmdList, envMap, initialEnvKeys, dir, redirectErr)
        } else {
            return startWindowsProcess(cmdList, envMap, dir, redirectErr)
        }
    }

    private fun resolvePosixExecutable(prog: String, dir: File?, pathEnv: String?): String? {
        if (prog.contains('/')) {
            val f = if (prog.startsWith("/")) File(prog) else if (dir != null) File(dir, prog) else File(prog)
            return if (f.exists() && !f.isDirectory() && nativeCanExecute(f.absolutePath)) f.absolutePath else null
        }
        val searchPath = pathEnv ?: "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin"
        val parts = searchPath.split(':')
        for (part in parts) {
            if (part.isEmpty()) continue
            val candidate = File(part, prog)
            if (candidate.exists() && !candidate.isDirectory() && nativeCanExecute(candidate.absolutePath)) {
                return candidate.absolutePath
            }
        }
        return null
    }
}

internal fun posixShellQuote(s: String): String {
    return "'" + s.replace("'", "'\\''") + "'"
}

internal fun startPosixProcess(
    resolvedExec: String,
    cmdList: List<String>,
    envMap: Map<String, String>,
    initialEnvKeys: Set<String>,
    workDir: File?,
    redirectErr: Boolean
): Process {
    val id = "${System.currentTimeMillis()}_${processCounter.incrementAndGet()}"
    val tmpDir = File(nativeTmpDir())
    val stdoutFile = File(tmpDir, "jako_proc_out_$id.tmp")
    val stderrFile = if (redirectErr) null else File(tmpDir, "jako_proc_err_$id.tmp")
    val pidFile = File(tmpDir, "jako_proc_pid_$id.tmp")

    stdoutFile.createNewFile()
    stderrFile?.createNewFile()
    pidFile.createNewFile()

    val pipeFds = nativeCreatePipe()
    val readFd = pipeFds?.get(0) ?: -1
    val writeFd = pipeFds?.get(1) ?: -1

    val sb = StringBuilder()
    for (removedKey in initialEnvKeys) {
        if (!envMap.containsKey(removedKey) && isValidEnvName(removedKey)) {
            sb.append("unset ").append(removedKey).append("; ")
        }
    }
    for ((k, v) in envMap) {
        if (isValidEnvName(k) && v != System.getenv(k)) {
            sb.append("export ").append(k).append("=").append(posixShellQuote(v)).append("; ")
        }
    }
    if (workDir != null) {
        sb.append("cd ").append(posixShellQuote(workDir.absolutePath)).append(" || exit 127; ")
    }
    if (readFd >= 0) {
        sb.append("exec 0<&").append(readFd).append("; ")
    }
    sb.append("echo \$\$ > ").append(posixShellQuote(pidFile.absolutePath)).append("; ")
    sb.append("exec ").append(posixShellQuote(resolvedExec))
    for (i in 1 until cmdList.size) {
        sb.append(" ").append(posixShellQuote(cmdList[i]))
    }
    sb.append(" > ").append(posixShellQuote(stdoutFile.absolutePath))
    if (redirectErr) {
        sb.append(" 2>&1")
    } else if (stderrFile != null) {
        sb.append(" 2> ").append(posixShellQuote(stderrFile.absolutePath))
    }

    return NativeProcessImpl(sb.toString(), stdoutFile, stderrFile, pidFile, readFd, writeFd)
}

internal fun startWindowsProcess(
    cmdList: List<String>,
    envMap: Map<String, String>,
    workDir: File?,
    redirectErr: Boolean
): Process {
    val id = "${System.currentTimeMillis()}_${processCounter.incrementAndGet()}"
    val tmpDir = File(nativeTmpDir())
    val stdoutFile = File(tmpDir, "jako_proc_out_$id.tmp")
    val stderrFile = if (redirectErr) null else File(tmpDir, "jako_proc_err_$id.tmp")
    val pidFile = File(tmpDir, "jako_proc_pid_$id.tmp")

    stdoutFile.createNewFile()
    stderrFile?.createNewFile()

    val sb = StringBuilder("cmd.exe /C \"")
    if (workDir != null) {
        sb.append("cd /d \"").append(workDir.absolutePath).append("\" && ")
    }
    for ((k, v) in envMap) {
        if (isValidEnvName(k) && v != System.getenv(k)) {
            sb.append("set \"").append(k).append("=").append(v).append("\" && ")
        }
    }
    for ((idx, arg) in cmdList.withIndex()) {
        if (idx > 0) sb.append(" ")
        if (arg.contains(' ') || arg.contains('\t')) {
            sb.append("\"").append(arg.replace("\"", "\\\"")).append("\"")
        } else {
            sb.append(arg)
        }
    }
    sb.append(" > \"").append(stdoutFile.absolutePath).append("\"")
    if (redirectErr) {
        sb.append(" 2>&1")
    } else if (stderrFile != null) {
        sb.append(" 2> \"").append(stderrFile.absolutePath).append("\"")
    }
    sb.append("\"")
    return NativeProcessImpl(sb.toString(), stdoutFile, stderrFile, pidFile, -1, -1)
}

internal fun isValidEnvName(name: String): Boolean {
    if (name.isEmpty()) return false
    for (i in name.indices) {
        val c = name[i]
        val ok = (c in 'a'..'z') || (c in 'A'..'Z') || c == '_' || (i > 0 && c in '0'..'9')
        if (!ok) return false
    }
    return true
}

internal class NativeProcessImpl(
    private val shellCommand: String,
    private val stdoutFile: File,
    private val stderrFile: File?,
    private val pidFile: File,
    private var childReadFd: Int,
    writeFd: Int
) : Process() {
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()
    @kotlin.concurrent.Volatile
    private var done = false
    @kotlin.concurrent.Volatile
    private var exitCode = 0
    @kotlin.concurrent.Volatile
    private var childPid = -1

    private val stdinStream = PipeOutputStream(writeFd)
    private val stdoutStream = TailFileInputStream(stdoutFile, this)
    private val stderrStream: InputStream = if (stderrFile != null) {
        TailFileInputStream(stderrFile, this)
    } else {
        ByteArrayInputStream(ByteArray(0))
    }

    init {
        val worker = Thread(Runnable {
            val raw = nativeRunSystemCommand(shellCommand)
            val code = nativeDecodeWaitStatus(raw)
            lock.withLock {
                if (childReadFd >= 0) {
                    nativeCloseFd(childReadFd)
                    childReadFd = -1
                }
                exitCode = code
                done = true
                cond.signalAll()
            }
            pidFile.delete()
        }, "Process-Worker")
        worker.isDaemon = true
        worker.start()

        // Wait briefly for child shell to record its PID and duplicate readFd
        if (!nativeIsWindows()) {
            var attempts = 0
            while (!done && childPid <= 0 && attempts < 2000) {
                val p = readPidFile()
                if (p > 0) {
                    childPid = p
                    lock.withLock {
                        if (childReadFd >= 0) {
                            nativeCloseFd(childReadFd)
                            childReadFd = -1
                        }
                    }
                    break
                }
                Thread.sleep(1)
                attempts++
            }
        }
    }

    private fun readPidFile(): Int {
        val len = pidFile.length().toInt()
        if (!pidFile.exists() || len <= 0) return -1
        return try {
            val raf = RandomAccessFile(pidFile, "r")
            try {
                val buf = ByteArray(len)
                val n = raf.read(buf, 0, len)
                if (n <= 0) return -1
                buf.decodeToString(0, n).trim().toIntOrNull() ?: -1
            } finally {
                raf.close()
            }
        } catch (_: Throwable) {
            -1
        }
    }

    internal fun isFinished(): Boolean = done

    internal fun awaitBrief(millis: Long) {
        lock.withLock {
            if (!done) {
                cond.await(millis, TimeUnit.MILLISECONDS)
            }
        }
    }

    override fun getOutputStream(): OutputStream = stdinStream
    override fun getInputStream(): InputStream = stdoutStream
    override fun getErrorStream(): InputStream = stderrStream

    override fun waitFor(): Int {
        lock.withLock {
            while (!done) {
                cond.await()
            }
            return exitCode
        }
    }

    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
        var nanos = unit.toNanos(timeout)
        lock.withLock {
            while (!done) {
                if (nanos <= 0L) return false
                nanos = cond.awaitNanos(nanos)
            }
            return true
        }
    }

    override fun exitValue(): Int {
        if (!done) {
            throw IllegalThreadStateException("process hasn't exited")
        }
        return exitCode
    }

    override fun destroy() {
        destroyInternal(forcibly = false)
    }

    override fun destroyForcibly(): Process {
        destroyInternal(forcibly = true)
        return this
    }

    private fun destroyInternal(forcibly: Boolean) {
        try {
            stdinStream.close()
        } catch (_: Throwable) {
        }
        if (!done) {
            var pid = childPid
            var attempts = 0
            while (pid <= 0 && !done && !nativeIsWindows() && attempts < 500) {
                pid = readPidFile()
                if (pid > 0) {
                    childPid = pid
                    break
                }
                Thread.sleep(1)
                attempts++
            }
            if (pid > 0) {
                nativeKillProcess(pid, forcibly)
            }
        }
    }

    override fun isAlive(): Boolean = !done
}

internal class PipeOutputStream(
    @kotlin.concurrent.Volatile private var fd: Int
) : OutputStream() {
    private val lock = ReentrantLock()

    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()), 0, 1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
        }
        if (len == 0) return
        lock.withLock {
            val currentFd = fd
            if (currentFd < 0) throw IOException("Stream closed")
            var writtenTotal = 0
            while (writtenTotal < len) {
                val n = nativeWriteFd(currentFd, b, off + writtenTotal, len - writtenTotal)
                if (n <= 0) throw IOException("Broken pipe")
                writtenTotal += n
            }
        }
    }

    override fun close() {
        lock.withLock {
            val currentFd = fd
            if (currentFd >= 0) {
                fd = -1
                nativeCloseFd(currentFd)
            }
        }
    }
}

internal class TailFileInputStream(
    private val file: File,
    private val proc: NativeProcessImpl
) : InputStream() {
    private var raf: RandomAccessFile? = null
    private var pos: Long = 0L
    private var closed = false

    private fun ensureOpen(): RandomAccessFile {
        if (closed) throw IOException("Stream closed")
        var r = raf
        if (r == null) {
            r = RandomAccessFile(file, "r")
            raf = r
        }
        return r
    }

    override fun read(): Int {
        val buf = ByteArray(1)
        val n = read(buf, 0, 1)
        return if (n <= 0) -1 else (buf[0].toInt() and 0xFF)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
        }
        if (len == 0) return 0
        val r = ensureOpen()
        while (true) {
            r.seek(pos)
            val n = r.read(b, off, len)
            if (n > 0) {
                pos += n
                return n
            }
            if (proc.isFinished()) {
                r.seek(pos)
                val finalRead = r.read(b, off, len)
                if (finalRead > 0) {
                    pos += finalRead
                    return finalRead
                }
                cleanupFile()
                return -1
            }
            proc.awaitBrief(5L)
        }
    }

    override fun available(): Int {
        if (closed) return 0
        val len = file.length()
        val rem = len - pos
        return if (rem > 0L) rem.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() else 0
    }

    override fun close() {
        if (!closed) {
            closed = true
            try {
                raf?.close()
            } catch (_: Throwable) {
            }
            raf = null
            if (proc.isFinished()) {
                cleanupFile()
            }
        }
    }

    private fun cleanupFile() {
        try {
            raf?.close()
        } catch (_: Throwable) {
        }
        raf = null
        file.delete()
    }
}
