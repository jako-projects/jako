package com.bennyhuo.jako

/**
 * Internal bridge to Node.js runtime APIs (`fs`, `os`, `path`, `process`, `crypto`, `child_process`)
 * shared across `jsMain` and `wasmJsMain`.
 *
 * Binary payloads are transferred as Latin-1 strings (1 char code unit `0x00..0xFF` per byte)
 * so both Kotlin/JS and Kotlin/WasmJs `@JsFun` interop work with zero external dependencies.
 */
internal expect object NodeBridge {
    fun currentTimeMillis(): Double
    fun nanoTime(): Double
    fun sleepMillis(ms: Int)

    fun pid(): Int
    fun uid(): Int
    fun availableProcessors(): Int
    fun osName(): String
    fun osArch(): String
    fun osVersion(): String
    fun fileSeparator(): String
    fun pathSeparator(): String
    fun lineSeparator(): String
    fun userDir(): String
    fun userHome(): String
    fun userName(): String
    fun tmpDir(): String
    fun hostname(): String

    fun getenv(name: String): String?
    fun getenvAll(): String
    fun writeStdout(latin1: String)
    fun writeStderr(latin1: String)
    fun exit(status: Int)

    fun randomBytesLatin1(length: Int): String

    /**
     * Returns `""` if not found, or `"ERR|<code>|<message>"` if an unexpected error occurs,
     * or `"<type>|<size>|<mtimeMs>|<atimeMs>|<ctimeMs>|<mode>|<ino>"` where `<type>` is:
     * `1` = regular file, `2` = directory, `3` = symlink, `4` = other.
     */
    fun stat(path: String, followSymlinks: Boolean): String
    fun access(path: String, mode: Int): Boolean
    fun chmod(path: String, mode: Int): Boolean
    fun utimes(path: String, atimeMs: Double, mtimeMs: Double): Boolean
    fun mkdir(path: String): Boolean
    fun mkdirOrError(path: String): String
    fun readdir(path: String): String
    fun unlinkOrRmdir(path: String): Boolean
    fun deleteOrError(path: String): String
    fun rename(src: String, dst: String): Boolean
    fun renameOrError(src: String, dst: String): String
    fun copyFileOrError(src: String, dst: String, overwrite: Boolean): String
    fun symlinkOrError(target: String, link: String): String
    fun readlinkOrError(path: String): String
    fun realpath(path: String): String
    fun diskSpace(path: String, kind: Int): Double

    /**
     * Opens a file and returns an integer file descriptor (`>= 0`), or `-1` on error.
     * `mode` is one of `"r"`, `"w"`, `"a"`, `"r+"`, `"rw"`, `"rws"`, `"rwd"`.
     */
    fun open(path: String, mode: String): Int
    fun lastError(): String
    fun close(fd: Int): Int
    fun read(fd: Int, length: Int, position: Double): String
    fun write(fd: Int, latin1: String, position: Double): Int
    fun lseek(fd: Int, offset: Double, whence: Int): Double
    fun fstatSize(fd: Int): Double
    fun ftruncate(fd: Int, length: Double): Boolean
    fun fsync(fd: Int, metadata: Boolean): Boolean

    /**
     * Checks whether an executable exists (either as a path with `/` or `\` or in `PATH`).
     */
    fun canResolveExecutable(cmd: String, cwd: String, pathEnv: String): Boolean

    /**
     * Runs a command synchronously via `child_process.spawnSync`.
     * `argsJoined` is `\u0000`-separated arguments.
     * `envJoined` is `\u0000`-separated `KEY=VALUE` pairs.
     * `stdinLatin1` is Latin-1 encoded stdin bytes.
     * Returns `"OK|<exitCode>|<stdoutLen>|<stderrLen>|<stdoutLatin1><stderrLatin1>"`,
     * or `"TIMEOUT"` if `timeoutMs > 0` and the process timed out,
     * or `"ERR|<message>"` if spawning failed.
     */
    fun spawnSync(
        cmd: String,
        argsJoined: String,
        cwd: String,
        envJoined: String,
        stdinLatin1: String,
        redirectErrorStream: Boolean,
        timeoutMs: Int
    ): String

    fun dnsLookupAll(host: String): String
    fun tcpBindServer(host: String, port: Int, backlog: Int, reuseAddress: Boolean): String
    fun tcpAccept(serverHandle: Int, timeoutMs: Int): String
    fun tcpConnect(host: String, port: Int, timeoutMs: Int, localHost: String, localPort: Int): String
    fun tcpRead(handle: Int, maxBytes: Int, timeoutMs: Int): String
    fun tcpWrite(handle: Int, latin1: String): Int
    fun tcpAvailable(handle: Int): Int
    fun tcpShutdown(handle: Int, how: Int): Boolean
    fun tcpSetOption(handle: Int, option: String, intVal: Int, boolVal: Boolean): Boolean
    fun tcpClose(handle: Int): Boolean

    fun udpBind(host: String, port: Int, reuseAddress: Boolean): String
    fun udpSend(handle: Int, latin1: String, host: String, port: Int): Int
    fun udpReceive(handle: Int, maxBytes: Int, timeoutMs: Int): String
    fun udpSetBroadcast(handle: Int, on: Boolean): Boolean
    fun udpClose(handle: Int): Boolean

    fun pollHandle(handle: Int, interestOps: Int): Int
    fun selectorWait(packedEntries: String, timeoutMs: Int): Boolean
}

internal fun ByteArray.toLatin1String(offset: Int = 0, length: Int = this.size - offset): String {
    if (length <= 0) return ""
    val chars = CharArray(length) { i -> (this[offset + i].toInt() and 0xFF).toChar() }
    return chars.concatToString()
}

internal fun String.toLatin1ByteArray(): ByteArray {
    if (this.isEmpty()) return ByteArray(0)
    return ByteArray(this.length) { i -> (this[i].code and 0xFF).toByte() }
}
