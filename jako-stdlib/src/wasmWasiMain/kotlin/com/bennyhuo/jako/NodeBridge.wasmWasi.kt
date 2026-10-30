@file:OptIn(kotlin.wasm.WasmImport::class, kotlin.wasm.unsafe.UnsafeWasmMemoryApi::class)

package com.bennyhuo.jako

import com.bennyhuo.jako.annotations.WebRuntime
import kotlin.wasm.WasmImport
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.withScopedMemoryAllocator

private const val CLOCKID_REALTIME = 0
private const val CLOCKID_MONOTONIC = 1

@WasmImport("wasi_snapshot_preview1", "clock_time_get")
private external fun wasiClockTimeGet(clockId: Int, precision: Long, resultPtr: Int): Int

@WasmImport("wasi_snapshot_preview1", "random_get")
private external fun wasiRandomGet(bufPtr: Int, bufLen: Int): Int

@WasmImport("wasi_snapshot_preview1", "fd_write")
private external fun wasiFdWrite(fd: Int, iovsPtr: Int, iovsLen: Int, nwrittenPtr: Int): Int

@WasmImport("wasi_snapshot_preview1", "proc_exit")
private external fun wasiProcExit(rval: Int)

@WasmImport("wasi_snapshot_preview1", "environ_sizes_get")
private external fun wasiEnvironSizesGet(environCountPtr: Int, environBufSizePtr: Int): Int

@WasmImport("wasi_snapshot_preview1", "environ_get")
private external fun wasiEnvironGet(environPtr: Int, environBufPtr: Int): Int

@WasmImport("wasi_snapshot_preview1", "poll_oneoff")
private external fun wasiPollOneoff(inPtr: Int, outPtr: Int, nsubscriptions: Int, neventsPtr: Int): Int

internal actual object NodeBridge {
    private var nextIno = 1
    private var nextFd = 10
    private var lastErrStr = ""

    private sealed class VfsNode(
        val ino: Int,
        var mode: Int,
        var mtimeMs: Double,
        var atimeMs: Double,
        val ctimeMs: Double
    )

    private class DirNode(
        ino: Int,
        mode: Int = 0b111_101_101, // 0755
        mtimeMs: Double,
        atimeMs: Double = mtimeMs,
        ctimeMs: Double = mtimeMs
    ) : VfsNode(ino, mode, mtimeMs, atimeMs, ctimeMs) {
        val children = LinkedHashMap<String, VfsNode>()
    }

    private class FileNode(
        ino: Int,
        mode: Int = 0b110_100_100, // 0644
        mtimeMs: Double,
        atimeMs: Double = mtimeMs,
        ctimeMs: Double = mtimeMs
    ) : VfsNode(ino, mode, mtimeMs, atimeMs, ctimeMs) {
        var bytes: ByteArray = ByteArray(0)
        var size: Int = 0

        fun ensureCapacity(minCap: Int) {
            if (minCap > bytes.size) {
                var newCap = if (bytes.isEmpty()) 64 else bytes.size * 2
                if (newCap < minCap) newCap = minCap
                bytes = bytes.copyOf(newCap)
            }
        }
    }

    private class SymlinkNode(
        ino: Int,
        val target: String,
        mtimeMs: Double,
        atimeMs: Double = mtimeMs,
        ctimeMs: Double = mtimeMs
    ) : VfsNode(ino, 0b111_111_111, mtimeMs, atimeMs, ctimeMs)

    private class FdEntry(
        val node: FileNode,
        val readable: Boolean,
        val writable: Boolean,
        val append: Boolean,
        var pos: Long
    )

    private val rootDir: DirNode
    private val fdTable = HashMap<Int, FdEntry>()
    private val envCache: Map<String, String> by lazy { loadWasiEnviron() }

    init {
        val now = currentTimeMillis()
        rootDir = DirNode(ino = nextIno++, mtimeMs = now)
        val tmpNode = DirNode(ino = nextIno++, mtimeMs = now)
        val homeNode = DirNode(ino = nextIno++, mtimeMs = now)
        val userHomeNode = DirNode(ino = nextIno++, mtimeMs = now)
        homeNode.children["wasi"] = userHomeNode
        rootDir.children["tmp"] = tmpNode
        rootDir.children["home"] = homeNode
    }

    private fun setErr(code: String, msg: String): String {
        lastErrStr = "$code|$msg"
        return lastErrStr
    }

    actual fun hostRuntime(): WebRuntime = WebRuntime.WASI

    actual fun currentTimeMillis(): Double = withScopedMemoryAllocator { allocator ->
        val ptr = allocator.allocate(8)
        val err = wasiClockTimeGet(CLOCKID_REALTIME, 1_000_000L, ptr.address.toInt())
        if (err == 0) {
            (ptr.loadLong() / 1_000_000L).toDouble()
        } else {
            0.0
        }
    }

    actual fun nanoTime(): Double = withScopedMemoryAllocator { allocator ->
        val ptr = allocator.allocate(8)
        val err = wasiClockTimeGet(CLOCKID_MONOTONIC, 1L, ptr.address.toInt())
        if (err == 0) {
            ptr.loadLong().toDouble()
        } else {
            0.0
        }
    }

    actual fun sleepMillis(ms: Int) {
        if (ms <= 0) return
        withScopedMemoryAllocator { allocator ->
            // WASI subscription_t is 48 bytes, event_t is 32 bytes
            val subPtr = allocator.allocate(48)
            val evtPtr = allocator.allocate(32)
            val neventsPtr = allocator.allocate(4)
            for (i in 0 until 48 step 4) {
                (subPtr + i).storeInt(0)
            }
            // userdata = 0 (offset 0, 8 bytes)
            // u.tag = 0 (EVENTTYPE_CLOCK at offset 8, 1 byte)
            // u.u.clock.id = CLOCKID_MONOTONIC (1) at offset 16 (4 bytes)
            (subPtr + 16).storeInt(CLOCKID_MONOTONIC)
            // u.u.clock.timeout = ms * 1_000_000L at offset 24 (8 bytes)
            (subPtr + 24).storeLong(ms.toLong() * 1_000_000L)
            // u.u.clock.precision = 1_000_000L at offset 32 (8 bytes)
            (subPtr + 32).storeLong(1_000_000L)
            // u.u.clock.flags = 0 (relative) at offset 40 (2 bytes)
            wasiPollOneoff(subPtr.address.toInt(), evtPtr.address.toInt(), 1, neventsPtr.address.toInt())
        }
    }

    actual fun pid(): Int = 1
    actual fun uid(): Int = 1000
    actual fun availableProcessors(): Int = 1
    actual fun osName(): String = "WASI"
    actual fun osArch(): String = "wasm32"
    actual fun osVersion(): String = "preview1"
    actual fun fileSeparator(): String = "/"
    actual fun pathSeparator(): String = ":"
    actual fun lineSeparator(): String = "\n"
    actual fun userDir(): String = "/home/wasi"
    actual fun userHome(): String = "/home/wasi"
    actual fun userName(): String = "wasi"
    actual fun tmpDir(): String = "/tmp"
    actual fun hostname(): String = "localhost"

    private fun loadWasiEnviron(): Map<String, String> = withScopedMemoryAllocator { allocator ->
        val countPtr = allocator.allocate(4)
        val bufSizePtr = allocator.allocate(4)
        if (wasiEnvironSizesGet(countPtr.address.toInt(), bufSizePtr.address.toInt()) != 0) {
            return@withScopedMemoryAllocator emptyMap()
        }
        val count = countPtr.loadInt()
        val bufSize = bufSizePtr.loadInt()
        if (count <= 0 || bufSize <= 0) {
            return@withScopedMemoryAllocator emptyMap()
        }
        val ptrs = allocator.allocate(count * 4)
        val buf = allocator.allocate(bufSize)
        if (wasiEnvironGet(ptrs.address.toInt(), buf.address.toInt()) != 0) {
            return@withScopedMemoryAllocator emptyMap()
        }
        val map = LinkedHashMap<String, String>(count)
        for (i in 0 until count) {
            val strAddr = (ptrs + i * 4).loadInt()
            var p = Pointer(strAddr.toUInt())
            val bytes = ByteArray(bufSize)
            var len = 0
            while (len < bufSize) {
                val b = p.loadByte()
                if (b.toInt() == 0) break
                bytes[len++] = b
                p += 1
            }
            val entry = bytes.decodeToString(0, len)
            val eq = entry.indexOf('=')
            if (eq > 0) {
                map[entry.substring(0, eq)] = entry.substring(eq + 1)
            }
        }
        map
    }

    actual fun getenv(name: String): String? = envCache[name]

    actual fun getenvAll(): String =
        envCache.entries.joinToString("\u0000") { "${it.key}=${it.value}" }

    private fun writeFd(fd: Int, latin1: String) {
        if (latin1.isEmpty()) return
        withScopedMemoryAllocator { allocator ->
            val len = latin1.length
            val dataPtr = allocator.allocate(len)
            for (i in 0 until len) {
                (dataPtr + i).storeByte((latin1[i].code and 0xFF).toByte())
            }
            val iovPtr = allocator.allocate(8)
            iovPtr.storeInt(dataPtr.address.toInt())
            (iovPtr + 4).storeInt(len)
            val nwrittenPtr = allocator.allocate(4)
            wasiFdWrite(fd, iovPtr.address.toInt(), 1, nwrittenPtr.address.toInt())
        }
    }

    actual fun writeStdout(latin1: String) = writeFd(1, latin1)
    actual fun writeStderr(latin1: String) = writeFd(2, latin1)

    actual fun exit(status: Int) {
        wasiProcExit(status)
    }

    actual fun randomBytesLatin1(length: Int): String {
        if (length <= 0) return ""
        return withScopedMemoryAllocator { allocator ->
            val bufPtr = allocator.allocate(length)
            val err = wasiRandomGet(bufPtr.address.toInt(), length)
            if (err == 0) {
                val chars = CharArray(length) { i -> ((bufPtr + i).loadByte().toInt() and 0xFF).toChar() }
                chars.concatToString()
            } else {
                val bytes = ByteArray(length)
                kotlin.random.Random.nextBytes(bytes)
                bytes.toLatin1String()
            }
        }
    }

    private fun normalizeAbsoluteSegments(rawPath: String): List<String> {
        val replaced = rawPath.replace('\\', '/')
        val full = if (replaced.startsWith("/")) replaced else "${userDir()}/$replaced"
        val out = ArrayList<String>()
        for (seg in full.split('/')) {
            when (seg) {
                "", "." -> {}
                ".." -> if (out.isNotEmpty()) out.removeAt(out.lastIndex)
                else -> out.add(seg)
            }
        }
        return out
    }

    private fun resolveNode(
        rawPath: String,
        followFinalSymlink: Boolean,
        hopLimit: Int = 32
    ): VfsNode? {
        if (rawPath.isEmpty()) return null
        val segments = normalizeAbsoluteSegments(rawPath)
        return resolveSegments(segments, followFinalSymlink, hopLimit)
    }

    private fun resolveSegments(
        segments: List<String>,
        followFinalSymlink: Boolean,
        hopLimit: Int
    ): VfsNode? {
        if (hopLimit <= 0) {
            setErr("ELOOP", "Too many symbolic links")
            return null
        }
        var curr: VfsNode = rootDir
        val traversed = ArrayList<String>()
        for (i in segments.indices) {
            val seg = segments[i]
            val isLast = (i == segments.lastIndex)
            val dir = curr as? DirNode ?: run {
                setErr("ENOTDIR", "Not a directory")
                return null
            }
            val next = dir.children[seg] ?: run {
                setErr("ENOENT", "No such file or directory")
                return null
            }
            if (next is SymlinkNode && (!isLast || followFinalSymlink)) {
                val targetPath = if (next.target.startsWith("/")) {
                    next.target
                } else {
                    "/" + traversed.joinToString("/") + "/" + next.target
                }
                val targetSegments = normalizeAbsoluteSegments(targetPath)
                val remaining = if (isLast) emptyList() else segments.subList(i + 1, segments.size)
                return resolveSegments(targetSegments + remaining, followFinalSymlink, hopLimit - 1)
            }
            traversed.add(seg)
            curr = next
        }
        return curr
    }

    private fun resolveParentAndName(rawPath: String): Pair<DirNode, String>? {
        if (rawPath.isEmpty()) {
            setErr("ENOENT", "Empty path")
            return null
        }
        val segments = normalizeAbsoluteSegments(rawPath)
        if (segments.isEmpty()) {
            setErr("EEXIST", "Root directory")
            return null
        }
        val parentSegments = segments.subList(0, segments.lastIndex)
        val parentNode = resolveSegments(parentSegments, followFinalSymlink = true, hopLimit = 32)
        if (parentNode == null) return null
        val dir = parentNode as? DirNode ?: run {
            setErr("ENOTDIR", "Parent is not a directory")
            return null
        }
        return dir to segments.last()
    }

    actual fun stat(path: String, followSymlinks: Boolean): String {
        val node = resolveNode(path, followSymlinks) ?: return ""
        val type = when (node) {
            is FileNode -> 1
            is DirNode -> 2
            is SymlinkNode -> 3
        }
        val size = when (node) {
            is FileNode -> node.size
            is DirNode -> 4096
            is SymlinkNode -> node.target.length
        }
        val mtime = node.mtimeMs.toLong()
        val atime = node.atimeMs.toLong()
        val ctime = node.ctimeMs.toLong()
        return "$type|$size|$mtime|$atime|$ctime|${node.mode}|${node.ino}"
    }

    actual fun access(path: String, mode: Int): Boolean {
        val node = resolveNode(path, followFinalSymlink = true) ?: return false
        if ((mode and 4) != 0 && (node.mode and 0b100_000_000) == 0) return false
        if ((mode and 2) != 0 && (node.mode and 0b010_000_000) == 0) return false
        if ((mode and 1) != 0 && (node.mode and 0b001_000_000) == 0) return false
        return true
    }

    actual fun chmod(path: String, mode: Int): Boolean {
        val node = resolveNode(path, followFinalSymlink = true) ?: return false
        node.mode = mode and 0x1FF
        return true
    }

    actual fun utimes(path: String, atimeMs: Double, mtimeMs: Double): Boolean {
        val node = resolveNode(path, followFinalSymlink = true) ?: return false
        node.atimeMs = atimeMs
        node.mtimeMs = mtimeMs
        return true
    }

    actual fun mkdir(path: String): Boolean = mkdirOrError(path).isEmpty()

    actual fun mkdirOrError(path: String): String {
        val (parent, name) = resolveParentAndName(path) ?: return lastError()
        if (parent.children.containsKey(name)) {
            return setErr("EEXIST", "File exists: $path")
        }
        val now = currentTimeMillis()
        parent.children[name] = DirNode(ino = nextIno++, mtimeMs = now)
        parent.mtimeMs = now
        return ""
    }

    actual fun readdir(path: String): String {
        val node = resolveNode(path, followFinalSymlink = true)
            ?: return "ERR|$lastErrStr"
        val dir = node as? DirNode
            ?: return "ERR|" + setErr("ENOTDIR", "Not a directory: $path")
        return "OK\u0000" + dir.children.keys.joinToString("\u0000")
    }

    actual fun unlinkOrRmdir(path: String): Boolean = deleteOrError(path).isEmpty()

    actual fun deleteOrError(path: String): String {
        val (parent, name) = resolveParentAndName(path) ?: return lastError()
        val existing = parent.children[name]
            ?: return setErr("ENOENT", "No such file or directory: $path")
        if (existing is DirNode && existing.children.isNotEmpty()) {
            return setErr("ENOTEMPTY", "Directory not empty: $path")
        }
        parent.children.remove(name)
        parent.mtimeMs = currentTimeMillis()
        return ""
    }

    actual fun rename(src: String, dst: String): Boolean = renameOrError(src, dst).isEmpty()

    actual fun renameOrError(src: String, dst: String): String {
        val (srcParent, srcName) = resolveParentAndName(src) ?: return lastError()
        val node = srcParent.children[srcName]
            ?: return setErr("ENOENT", "Source does not exist: $src")
        val (dstParent, dstName) = resolveParentAndName(dst) ?: return lastError()
        val existingDst = dstParent.children[dstName]
        if (existingDst != null) {
            if (existingDst is DirNode && node !is DirNode) {
                return setErr("EISDIR", "Target is a directory: $dst")
            }
            if (existingDst !is DirNode && node is DirNode) {
                return setErr("ENOTDIR", "Target is not a directory: $dst")
            }
            if (existingDst is DirNode && existingDst.children.isNotEmpty()) {
                return setErr("ENOTEMPTY", "Target directory not empty: $dst")
            }
        }
        srcParent.children.remove(srcName)
        dstParent.children[dstName] = node
        val now = currentTimeMillis()
        srcParent.mtimeMs = now
        dstParent.mtimeMs = now
        return ""
    }

    actual fun copyFileOrError(src: String, dst: String, overwrite: Boolean): String {
        val srcNode = resolveNode(src, followFinalSymlink = true)
            ?: return lastError()
        val srcFile = srcNode as? FileNode
            ?: return setErr("EISDIR", "Source is not a regular file: $src")
        val (dstParent, dstName) = resolveParentAndName(dst) ?: return lastError()
        val existing = dstParent.children[dstName]
        if (existing != null) {
            if (!overwrite) {
                return setErr("EEXIST", "Target file exists: $dst")
            }
            if (existing is DirNode) {
                return setErr("EISDIR", "Target is a directory: $dst")
            }
        }
        val now = currentTimeMillis()
        val copy = FileNode(ino = nextIno++, mode = srcFile.mode, mtimeMs = now)
        copy.bytes = srcFile.bytes.copyOf(srcFile.size)
        copy.size = srcFile.size
        dstParent.children[dstName] = copy
        return ""
    }

    actual fun symlinkOrError(target: String, link: String): String {
        val (parent, name) = resolveParentAndName(link) ?: return lastError()
        if (parent.children.containsKey(name)) {
            return setErr("EEXIST", "Link already exists: $link")
        }
        val now = currentTimeMillis()
        parent.children[name] = SymlinkNode(ino = nextIno++, target = target, mtimeMs = now)
        return ""
    }

    actual fun readlinkOrError(path: String): String {
        val node = resolveNode(path, followFinalSymlink = false)
            ?: return "ERR|$lastErrStr"
        val sym = node as? SymlinkNode
            ?: return "ERR|" + setErr("EINVAL", "Not a symbolic link: $path")
        return "OK|${sym.target}"
    }

    actual fun realpath(path: String): String {
        val segments = normalizeAbsoluteSegments(path)
        val out = ArrayList<String>()
        var curr: VfsNode = rootDir
        var hops = 0
        var idx = 0
        val queue = ArrayDeque<String>()
        queue.addAll(segments)
        while (queue.isNotEmpty() && hops < 32) {
            val seg = queue.removeFirst()
            val dir = curr as? DirNode ?: break
            val next = dir.children[seg]
            if (next == null) {
                out.add(seg)
                while (queue.isNotEmpty()) out.add(queue.removeFirst())
                break
            }
            if (next is SymlinkNode) {
                hops++
                val targetPath = if (next.target.startsWith("/")) {
                    next.target
                } else {
                    "/" + out.joinToString("/") + "/" + next.target
                }
                val targetSegs = normalizeAbsoluteSegments(targetPath)
                val remaining = queue.toList()
                queue.clear()
                queue.addAll(targetSegs)
                queue.addAll(remaining)
                out.clear()
                curr = rootDir
            } else {
                out.add(seg)
                curr = next
            }
            idx++
        }
        return "/" + out.joinToString("/")
    }

    actual fun diskSpace(path: String, kind: Int): Double {
        return if (resolveNode(path, followFinalSymlink = true) != null) {
            1_000_000_000.0
        } else {
            0.0
        }
    }

    actual fun open(path: String, mode: String): Int {
        val readable = mode == "r" || mode == "rw" || mode == "rws" || mode == "rwd" || mode == "r+"
        val writable = mode == "w" || mode == "a" || mode == "rw" || mode == "rws" || mode == "rwd" || mode == "r+"
        val truncate = mode == "w"
        val append = mode == "a"
        val createIfMissing = writable && mode != "r+"

        val existing = resolveNode(path, followFinalSymlink = true)
        if (existing is DirNode) {
            setErr("EISDIR", "Is a directory: $path")
            return -1
        }
        val fileNode: FileNode = if (existing is FileNode) {
            if (truncate) {
                existing.size = 0
                existing.mtimeMs = currentTimeMillis()
            }
            existing
        } else {
            if (!createIfMissing) {
                setErr("ENOENT", "No such file or directory: $path")
                return -1
            }
            val (parent, name) = resolveParentAndName(path) ?: return -1
            val now = currentTimeMillis()
            val created = FileNode(ino = nextIno++, mtimeMs = now)
            parent.children[name] = created
            parent.mtimeMs = now
            created
        }

        val fd = nextFd++
        val initialPos = if (append) fileNode.size.toLong() else 0L
        fdTable[fd] = FdEntry(
            node = fileNode,
            readable = readable,
            writable = writable,
            append = append,
            pos = initialPos
        )
        return fd
    }

    actual fun lastError(): String = lastErrStr

    actual fun close(fd: Int): Int {
        return if (fdTable.remove(fd) != null) 0 else {
            setErr("EBADF", "Bad file descriptor")
            -1
        }
    }

    actual fun read(fd: Int, length: Int, position: Double): String {
        if (length <= 0) return "1"
        val entry = fdTable[fd] ?: return "E" + setErr("EBADF", "Bad file descriptor")
        if (!entry.readable) return "E" + setErr("EBADF", "File not open for reading")
        val readPos = if (position >= 0.0) position.toLong() else entry.pos
        if (readPos >= entry.node.size) return "0"
        val avail = (entry.node.size - readPos.toInt()).coerceAtMost(length)
        if (avail <= 0) return "0"
        val start = readPos.toInt()
        if (position < 0.0) {
            entry.pos = readPos + avail
        }
        val chars = CharArray(avail + 1)
        chars[0] = '1'
        for (i in 0 until avail) {
            chars[i + 1] = (entry.node.bytes[start + i].toInt() and 0xFF).toChar()
        }
        return chars.concatToString()
    }

    actual fun write(fd: Int, latin1: String, position: Double): Int {
        val entry = fdTable[fd] ?: run {
            setErr("EBADF", "Bad file descriptor")
            return -1
        }
        if (!entry.writable) {
            setErr("EBADF", "File not open for writing")
            return -1
        }
        val len = latin1.length
        val writePos = when {
            position >= 0.0 -> position.toInt()
            entry.append -> entry.node.size
            else -> entry.pos.toInt()
        }
        val endPos = writePos + len
        entry.node.ensureCapacity(endPos)
        if (writePos > entry.node.size) {
            entry.node.bytes.fill(0, entry.node.size, writePos)
        }
        for (i in 0 until len) {
            entry.node.bytes[writePos + i] = (latin1[i].code and 0xFF).toByte()
        }
        if (endPos > entry.node.size) {
            entry.node.size = endPos
        }
        if (position < 0.0) {
            entry.pos = endPos.toLong()
        }
        entry.node.mtimeMs = currentTimeMillis()
        return len
    }

    actual fun lseek(fd: Int, offset: Double, whence: Int): Double {
        val entry = fdTable[fd] ?: run {
            setErr("EBADF", "Bad file descriptor")
            return -1.0
        }
        val base = when (whence) {
            0 -> 0L
            1 -> entry.pos
            2 -> entry.node.size.toLong()
            else -> {
                setErr("EINVAL", "Invalid whence")
                return -1.0
            }
        }
        val newPos = base + offset.toLong()
        if (newPos < 0L) {
            setErr("EINVAL", "Negative seek offset")
            return -1.0
        }
        entry.pos = newPos
        return newPos.toDouble()
    }

    actual fun fstatSize(fd: Int): Double {
        val entry = fdTable[fd] ?: run {
            setErr("EBADF", "Bad file descriptor")
            return -1.0
        }
        return entry.node.size.toDouble()
    }

    actual fun ftruncate(fd: Int, length: Double): Boolean {
        val entry = fdTable[fd] ?: run {
            setErr("EBADF", "Bad file descriptor")
            return false
        }
        if (!entry.writable) {
            setErr("EBADF", "File not open for writing")
            return false
        }
        val newLen = length.toInt().coerceAtLeast(0)
        entry.node.ensureCapacity(newLen)
        if (newLen > entry.node.size) {
            entry.node.bytes.fill(0, entry.node.size, newLen)
        }
        entry.node.size = newLen
        if (entry.pos > newLen) {
            entry.pos = newLen.toLong()
        }
        entry.node.mtimeMs = currentTimeMillis()
        return true
    }

    actual fun fsync(fd: Int, metadata: Boolean): Boolean = fdTable.containsKey(fd)

    actual fun canResolveExecutable(cmd: String, cwd: String, pathEnv: String): Boolean = false

    actual fun spawnSync(
        cmd: String,
        argsJoined: String,
        cwd: String,
        envJoined: String,
        stdinLatin1: String,
        redirectErrorStream: Boolean,
        timeoutMs: Int
    ): String = "ERR|ENOSYS|Process spawning is not supported on WASI"

    actual fun dnsLookupAll(host: String): String = "ERR|ENOSYS|DNS lookup is not supported on WASI"
    actual fun tcpBindServer(host: String, port: Int, backlog: Int, reuseAddress: Boolean): String =
        "ERR|ENOSYS|TCP server sockets are not supported on WASI"
    actual fun tcpAccept(serverHandle: Int, timeoutMs: Int): String =
        "ERR|ENOSYS|TCP server sockets are not supported on WASI"
    actual fun tcpConnect(host: String, port: Int, timeoutMs: Int, localHost: String, localPort: Int): String =
        "ERR|ENOSYS|TCP sockets are not supported on WASI"
    actual fun tcpRead(handle: Int, maxBytes: Int, timeoutMs: Int): String =
        "ERR|ENOSYS|TCP sockets are not supported on WASI"
    actual fun tcpWrite(handle: Int, latin1: String): Int = -1
    actual fun tcpAvailable(handle: Int): Int = 0
    actual fun tcpShutdown(handle: Int, how: Int): Boolean = false
    actual fun tcpSetOption(handle: Int, option: String, intVal: Int, boolVal: Boolean): Boolean = false
    actual fun tcpClose(handle: Int): Boolean = false

    actual fun udpBind(host: String, port: Int, reuseAddress: Boolean): String =
        "ERR|ENOSYS|UDP sockets are not supported on WASI"
    actual fun udpSend(handle: Int, latin1: String, host: String, port: Int): Int = -1
    actual fun udpReceive(handle: Int, maxBytes: Int, timeoutMs: Int): String =
        "ERR|ENOSYS|UDP sockets are not supported on WASI"
    actual fun udpSetBroadcast(handle: Int, on: Boolean): Boolean = false
    actual fun udpClose(handle: Int): Boolean = false

    actual fun pollHandle(handle: Int, interestOps: Int): Int = 0
    actual fun selectorWait(packedEntries: String, timeoutMs: Int): Boolean = false
}
