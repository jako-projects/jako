@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.bennyhuo.jako

import com.bennyhuo.jako.annotations.WebRuntime

@JsFun(
    """() => {
        if (!globalThis.__jakoNodeBridge) {
            const isNode = typeof process !== 'undefined' && process !== null && process.versions != null && process.versions.node != null;
            const req = isNode ? (typeof process.getBuiltinModule === 'function' ? process.getBuiltinModule.bind(process) : (typeof require !== 'undefined' ? require : null)) : null;
            const sleepBuf = typeof SharedArrayBuffer !== 'undefined' ? new Int32Array(new SharedArrayBuffer(4)) : null;
            if (!isNode || !req) {
                const perfStart = (typeof performance !== 'undefined' && typeof performance.now === 'function') ? performance.now() : Date.now();
                globalThis.__jakoNodeBridge = {
                    isNodeJs() { return false; },
                    currentTimeMillis() { return Date.now(); },
                    nanoTime() {
                        const now = (typeof performance !== 'undefined' && typeof performance.now === 'function')
                            ? (performance.now() - perfStart)
                            : Date.now();
                        return Math.floor(now * 1000000);
                    },
                    sleepMillis(ms) {
                        if (ms <= 0) return;
                        if (sleepBuf && typeof Atomics !== 'undefined' && typeof Atomics.wait === 'function') {
                            try {
                                Atomics.wait(sleepBuf, 0, 0, ms);
                                return;
                            } catch (_) {}
                        }
                        const end = Date.now() + ms;
                        while (Date.now() < end) {}
                    },
                    pid() { return 1; },
                    uid() { return 1000; },
                    availableProcessors() {
                        return (typeof navigator !== 'undefined' && navigator.hardwareConcurrency > 0) ? navigator.hardwareConcurrency : 1;
                    },
                    osName() { return "Browser"; },
                    osArch() { return "wasm32"; },
                    osVersion() { return (typeof navigator !== 'undefined' && navigator.userAgent) ? String(navigator.userAgent) : ""; },
                    fileSeparator() { return "/"; },
                    pathSeparator() { return ":"; },
                    lineSeparator() { return "\n"; },
                    userDir() { return "/"; },
                    userHome() { return "/"; },
                    userName() { return "browser"; },
                    tmpDir() { return "/tmp"; },
                    hostname() { return (typeof location !== 'undefined' && location.hostname) ? String(location.hostname) : "localhost"; },
                    getenv(name) { return null; },
                    getenvAll() { return ""; },
                    writeStdout(latin1) { if (typeof console !== 'undefined' && typeof console.log === 'function') console.log(latin1); },
                    writeStderr(latin1) { if (typeof console !== 'undefined' && typeof console.error === 'function') console.error(latin1); },
                    exit(status) {},
                    randomBytesLatin1(len) {
                        if (len <= 0) return "";
                        if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
                            const arr = new Uint8Array(len);
                            crypto.getRandomValues(arr);
                            let s = "";
                            for (let i = 0; i < len; i++) s += String.fromCharCode(arr[i]);
                            return s;
                        }
                        let s = "";
                        for (let i = 0; i < len; i++) s += String.fromCharCode(Math.floor(Math.random() * 256));
                        return s;
                    },
                    stat(p, followSymlinks) { return ""; },
                    access(p, mode) { return false; },
                    chmod(p, mode) { return false; },
                    utimes(p, atimeMs, mtimeMs) { return false; },
                    mkdir(p) { return false; },
                    mkdirOrError(p) { return "ENOSYS|Not supported in Browser"; },
                    readdir(p) { return "ERR|ENOSYS|Not supported in Browser"; },
                    unlinkOrRmdir(p) { return false; },
                    deleteOrError(p) { return "ENOSYS|Not supported in Browser"; },
                    rename(src, dst) { return false; },
                    renameOrError(src, dst) { return "ENOSYS|Not supported in Browser"; },
                    copyFileOrError(src, dst, overwrite) { return "ENOSYS|Not supported in Browser"; },
                    symlinkOrError(target, link) { return "ENOSYS|Not supported in Browser"; },
                    readlinkOrError(p) { return "ERR|ENOSYS|Not supported in Browser"; },
                    realpath(p) { return p; },
                    diskSpace(p, kind) { return 0.0; },
                    open(p, mode) { return -1; },
                    lastError() { return "ENOSYS|Not supported in Browser"; },
                    close(fd) { return -1; },
                    read(fd, length, position) { return "EENOSYS|Not supported in Browser"; },
                    write(fd, latin1, position) { return -1; },
                    lseek(fd, offset, whence) { return -1.0; },
                    fstatSize(fd) { return -1.0; },
                    ftruncate(fd, len) { return false; },
                    fsync(fd, meta) { return false; },
                    canResolveExecutable(cmd, cwd, pathEnv) { return false; },
                    spawnSync(cmd, argsJoined, cwd, envJoined, stdinLatin1, redirectErrorStream, timeoutMs) { return "ERR|ENOSYS|Not supported in Browser"; },
                    dnsLookupAll(host) { return "ERR|ENOSYS|Not supported in Browser"; },
                    tcpBindServer(host, port, backlog, reuseAddress) { return "ERR|ENOSYS|Not supported in Browser"; },
                    tcpAccept(handle, timeoutMs) { return "ERR|ENOSYS|Not supported in Browser"; },
                    tcpConnect(host, port, timeoutMs, localHost, localPort) { return "ERR|ENOSYS|Not supported in Browser"; },
                    tcpRead(handle, maxBytes, timeoutMs) { return "ERR|ENOSYS|Not supported in Browser"; },
                    tcpWrite(handle, latin1) { return -1; },
                    tcpAvailable(handle) { return 0; },
                    tcpShutdown(handle, how) { return false; },
                    tcpSetOption(handle, option, intVal, boolVal) { return false; },
                    tcpClose(handle) { return false; },
                    udpBind(host, port, reuseAddress) { return "ERR|ENOSYS|Not supported in Browser"; },
                    udpSend(handle, latin1, host, port) { return -1; },
                    udpReceive(handle, maxBytes, timeoutMs) { return "ERR|ENOSYS|Not supported in Browser"; },
                    udpSetBroadcast(handle, on) { return false; },
                    udpClose(handle) { return false; },
                    pollHandle(handle, interestOps) { return 0; },
                    selectorWait(packedEntries, timeoutMs) { return false; }
                };
            } else {
            const fs = req('fs');
            const os = req('os');
            const path = req('path');
            const crypto = req('crypto');
            const child_process = req('child_process');
            const fdTable = new Map();
            const hrStart = process.hrtime.bigint() - BigInt(1000000000);
            let lastErrStr = "";

            function setErr(e) {
                const code = (e && e.code) ? String(e.code) : "EIO";
                const msg = (e && e.message) ? String(e.message) : String(e);
                lastErrStr = code + "|" + msg;
                return lastErrStr;
            }

            globalThis.__jakoNodeBridge = {
                isNodeJs() { return true; },
                currentTimeMillis() { return Date.now(); },
                nanoTime() { return Number(process.hrtime.bigint() - hrStart); },
                sleepMillis(ms) {
                    if (ms <= 0) return;
                    if (sleepBuf && typeof Atomics !== 'undefined' && typeof Atomics.wait === 'function') {
                        try {
                            Atomics.wait(sleepBuf, 0, 0, ms);
                            return;
                        } catch (_) {}
                    }
                    const end = Date.now() + ms;
                    while (Date.now() < end) {}
                },
                pid() { return process.pid || 1; },
                uid() { return (typeof process.getuid === 'function') ? process.getuid() : 1000; },
                availableProcessors() {
                    try {
                        const cpus = os.cpus();
                        return (cpus && cpus.length > 0) ? cpus.length : 1;
                    } catch (_) { return 1; }
                },
                osName() {
                    const p = process.platform;
                    if (p === 'darwin') return 'Mac OS X';
                    if (p === 'win32') return 'Windows';
                    if (p === 'linux') return 'Linux';
                    return p;
                },
                osArch() {
                    const a = process.arch;
                    if (a === 'arm64') return 'aarch64';
                    if (a === 'x64') return 'x86_64';
                    return a;
                },
                osVersion() {
                    try { return os.release(); } catch (_) { return ""; }
                },
                fileSeparator() { return path.sep; },
                pathSeparator() { return path.delimiter; },
                lineSeparator() { return os.EOL; },
                userDir() {
                    try { return process.cwd(); } catch (_) { return "."; }
                },
                userHome() {
                    try { return os.homedir(); } catch (_) { return "/"; }
                },
                userName() {
                    try { return os.userInfo().username; } catch (_) { return process.env.USER || process.env.USERNAME || "user"; }
                },
                tmpDir() {
                    try { return os.tmpdir(); } catch (_) { return "/tmp"; }
                },
                hostname() {
                    try { return os.hostname(); } catch (_) { return "localhost"; }
                },
                getenv(name) {
                    const v = process.env[name];
                    return v !== undefined ? String(v) : null;
                },
                getenvAll() {
                    const entries = [];
                    for (const [k, v] of Object.entries(process.env)) {
                        if (v !== undefined) entries.push(k + "=" + v);
                    }
                    return entries.join("\u0000");
                },
                writeStdout(latin1) {
                    try {
                        const buf = Buffer.from(latin1, 'latin1');
                        fs.writeSync(1, buf, 0, buf.length);
                    } catch (_) {}
                },
                writeStderr(latin1) {
                    try {
                        const buf = Buffer.from(latin1, 'latin1');
                        fs.writeSync(2, buf, 0, buf.length);
                    } catch (_) {}
                },
                exit(status) { process.exit(status); },
                randomBytesLatin1(len) {
                    if (len <= 0) return "";
                    return crypto.randomBytes(len).toString('latin1');
                },
                stat(p, followSymlinks) {
                    try {
                        const st = followSymlinks ? fs.statSync(p) : fs.lstatSync(p);
                        let type = 4;
                        if (st.isSymbolicLink()) type = 3;
                        else if (st.isDirectory()) type = 2;
                        else if (st.isFile()) type = 1;
                        const mtime = Math.floor(st.mtimeMs);
                        const atime = Math.floor(st.atimeMs);
                        const ctime = Math.floor(st.birthtimeMs || st.ctimeMs);
                        return type + "|" + st.size + "|" + mtime + "|" + atime + "|" + ctime + "|" + st.mode + "|" + (st.ino || 0);
                    } catch (e) {
                        if (e && (e.code === 'ENOENT' || e.code === 'ENOTDIR')) return "";
                        return "ERR|" + setErr(e);
                    }
                },
                access(p, mode) {
                    try {
                        fs.accessSync(p, mode);
                        if (process.platform === 'win32' && (mode & 2) !== 0) {
                            const st = fs.statSync(p);
                            if ((st.mode & 0o200) === 0) return false;
                        }
                        return true;
                    } catch (_) {
                        return false;
                    }
                },
                chmod(p, mode) {
                    try {
                        fs.chmodSync(p, mode);
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                utimes(p, atimeMs, mtimeMs) {
                    try {
                        fs.utimesSync(p, atimeMs / 1000.0, mtimeMs / 1000.0);
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                mkdir(p) {
                    try {
                        fs.mkdirSync(p);
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                mkdirOrError(p) {
                    try {
                        fs.mkdirSync(p);
                        return "";
                    } catch (e) {
                        return setErr(e);
                    }
                },
                readdir(p) {
                    try {
                        const list = fs.readdirSync(p);
                        return "OK\u0000" + list.join("\u0000");
                    } catch (e) {
                        return "ERR|" + setErr(e);
                    }
                },
                unlinkOrRmdir(p) {
                    try {
                        const st = fs.lstatSync(p);
                        if (st.isDirectory()) {
                            fs.rmdirSync(p);
                        } else {
                            fs.unlinkSync(p);
                        }
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                deleteOrError(p) {
                    try {
                        const st = fs.lstatSync(p);
                        if (st.isDirectory()) {
                            fs.rmdirSync(p);
                        } else {
                            fs.unlinkSync(p);
                        }
                        return "";
                    } catch (e) {
                        return setErr(e);
                    }
                },
                rename(src, dst) {
                    try {
                        fs.renameSync(src, dst);
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                renameOrError(src, dst) {
                    try {
                        fs.renameSync(src, dst);
                        return "";
                    } catch (e) {
                        return setErr(e);
                    }
                },
                copyFileOrError(src, dst, overwrite) {
                    try {
                        const flags = overwrite ? 0 : fs.constants.COPYFILE_EXCL;
                        fs.copyFileSync(src, dst, flags);
                        return "";
                    } catch (e) {
                        return setErr(e);
                    }
                },
                symlinkOrError(target, link) {
                    try {
                        fs.symlinkSync(target, link);
                        return "";
                    } catch (e) {
                        return setErr(e);
                    }
                },
                readlinkOrError(p) {
                    try {
                        return "OK|" + fs.readlinkSync(p);
                    } catch (e) {
                        return "ERR|" + setErr(e);
                    }
                },
                realpath(p) {
                    const resolved = path.resolve(p);
                    try {
                        return fs.realpathSync(resolved);
                    } catch (_) {
                        let curr = resolved;
                        const tail = [];
                        while (true) {
                            const parent = path.dirname(curr);
                            if (parent === curr) break;
                            tail.unshift(path.basename(curr));
                            curr = parent;
                            try {
                                const baseReal = fs.realpathSync(curr);
                                return path.join(baseReal, ...tail);
                            } catch (_) {}
                        }
                        return resolved;
                    }
                },
                diskSpace(p, kind) {
                    try {
                        if (typeof fs.statfsSync === 'function') {
                            const st = fs.statfsSync(p);
                            const bsize = Number(st.bsize || 4096);
                            if (kind === 0) return Number(st.blocks) * bsize;
                            if (kind === 1) return Number(st.bfree) * bsize;
                            if (kind === 2) return Number(st.bavail) * bsize;
                        }
                        const st = fs.statSync(p);
                        return st ? 1000000000.0 : 0.0;
                    } catch (_) {
                        return 0.0;
                    }
                },
                open(p, mode) {
                    try {
                        let flags = 'r';
                        let isAppend = false;
                        let syncData = false;
                        let syncMeta = false;
                        if (mode === 'r') {
                            const st = fs.statSync(p);
                            if (st.isDirectory()) {
                                lastErrStr = "EISDIR|Is a directory";
                                return -1;
                            }
                            flags = 'r';
                        } else if (mode === 'w') {
                            flags = 'w';
                        } else if (mode === 'a') {
                            flags = 'a';
                            isAppend = true;
                        } else if (mode === 'rw' || mode === 'rws' || mode === 'rwd' || mode === 'r+') {
                            syncMeta = (mode === 'rws');
                            syncData = (mode === 'rwd');
                            if (fs.existsSync(p)) {
                                const st = fs.statSync(p);
                                if (st.isDirectory()) {
                                    lastErrStr = "EISDIR|Is a directory";
                                    return -1;
                                }
                                flags = 'r+';
                            } else {
                                flags = 'w+';
                            }
                        }
                        const fd = fs.openSync(p, flags, 0o666);
                        let initialPos = 0;
                        if (isAppend) {
                            try { initialPos = fs.fstatSync(fd).size; } catch (_) {}
                        }
                        fdTable.set(fd, { pos: initialPos, append: isAppend, syncData: syncData, syncMeta: syncMeta });
                        return fd;
                    } catch (e) {
                        setErr(e);
                        return -1;
                    }
                },
                lastError() { return lastErrStr; },
                close(fd) {
                    try {
                        fdTable.delete(fd);
                        fs.closeSync(fd);
                        return 0;
                    } catch (e) {
                        setErr(e);
                        return -1;
                    }
                },
                read(fd, length, position) {
                    if (length <= 0) return "1";
                    try {
                        const entry = fdTable.get(fd);
                        const readPos = position >= 0 ? position : (entry ? entry.pos : null);
                        const buf = Buffer.allocUnsafe(length);
                        const n = fs.readSync(fd, buf, 0, length, readPos);
                        if (n <= 0) return "0";
                        if (position < 0 && entry) entry.pos += n;
                        return "1" + buf.subarray(0, n).toString('latin1');
                    } catch (e) {
                        return "E" + setErr(e);
                    }
                },
                write(fd, latin1, position) {
                    try {
                        const entry = fdTable.get(fd);
                        const buf = Buffer.from(latin1, 'latin1');
                        let writePos;
                        if (position >= 0) {
                            writePos = position;
                        } else if (entry && entry.append) {
                            writePos = fs.fstatSync(fd).size;
                        } else {
                            writePos = entry ? entry.pos : null;
                        }
                        const n = fs.writeSync(fd, buf, 0, buf.length, writePos);
                        if (position < 0 && entry && writePos !== null) {
                            entry.pos = writePos + n;
                        }
                        if (entry && entry.syncMeta) fs.fsyncSync(fd);
                        else if (entry && entry.syncData) fs.fdatasyncSync(fd);
                        return n;
                    } catch (e) {
                        setErr(e);
                        return -1;
                    }
                },
                lseek(fd, offset, whence) {
                    try {
                        let entry = fdTable.get(fd);
                        if (!entry) {
                            entry = { pos: 0, append: false, syncData: false, syncMeta: false };
                            fdTable.set(fd, entry);
                        }
                        let newPos = 0;
                        if (whence === 0) newPos = offset;
                        else if (whence === 1) newPos = entry.pos + offset;
                        else if (whence === 2) newPos = fs.fstatSync(fd).size + offset;
                        else return -1.0;
                        if (newPos < 0) {
                            lastErrStr = "EINVAL|Negative seek offset";
                            return -1.0;
                        }
                        entry.pos = newPos;
                        return newPos;
                    } catch (e) {
                        setErr(e);
                        return -1.0;
                    }
                },
                fstatSize(fd) {
                    try {
                        return Number(fs.fstatSync(fd).size);
                    } catch (e) {
                        setErr(e);
                        return -1.0;
                    }
                },
                ftruncate(fd, len) {
                    try {
                        fs.ftruncateSync(fd, len);
                        const entry = fdTable.get(fd);
                        if (entry && entry.pos > len) entry.pos = len;
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                fsync(fd, meta) {
                    try {
                        if (meta) fs.fsyncSync(fd);
                        else fs.fdatasyncSync(fd);
                        return true;
                    } catch (e) {
                        setErr(e);
                        return false;
                    }
                },
                canResolveExecutable(cmd, cwd, pathEnv) {
                    if (!cmd) return false;
                    if (cmd.includes('/') || cmd.includes('\\')) {
                        const full = path.resolve(cwd || process.cwd(), cmd);
                        try {
                            const st = fs.statSync(full);
                            return st.isFile();
                        } catch (_) { return false; }
                    }
                    const dirs = (pathEnv || process.env.PATH || "").split(path.delimiter);
                    const exts = process.platform === 'win32' ? ['', '.exe', '.cmd', '.bat', '.com'] : [''];
                    for (const d of dirs) {
                        if (!d) continue;
                        for (const ext of exts) {
                            const candidate = path.join(d, cmd + ext);
                            try {
                                const st = fs.statSync(candidate);
                                if (st.isFile()) return true;
                            } catch (_) {}
                        }
                    }
                    return false;
                },
                spawnSync(cmd, argsJoined, cwd, envJoined, stdinLatin1, redirectErrorStream, timeoutMs) {
                    try {
                        const args = argsJoined.length === 0 ? [] : argsJoined.split("\u0000");
                        const env = {};
                        if (envJoined.length > 0) {
                            for (const pair of envJoined.split("\u0000")) {
                                const idx = pair.indexOf('=');
                                if (idx > 0) {
                                    env[pair.substring(0, idx)] = pair.substring(idx + 1);
                                }
                            }
                        }
                        const opts = {
                            cwd: cwd || process.cwd(),
                            env: env,
                            input: Buffer.from(stdinLatin1, 'latin1'),
                            maxBuffer: 64 * 1024 * 1024
                        };
                        if (timeoutMs > 0) opts.timeout = timeoutMs;
                        const res = child_process.spawnSync(cmd, args, opts);
                        if (res.error) {
                            if (res.error.code === 'ETIMEDOUT') return "TIMEOUT";
                            return "ERR|" + setErr(res.error);
                        }
                        const outBuf = res.stdout ? Buffer.from(res.stdout) : Buffer.alloc(0);
                        const errBuf = res.stderr ? Buffer.from(res.stderr) : Buffer.alloc(0);
                        let finalOut, finalErr;
                        if (redirectErrorStream) {
                            finalOut = Buffer.concat([outBuf, errBuf]).toString('latin1');
                            finalErr = "";
                        } else {
                            finalOut = outBuf.toString('latin1');
                            finalErr = errBuf.toString('latin1');
                        }
                        const code = res.status !== null && res.status !== undefined ? res.status : (res.signal ? 143 : 0);
                        return "OK|" + code + "|" + finalOut.length + "|" + finalErr.length + "|" + finalOut + finalErr;
                    } catch (e) {
                        return "ERR|" + setErr(e);
                    }
                }
            };

            const worker_threads = req('worker_threads');
            let netWorker = null;
            let netPort = null;
            let netCtrlArr = null;
            let netReqSeq = 0;

            const workerLines = [
                "const { workerData } = require('worker_threads');",
                "const net = require('net');",
                "const dgram = require('dgram');",
                "const dns = require('dns');",
                "const ctrlArr = new Int32Array(workerData.ctrlSab);",
                "const port = workerData.port;",
                "let nextHandle = 1;",
                "const handles = new Map();",
                "const globalWaiters = [];",
                "function notifyGlobalWaiters() {",
                "  if (globalWaiters.length === 0) return;",
                "  const copy = globalWaiters.splice(0);",
                "  for (const fn of copy) { try { fn(); } catch (_) {} }",
                "}",
                "function sendReply(reqId, result) {",
                "  port.postMessage({ reqId, result });",
                "  if (Atomics.load(ctrlArr, 1) === reqId) {",
                "    Atomics.store(ctrlArr, 0, 1);",
                "    Atomics.notify(ctrlArr, 0, 1);",
                "  }",
                "}",
                "function fmtErr(err, fallbackCode) {",
                "  const code = (err && err.code) ? String(err.code) : (fallbackCode || 'EIO');",
                "  const msg = (err && err.message) ? String(err.message) : String(err || 'Error');",
                "  return 'ERR|' + code + '|' + msg;",
                "}",
                "function createSocketEntry(sock, connected) {",
                "  const id = nextHandle++;",
                "  const entry = {",
                "    id, type: 'tcpSocket', sock, connected: !!connected, connectError: null,",
                "    chunks: [], bufferedBytes: 0, eof: false, closed: false,",
                "    inputShutdown: false, outputShutdown: false, error: null, waiters: []",
                "  };",
                "  handles.set(id, entry);",
                "  function notifyEntry() {",
                "    if (entry.waiters.length > 0) {",
                "      const w = entry.waiters.splice(0);",
                "      for (const fn of w) { try { fn(); } catch (_) {} }",
                "    }",
                "    notifyGlobalWaiters();",
                "  }",
                "  entry.notify = notifyEntry;",
                "  sock.on('data', (chunk) => {",
                "    if (entry.inputShutdown || entry.closed) return;",
                "    if (chunk && chunk.length > 0) {",
                "      const b = Buffer.from(chunk);",
                "      entry.chunks.push(b);",
                "      entry.bufferedBytes += b.length;",
                "      notifyEntry();",
                "    }",
                "  });",
                "  sock.on('end', () => { entry.eof = true; notifyEntry(); });",
                "  sock.on('error', (err) => {",
                "    entry.error = err;",
                "    if (!entry.connected) entry.connectError = err;",
                "    notifyEntry();",
                "  });",
                "  sock.on('close', () => { entry.closed = true; entry.eof = true; notifyEntry(); });",
                "  return entry;",
                "}",
                "function checkReady(handle, interestOps) {",
                "  const entry = handles.get(handle);",
                "  if (!entry || entry.closed) return 0;",
                "  let ready = 0;",
                "  if (entry.type === 'tcpServer') {",
                "    if ((interestOps & 16) !== 0 && entry.acceptQueue.length > 0) ready |= 16;",
                "  } else if (entry.type === 'tcpSocket') {",
                "    if ((interestOps & 8) !== 0 && entry.connected) ready |= 8;",
                "    if ((interestOps & 1) !== 0 && !entry.inputShutdown && (entry.bufferedBytes > 0 || entry.eof || entry.error)) ready |= 1;",
                "    if ((interestOps & 4) !== 0 && entry.connected && !entry.outputShutdown) ready |= 4;",
                "  } else if (entry.type === 'udpSocket') {",
                "    if ((interestOps & 1) !== 0 && entry.packetQueue.length > 0) ready |= 1;",
                "    if ((interestOps & 4) !== 0) ready |= 4;",
                "  }",
                "  return ready;",
                "}",
                "port.on('message', (msg) => {",
                "  const reqId = msg.reqId;",
                "  const op = msg.op;",
                "  try {",
                "    if (op === 'dnsLookupAll') {",
                "      dns.lookup(msg.host, { all: true, verbatim: true }, (err, addresses) => {",
                "        if (err) { sendReply(reqId, fmtErr(err, 'ENOTFOUND')); return; }",
                "        const list = Array.isArray(addresses) ? addresses : (addresses ? [addresses] : []);",
                "        if (list.length === 0) { sendReply(reqId, 'ERR|ENOTFOUND|No addresses'); return; }",
                "        sendReply(reqId, 'OK|' + list.map(a => a.address).join('|'));",
                "      });",
                "      return;",
                "    }",
                "    if (op === 'tcpBindServer') {",
                "      const id = nextHandle++;",
                "      const server = net.createServer({ pauseOnConnect: false, allowHalfOpen: true });",
                "      const entry = { id, type: 'tcpServer', server, acceptQueue: [], closed: false, error: null, waiters: [] };",
                "      function notifyServer() {",
                "        if (entry.waiters.length > 0) {",
                "          const w = entry.waiters.splice(0);",
                "          for (const fn of w) { try { fn(); } catch (_) {} }",
                "        }",
                "        notifyGlobalWaiters();",
                "      }",
                "      entry.notify = notifyServer;",
                "      handles.set(id, entry);",
                "      server.on('connection', (clientSock) => {",
                "        if (entry.closed) { try { clientSock.destroy(); } catch (_) {} return; }",
                "        const clientEntry = createSocketEntry(clientSock, true);",
                "        entry.acceptQueue.push(clientEntry);",
                "        notifyServer();",
                "      });",
                "      let settled = false;",
                "      server.on('error', (err) => {",
                "        if (!settled) {",
                "          settled = true;",
                "          handles.delete(id);",
                "          try { server.close(); } catch (_) {}",
                "          sendReply(reqId, fmtErr(err, 'EADDRINUSE'));",
                "        } else {",
                "          entry.error = err;",
                "          notifyServer();",
                "        }",
                "      });",
                "      server.on('close', () => { entry.closed = true; notifyServer(); });",
                "      const bindHost = (!msg.host || msg.host === '0.0.0.0' || msg.host === '::' || msg.host === '0:0:0:0:0:0:0:0') ? '0.0.0.0' : msg.host;",
                "      server.listen({ port: msg.port, host: bindHost, backlog: msg.backlog > 0 ? msg.backlog : 50 }, () => {",
                "        if (settled) return;",
                "        settled = true;",
                "        const addr = server.address();",
                "        const boundPort = (addr && typeof addr === 'object') ? addr.port : msg.port;",
                "        const boundHost = (addr && typeof addr === 'object' && addr.address) ? addr.address : bindHost;",
                "        sendReply(reqId, 'OK|' + id + '|' + boundPort + '|' + boundHost);",
                "      });",
                "      return;",
                "    }",
                "    if (op === 'tcpAccept') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpServer' || entry.closed) {",
                "        sendReply(reqId, 'ERR|EBADF|ServerSocket is closed');",
                "        return;",
                "      }",
                "      function popClient() {",
                "        if (entry.acceptQueue.length > 0) {",
                "          const c = entry.acceptQueue.shift();",
                "          const s = c.sock;",
                "          return 'OK|' + c.id + '|' + (s.localAddress || '127.0.0.1') + '|' + (s.localPort || 0) + '|' + (s.remoteAddress || '127.0.0.1') + '|' + (s.remotePort || 0);",
                "        }",
                "        return null;",
                "      }",
                "      const imm = popClient();",
                "      if (imm !== null) { sendReply(reqId, imm); return; }",
                "      if (msg.timeoutMs < 0) { sendReply(reqId, 'TIMEOUT'); return; }",
                "      let done = false;",
                "      let timer = null;",
                "      const check = () => {",
                "        if (done) return;",
                "        const res = popClient();",
                "        if (res !== null) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, res); return; }",
                "        if (entry.closed) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, 'ERR|EBADF|ServerSocket closed'); return; }",
                "        if (entry.error) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, fmtErr(entry.error, 'EIO')); return; }",
                "        entry.waiters.push(check);",
                "      };",
                "      if (msg.timeoutMs > 0) {",
                "        timer = setTimeout(() => {",
                "          if (done) return;",
                "          done = true;",
                "          const idx = entry.waiters.indexOf(check);",
                "          if (idx >= 0) entry.waiters.splice(idx, 1);",
                "          sendReply(reqId, 'TIMEOUT');",
                "        }, msg.timeoutMs);",
                "      }",
                "      entry.waiters.push(check);",
                "      return;",
                "    }",
                "    if (op === 'tcpConnect') {",
                "      const sock = new net.Socket({ allowHalfOpen: true });",
                "      const entry = createSocketEntry(sock, false);",
                "      let done = false;",
                "      let timer = null;",
                "      const connectOpts = {",
                "        host: (!msg.host || msg.host === '0.0.0.0' || msg.host === '::') ? '127.0.0.1' : msg.host,",
                "        port: msg.port",
                "      };",
                "      if (msg.localHost && msg.localHost !== '0.0.0.0' && msg.localHost !== '::' && msg.localHost !== '0:0:0:0:0:0:0:0') {",
                "        connectOpts.localAddress = msg.localHost;",
                "      }",
                "      if (msg.localPort > 0) connectOpts.localPort = msg.localPort;",
                "      const finish = () => {",
                "        if (done) return;",
                "        if (entry.connected) {",
                "          done = true;",
                "          if (timer) clearTimeout(timer);",
                "          const lAddr = sock.localAddress || '127.0.0.1';",
                "          const lPort = sock.localPort || 0;",
                "          const rAddr = sock.remoteAddress || connectOpts.host;",
                "          const rPort = sock.remotePort || msg.port;",
                "          setImmediate(() => {",
                "            sendReply(reqId, 'OK|' + entry.id + '|' + lAddr + '|' + lPort + '|' + rAddr + '|' + rPort);",
                "          });",
                "          return;",
                "        }",
                "        if (entry.connectError || entry.error || entry.closed) {",
                "          done = true;",
                "          if (timer) clearTimeout(timer);",
                "          const err = entry.connectError || entry.error || new Error('Connection refused');",
                "          handles.delete(entry.id);",
                "          try { sock.destroy(); } catch (_) {}",
                "          sendReply(reqId, fmtErr(err, 'ECONNREFUSED'));",
                "          return;",
                "        }",
                "        entry.waiters.push(finish);",
                "      };",
                "      sock.once('connect', () => { entry.connected = true; entry.notify(); });",
                "      if (msg.timeoutMs > 0) {",
                "        timer = setTimeout(() => {",
                "          if (done) return;",
                "          done = true;",
                "          handles.delete(entry.id);",
                "          try { sock.destroy(); } catch (_) {}",
                "          sendReply(reqId, 'TIMEOUT');",
                "        }, msg.timeoutMs);",
                "      }",
                "      entry.waiters.push(finish);",
                "      sock.connect(connectOpts);",
                "      return;",
                "    }",
                "    if (op === 'tcpRead') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpSocket') { sendReply(reqId, 'ERR|EBADF|Socket is closed'); return; }",
                "      if (entry.inputShutdown) { sendReply(reqId, 'EOF'); return; }",
                "      function consume() {",
                "        const toRead = Math.min(msg.maxBytes, entry.bufferedBytes);",
                "        if (toRead <= 0) return '';",
                "        const out = Buffer.allocUnsafe(toRead);",
                "        let copied = 0;",
                "        while (copied < toRead && entry.chunks.length > 0) {",
                "          const first = entry.chunks[0];",
                "          const take = Math.min(first.length, toRead - copied);",
                "          first.copy(out, copied, 0, take);",
                "          copied += take;",
                "          if (take === first.length) entry.chunks.shift();",
                "          else entry.chunks[0] = first.subarray(take);",
                "        }",
                "        entry.bufferedBytes -= copied;",
                "        return out.subarray(0, copied).toString('latin1');",
                "      }",
                "      if (entry.bufferedBytes > 0) { sendReply(reqId, 'OK|' + consume()); return; }",
                "      if (entry.eof || entry.closed) { sendReply(reqId, 'EOF'); return; }",
                "      if (entry.error) { sendReply(reqId, fmtErr(entry.error, 'EIO')); return; }",
                "      if (msg.timeoutMs < 0) { sendReply(reqId, 'TIMEOUT'); return; }",
                "      let done = false;",
                "      let timer = null;",
                "      const check = () => {",
                "        if (done) return;",
                "        if (entry.bufferedBytes > 0) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, 'OK|' + consume()); return; }",
                "        if (entry.inputShutdown || entry.eof || entry.closed) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, 'EOF'); return; }",
                "        if (entry.error) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, fmtErr(entry.error, 'EIO')); return; }",
                "        entry.waiters.push(check);",
                "      };",
                "      if (msg.timeoutMs > 0) {",
                "        timer = setTimeout(() => {",
                "          if (done) return;",
                "          done = true;",
                "          const idx = entry.waiters.indexOf(check);",
                "          if (idx >= 0) entry.waiters.splice(idx, 1);",
                "          sendReply(reqId, 'TIMEOUT');",
                "        }, msg.timeoutMs);",
                "      }",
                "      entry.waiters.push(check);",
                "      return;",
                "    }",
                "    if (op === 'tcpWrite') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpSocket' || entry.closed || entry.outputShutdown || entry.error) {",
                "        sendReply(reqId, -1);",
                "        return;",
                "      }",
                "      const buf = Buffer.from(msg.latin1, 'latin1');",
                "      if (buf.length === 0) { sendReply(reqId, 0); return; }",
                "      let done = false;",
                "      const finish = (res) => {",
                "        if (done) return;",
                "        done = true;",
                "        const idx = entry.waiters.indexOf(onStateChange);",
                "        if (idx >= 0) entry.waiters.splice(idx, 1);",
                "        sendReply(reqId, res);",
                "      };",
                "      const onStateChange = () => {",
                "        if (entry.error || entry.closed) finish(-1);",
                "      };",
                "      entry.waiters.push(onStateChange);",
                "      try {",
                "        entry.sock.write(buf, (err) => {",
                "          if (err) { entry.error = err; finish(-1); }",
                "          else { setImmediate(() => finish(buf.length)); }",
                "        });",
                "      } catch (e) {",
                "        entry.error = e;",
                "        finish(-1);",
                "      }",
                "      return;",
                "    }",
                "    if (op === 'tcpAvailable') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpSocket' || entry.closed || entry.inputShutdown) { sendReply(reqId, 0); return; }",
                "      if (entry.bufferedBytes > 0 || entry.eof) { sendReply(reqId, entry.bufferedBytes); return; }",
                "      setTimeout(() => { sendReply(reqId, entry.bufferedBytes); }, 1);",
                "      return;",
                "    }",
                "    if (op === 'tcpShutdown') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpSocket' || entry.closed) { sendReply(reqId, false); return; }",
                "      if (msg.how === 0) {",
                "        entry.inputShutdown = true;",
                "        entry.chunks = [];",
                "        entry.bufferedBytes = 0;",
                "        entry.notify();",
                "        sendReply(reqId, true);",
                "      } else {",
                "        entry.outputShutdown = true;",
                "        let done = false;",
                "        const finish = () => { if (!done) { done = true; setTimeout(() => sendReply(reqId, true), 1); } };",
                "        setTimeout(finish, 30);",
                "        entry.sock.end(finish);",
                "      }",
                "      return;",
                "    }",
                "    if (op === 'tcpSetOption') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'tcpSocket' || entry.closed) { sendReply(reqId, false); return; }",
                "      try {",
                "        if (msg.option === 'TCP_NODELAY') entry.sock.setNoDelay(!!msg.boolVal);",
                "        else if (msg.option === 'SO_KEEPALIVE') entry.sock.setKeepAlive(!!msg.boolVal);",
                "        sendReply(reqId, true);",
                "      } catch (_) { sendReply(reqId, false); }",
                "      return;",
                "    }",
                "    if (op === 'tcpClose') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry) { sendReply(reqId, true); return; }",
                "      handles.delete(msg.handle);",
                "      entry.closed = true;",
                "      entry.eof = true;",
                "      if (entry.notify) entry.notify();",
                "      if (entry.type === 'tcpServer') {",
                "        while (entry.acceptQueue.length > 0) {",
                "          const c = entry.acceptQueue.shift();",
                "          handles.delete(c.id);",
                "          try { c.sock.destroy(); } catch (_) {}",
                "        }",
                "        let done = false;",
                "        const finish = () => { if (!done) { done = true; sendReply(reqId, true); } };",
                "        setTimeout(finish, 50);",
                "        try { entry.server.close(finish); } catch (_) { finish(); }",
                "      } else {",
                "        let done = false;",
                "        const finish = () => {",
                "          if (done) return;",
                "          done = true;",
                "          try { entry.sock.destroy(); } catch (_) {}",
                "          sendReply(reqId, true);",
                "        };",
                "        setTimeout(finish, 20);",
                "        try {",
                "          entry.sock.once('close', finish);",
                "          entry.sock.end(() => {",
                "            try { entry.sock.destroy(); } catch (_) {}",
                "            finish();",
                "          });",
                "        } catch (_) { finish(); }",
                "      }",
                "      return;",
                "    }",
                "    if (op === 'udpBind') {",
                "      const id = nextHandle++;",
                "      const isIPv6 = !!(msg.host && msg.host.includes(':') && msg.host !== '::' && msg.host !== '0:0:0:0:0:0:0:0');",
                "      const sock = dgram.createSocket({ type: isIPv6 ? 'udp6' : 'udp4', reuseAddr: !!msg.reuseAddress });",
                "      const entry = { id, type: 'udpSocket', sock, packetQueue: [], closed: false, error: null, waiters: [] };",
                "      function notifyUdp() {",
                "        if (entry.waiters.length > 0) {",
                "          const w = entry.waiters.splice(0);",
                "          for (const fn of w) { try { fn(); } catch (_) {} }",
                "        }",
                "        notifyGlobalWaiters();",
                "      }",
                "      entry.notify = notifyUdp;",
                "      handles.set(id, entry);",
                "      sock.on('message', (data, rinfo) => {",
                "        if (entry.closed) return;",
                "        entry.packetQueue.push({ data: Buffer.from(data), address: rinfo.address, port: rinfo.port });",
                "        notifyUdp();",
                "      });",
                "      let settled = false;",
                "      sock.on('error', (err) => {",
                "        if (!settled) {",
                "          settled = true;",
                "          handles.delete(id);",
                "          try { sock.close(); } catch (_) {}",
                "          sendReply(reqId, fmtErr(err, 'EADDRINUSE'));",
                "        } else {",
                "          entry.error = err;",
                "          notifyUdp();",
                "        }",
                "      });",
                "      sock.on('close', () => { entry.closed = true; notifyUdp(); });",
                "      const bindHost = (!msg.host || msg.host === '0.0.0.0' || msg.host === '::' || msg.host === '0:0:0:0:0:0:0:0') ? (isIPv6 ? '::' : '0.0.0.0') : msg.host;",
                "      sock.bind({ port: msg.port, address: bindHost, exclusive: !msg.reuseAddress }, () => {",
                "        if (settled) return;",
                "        settled = true;",
                "        const addr = sock.address();",
                "        const boundPort = (addr && typeof addr === 'object') ? addr.port : msg.port;",
                "        const boundHost = (addr && typeof addr === 'object' && addr.address) ? addr.address : bindHost;",
                "        sendReply(reqId, 'OK|' + id + '|' + boundPort + '|' + boundHost);",
                "      });",
                "      return;",
                "    }",
                "    if (op === 'udpSend') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'udpSocket' || entry.closed) { sendReply(reqId, -1); return; }",
                "      const buf = Buffer.from(msg.latin1, 'latin1');",
                "      const targetHost = (!msg.host || msg.host === '0.0.0.0' || msg.host === '::') ? '127.0.0.1' : msg.host;",
                "      try {",
                "        entry.sock.send(buf, 0, buf.length, msg.port, targetHost, (err) => {",
                "          if (err) { entry.error = err; sendReply(reqId, -1); }",
                "          else { setImmediate(() => sendReply(reqId, buf.length)); }",
                "        });",
                "      } catch (e) {",
                "        entry.error = e;",
                "        sendReply(reqId, -1);",
                "      }",
                "      return;",
                "    }",
                "    if (op === 'udpReceive') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'udpSocket' || entry.closed) { sendReply(reqId, 'ERR|EBADF|Socket is closed'); return; }",
                "      function popPkt() {",
                "        if (entry.packetQueue.length > 0) {",
                "          const pkt = entry.packetQueue.shift();",
                "          const slice = pkt.data.length > msg.maxBytes ? pkt.data.subarray(0, msg.maxBytes) : pkt.data;",
                "          return 'OK|' + pkt.address + '|' + pkt.port + '|' + slice.toString('latin1');",
                "        }",
                "        return null;",
                "      }",
                "      const imm = popPkt();",
                "      if (imm !== null) { sendReply(reqId, imm); return; }",
                "      if (msg.timeoutMs < 0) { sendReply(reqId, 'TIMEOUT'); return; }",
                "      let done = false;",
                "      let timer = null;",
                "      const check = () => {",
                "        if (done) return;",
                "        const res = popPkt();",
                "        if (res !== null) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, res); return; }",
                "        if (entry.closed) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, 'ERR|EBADF|Socket closed'); return; }",
                "        if (entry.error) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, fmtErr(entry.error, 'EIO')); return; }",
                "        entry.waiters.push(check);",
                "      };",
                "      if (msg.timeoutMs > 0) {",
                "        timer = setTimeout(() => {",
                "          if (done) return;",
                "          done = true;",
                "          const idx = entry.waiters.indexOf(check);",
                "          if (idx >= 0) entry.waiters.splice(idx, 1);",
                "          sendReply(reqId, 'TIMEOUT');",
                "        }, msg.timeoutMs);",
                "      }",
                "      entry.waiters.push(check);",
                "      return;",
                "    }",
                "    if (op === 'udpSetBroadcast') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry || entry.type !== 'udpSocket' || entry.closed) { sendReply(reqId, false); return; }",
                "      try { entry.sock.setBroadcast(!!msg.on); sendReply(reqId, true); } catch (_) { sendReply(reqId, false); }",
                "      return;",
                "    }",
                "    if (op === 'udpClose') {",
                "      const entry = handles.get(msg.handle);",
                "      if (!entry) { sendReply(reqId, true); return; }",
                "      handles.delete(msg.handle);",
                "      entry.closed = true;",
                "      if (entry.notify) entry.notify();",
                "      let done = false;",
                "      const finish = () => { if (!done) { done = true; sendReply(reqId, true); } };",
                "      setTimeout(finish, 30);",
                "      try { entry.sock.close(finish); } catch (_) { finish(); }",
                "      return;",
                "    }",
                "    if (op === 'pollHandle') {",
                "      const r0 = checkReady(msg.handle, msg.interestOps);",
                "      if (r0 !== 0) { sendReply(reqId, r0); return; }",
                "      setTimeout(() => { sendReply(reqId, checkReady(msg.handle, msg.interestOps)); }, 1);",
                "      return;",
                "    }",
                "    if (op === 'selectorWait') {",
                "      const pairs = [];",
                "      if (msg.packedEntries && msg.packedEntries.length > 0) {",
                "        for (const item of msg.packedEntries.split(',')) {",
                "          const parts = item.split(':');",
                "          if (parts.length === 2) pairs.push([ Number(parts[0]), Number(parts[1]) ]);",
                "        }",
                "      }",
                "      function anyReady() {",
                "        for (const [h, ops] of pairs) {",
                "          if (checkReady(h, ops) !== 0) return true;",
                "        }",
                "        return false;",
                "      }",
                "      if (anyReady()) { sendReply(reqId, true); return; }",
                "      if (msg.timeoutMs <= 0) {",
                "        setTimeout(() => { sendReply(reqId, anyReady()); }, 1);",
                "        return;",
                "      }",
                "      let done = false;",
                "      let timer = null;",
                "      const check = () => {",
                "        if (done) return;",
                "        if (anyReady()) { done = true; if (timer) clearTimeout(timer); sendReply(reqId, true); return; }",
                "        globalWaiters.push(check);",
                "      };",
                "      timer = setTimeout(() => {",
                "        if (done) return;",
                "        done = true;",
                "        const idx = globalWaiters.indexOf(check);",
                "        if (idx >= 0) globalWaiters.splice(idx, 1);",
                "        sendReply(reqId, anyReady());",
                "      }, msg.timeoutMs);",
                "      globalWaiters.push(check);",
                "      return;",
                "    }",
                "    sendReply(reqId, 'ERR|ENOSYS|Unknown op: ' + op);",
                "  } catch (e) {",
                "    if (op === 'tcpWrite' || op === 'udpSend') sendReply(reqId, -1);",
                "    else if (op === 'tcpAvailable' || op === 'pollHandle') sendReply(reqId, 0);",
                "    else if (op === 'tcpShutdown' || op === 'tcpSetOption' || op === 'tcpClose' || op === 'udpSetBroadcast' || op === 'udpClose' || op === 'selectorWait') sendReply(reqId, false);",
                "    else sendReply(reqId, fmtErr(e, 'EIO'));",
                "  }",
                "});"
            ];

            function ensureNetWorker() {
                if (netWorker) return;
                const netCtrlSab = new SharedArrayBuffer(16);
                netCtrlArr = new Int32Array(netCtrlSab);
                const channel = new worker_threads.MessageChannel();
                netPort = channel.port1;
                if (typeof netPort.unref === 'function') netPort.unref();
                netWorker = new worker_threads.Worker(workerLines.join('\n'), {
                    eval: true,
                    workerData: { ctrlSab: netCtrlSab, port: channel.port2 },
                    transferList: [channel.port2]
                });
                if (typeof netWorker.unref === 'function') netWorker.unref();
            }

            function netCall(op, args) {
                ensureNetWorker();
                const reqId = ++netReqSeq;
                while (worker_threads.receiveMessageOnPort(netPort)) {}
                Atomics.store(netCtrlArr, 1, reqId);
                Atomics.store(netCtrlArr, 0, 0);
                netPort.postMessage(Object.assign({ reqId: reqId, op: op }, args));
                while (true) {
                    while (Atomics.load(netCtrlArr, 0) === 0) {
                        Atomics.wait(netCtrlArr, 0, 0, 100);
                    }
                    let pkt;
                    while ((pkt = worker_threads.receiveMessageOnPort(netPort)) !== undefined) {
                        if (pkt.message && pkt.message.reqId === reqId) {
                            return pkt.message.result;
                        }
                    }
                }
            }

            const b = globalThis.__jakoNodeBridge;
            b.dnsLookupAll = (host) => netCall('dnsLookupAll', { host });
            b.tcpBindServer = (host, port, backlog, reuseAddress) => netCall('tcpBindServer', { host, port, backlog, reuseAddress });
            b.tcpAccept = (handle, timeoutMs) => netCall('tcpAccept', { handle, timeoutMs });
            b.tcpConnect = (host, port, timeoutMs, localHost, localPort) => netCall('tcpConnect', { host, port, timeoutMs, localHost, localPort });
            b.tcpRead = (handle, maxBytes, timeoutMs) => netCall('tcpRead', { handle, maxBytes, timeoutMs });
            b.tcpWrite = (handle, latin1) => netCall('tcpWrite', { handle, latin1 });
            b.tcpAvailable = (handle) => netCall('tcpAvailable', { handle });
            b.tcpShutdown = (handle, how) => netCall('tcpShutdown', { handle, how });
            b.tcpSetOption = (handle, option, intVal, boolVal) => netCall('tcpSetOption', { handle, option, intVal, boolVal });
            b.tcpClose = (handle) => netCall('tcpClose', { handle });
            b.udpBind = (host, port, reuseAddress) => netCall('udpBind', { host, port, reuseAddress });
            b.udpSend = (handle, latin1, host, port) => netCall('udpSend', { handle, latin1, host, port });
            b.udpReceive = (handle, maxBytes, timeoutMs) => netCall('udpReceive', { handle, maxBytes, timeoutMs });
            b.udpSetBroadcast = (handle, on) => netCall('udpSetBroadcast', { handle, on });
            b.udpClose = (handle) => netCall('udpClose', { handle });
            b.pollHandle = (handle, interestOps) => netCall('pollHandle', { handle, interestOps });
            b.selectorWait = (packedEntries, timeoutMs) => netCall('selectorWait', { packedEntries, timeoutMs });
            }
        }
    }"""
)
private external fun jsInitBridge()

@JsFun("() => globalThis.__jakoNodeBridge.isNodeJs()")
private external fun jsIsNodeJs(): Boolean

@JsFun("() => globalThis.__jakoNodeBridge.currentTimeMillis()")
private external fun jsCurrentTimeMillis(): Double

@JsFun("() => globalThis.__jakoNodeBridge.nanoTime()")
private external fun jsNanoTime(): Double

@JsFun("(ms) => globalThis.__jakoNodeBridge.sleepMillis(ms)")
private external fun jsSleepMillis(ms: Int)

@JsFun("() => globalThis.__jakoNodeBridge.pid()")
private external fun jsPid(): Int

@JsFun("() => globalThis.__jakoNodeBridge.uid()")
private external fun jsUid(): Int

@JsFun("() => globalThis.__jakoNodeBridge.availableProcessors()")
private external fun jsAvailableProcessors(): Int

@JsFun("() => globalThis.__jakoNodeBridge.osName()")
private external fun jsOsName(): String

@JsFun("() => globalThis.__jakoNodeBridge.osArch()")
private external fun jsOsArch(): String

@JsFun("() => globalThis.__jakoNodeBridge.osVersion()")
private external fun jsOsVersion(): String

@JsFun("() => globalThis.__jakoNodeBridge.fileSeparator()")
private external fun jsFileSeparator(): String

@JsFun("() => globalThis.__jakoNodeBridge.pathSeparator()")
private external fun jsPathSeparator(): String

@JsFun("() => globalThis.__jakoNodeBridge.lineSeparator()")
private external fun jsLineSeparator(): String

@JsFun("() => globalThis.__jakoNodeBridge.userDir()")
private external fun jsUserDir(): String

@JsFun("() => globalThis.__jakoNodeBridge.userHome()")
private external fun jsUserHome(): String

@JsFun("() => globalThis.__jakoNodeBridge.userName()")
private external fun jsUserName(): String

@JsFun("() => globalThis.__jakoNodeBridge.tmpDir()")
private external fun jsTmpDir(): String

@JsFun("() => globalThis.__jakoNodeBridge.hostname()")
private external fun jsHostname(): String

@JsFun("(name) => globalThis.__jakoNodeBridge.getenv(name)")
private external fun jsGetenv(name: String): String?

@JsFun("() => globalThis.__jakoNodeBridge.getenvAll()")
private external fun jsGetenvAll(): String

@JsFun("(latin1) => globalThis.__jakoNodeBridge.writeStdout(latin1)")
private external fun jsWriteStdout(latin1: String)

@JsFun("(latin1) => globalThis.__jakoNodeBridge.writeStderr(latin1)")
private external fun jsWriteStderr(latin1: String)

@JsFun("(status) => globalThis.__jakoNodeBridge.exit(status)")
private external fun jsExit(status: Int)

@JsFun("(len) => globalThis.__jakoNodeBridge.randomBytesLatin1(len)")
private external fun jsRandomBytesLatin1(len: Int): String

@JsFun("(p, follow) => globalThis.__jakoNodeBridge.stat(p, follow)")
private external fun jsStat(p: String, follow: Boolean): String

@JsFun("(p, mode) => globalThis.__jakoNodeBridge.access(p, mode)")
private external fun jsAccess(p: String, mode: Int): Boolean

@JsFun("(p, mode) => globalThis.__jakoNodeBridge.chmod(p, mode)")
private external fun jsChmod(p: String, mode: Int): Boolean

@JsFun("(p, atimeMs, mtimeMs) => globalThis.__jakoNodeBridge.utimes(p, atimeMs, mtimeMs)")
private external fun jsUtimes(p: String, atimeMs: Double, mtimeMs: Double): Boolean

@JsFun("(p) => globalThis.__jakoNodeBridge.mkdir(p)")
private external fun jsMkdir(p: String): Boolean

@JsFun("(p) => globalThis.__jakoNodeBridge.mkdirOrError(p)")
private external fun jsMkdirOrError(p: String): String

@JsFun("(p) => globalThis.__jakoNodeBridge.readdir(p)")
private external fun jsReaddir(p: String): String

@JsFun("(p) => globalThis.__jakoNodeBridge.unlinkOrRmdir(p)")
private external fun jsUnlinkOrRmdir(p: String): Boolean

@JsFun("(p) => globalThis.__jakoNodeBridge.deleteOrError(p)")
private external fun jsDeleteOrError(p: String): String

@JsFun("(src, dst) => globalThis.__jakoNodeBridge.rename(src, dst)")
private external fun jsRename(src: String, dst: String): Boolean

@JsFun("(src, dst) => globalThis.__jakoNodeBridge.renameOrError(src, dst)")
private external fun jsRenameOrError(src: String, dst: String): String

@JsFun("(src, dst, overwrite) => globalThis.__jakoNodeBridge.copyFileOrError(src, dst, overwrite)")
private external fun jsCopyFileOrError(src: String, dst: String, overwrite: Boolean): String

@JsFun("(target, link) => globalThis.__jakoNodeBridge.symlinkOrError(target, link)")
private external fun jsSymlinkOrError(target: String, link: String): String

@JsFun("(p) => globalThis.__jakoNodeBridge.readlinkOrError(p)")
private external fun jsReadlinkOrError(p: String): String

@JsFun("(p) => globalThis.__jakoNodeBridge.realpath(p)")
private external fun jsRealpath(p: String): String

@JsFun("(p, kind) => globalThis.__jakoNodeBridge.diskSpace(p, kind)")
private external fun jsDiskSpace(p: String, kind: Int): Double

@JsFun("(p, mode) => globalThis.__jakoNodeBridge.open(p, mode)")
private external fun jsOpen(p: String, mode: String): Int

@JsFun("() => globalThis.__jakoNodeBridge.lastError()")
private external fun jsLastError(): String

@JsFun("(fd) => globalThis.__jakoNodeBridge.close(fd)")
private external fun jsClose(fd: Int): Int

@JsFun("(fd, len, pos) => globalThis.__jakoNodeBridge.read(fd, len, pos)")
private external fun jsRead(fd: Int, len: Int, pos: Double): String

@JsFun("(fd, latin1, pos) => globalThis.__jakoNodeBridge.write(fd, latin1, pos)")
private external fun jsWrite(fd: Int, latin1: String, pos: Double): Int

@JsFun("(fd, offset, whence) => globalThis.__jakoNodeBridge.lseek(fd, offset, whence)")
private external fun jsLseek(fd: Int, offset: Double, whence: Int): Double

@JsFun("(fd) => globalThis.__jakoNodeBridge.fstatSize(fd)")
private external fun jsFstatSize(fd: Int): Double

@JsFun("(fd, len) => globalThis.__jakoNodeBridge.ftruncate(fd, len)")
private external fun jsFtruncate(fd: Int, len: Double): Boolean

@JsFun("(fd, meta) => globalThis.__jakoNodeBridge.fsync(fd, meta)")
private external fun jsFsync(fd: Int, meta: Boolean): Boolean

@JsFun("(cmd, cwd, pathEnv) => globalThis.__jakoNodeBridge.canResolveExecutable(cmd, cwd, pathEnv)")
private external fun jsCanResolveExecutable(cmd: String, cwd: String, pathEnv: String): Boolean

@JsFun("(cmd, argsJoined, cwd, envJoined, stdinLatin1, redirectErrorStream, timeoutMs) => globalThis.__jakoNodeBridge.spawnSync(cmd, argsJoined, cwd, envJoined, stdinLatin1, redirectErrorStream, timeoutMs)")
private external fun jsSpawnSync(
    cmd: String,
    argsJoined: String,
    cwd: String,
    envJoined: String,
    stdinLatin1: String,
    redirectErrorStream: Boolean,
    timeoutMs: Int
): String

@JsFun("(host) => globalThis.__jakoNodeBridge.dnsLookupAll(host)")
private external fun jsDnsLookupAll(host: String): String

@JsFun("(host, port, backlog, reuseAddress) => globalThis.__jakoNodeBridge.tcpBindServer(host, port, backlog, reuseAddress)")
private external fun jsTcpBindServer(host: String, port: Int, backlog: Int, reuseAddress: Boolean): String

@JsFun("(serverHandle, timeoutMs) => globalThis.__jakoNodeBridge.tcpAccept(serverHandle, timeoutMs)")
private external fun jsTcpAccept(serverHandle: Int, timeoutMs: Int): String

@JsFun("(host, port, timeoutMs, localHost, localPort) => globalThis.__jakoNodeBridge.tcpConnect(host, port, timeoutMs, localHost, localPort)")
private external fun jsTcpConnect(host: String, port: Int, timeoutMs: Int, localHost: String, localPort: Int): String

@JsFun("(handle, maxBytes, timeoutMs) => globalThis.__jakoNodeBridge.tcpRead(handle, maxBytes, timeoutMs)")
private external fun jsTcpRead(handle: Int, maxBytes: Int, timeoutMs: Int): String

@JsFun("(handle, latin1) => globalThis.__jakoNodeBridge.tcpWrite(handle, latin1)")
private external fun jsTcpWrite(handle: Int, latin1: String): Int

@JsFun("(handle) => globalThis.__jakoNodeBridge.tcpAvailable(handle)")
private external fun jsTcpAvailable(handle: Int): Int

@JsFun("(handle, how) => globalThis.__jakoNodeBridge.tcpShutdown(handle, how)")
private external fun jsTcpShutdown(handle: Int, how: Int): Boolean

@JsFun("(handle, option, intVal, boolVal) => globalThis.__jakoNodeBridge.tcpSetOption(handle, option, intVal, boolVal)")
private external fun jsTcpSetOption(handle: Int, option: String, intVal: Int, boolVal: Boolean): Boolean

@JsFun("(handle) => globalThis.__jakoNodeBridge.tcpClose(handle)")
private external fun jsTcpClose(handle: Int): Boolean

@JsFun("(host, port, reuseAddress) => globalThis.__jakoNodeBridge.udpBind(host, port, reuseAddress)")
private external fun jsUdpBind(host: String, port: Int, reuseAddress: Boolean): String

@JsFun("(handle, latin1, host, port) => globalThis.__jakoNodeBridge.udpSend(handle, latin1, host, port)")
private external fun jsUdpSend(handle: Int, latin1: String, host: String, port: Int): Int

@JsFun("(handle, maxBytes, timeoutMs) => globalThis.__jakoNodeBridge.udpReceive(handle, maxBytes, timeoutMs)")
private external fun jsUdpReceive(handle: Int, maxBytes: Int, timeoutMs: Int): String

@JsFun("(handle, on) => globalThis.__jakoNodeBridge.udpSetBroadcast(handle, on)")
private external fun jsUdpSetBroadcast(handle: Int, on: Boolean): Boolean

@JsFun("(handle) => globalThis.__jakoNodeBridge.udpClose(handle)")
private external fun jsUdpClose(handle: Int): Boolean

@JsFun("(handle, interestOps) => globalThis.__jakoNodeBridge.pollHandle(handle, interestOps)")
private external fun jsPollHandle(handle: Int, interestOps: Int): Int

@JsFun("(packedEntries, timeoutMs) => globalThis.__jakoNodeBridge.selectorWait(packedEntries, timeoutMs)")
private external fun jsSelectorWait(packedEntries: String, timeoutMs: Int): Boolean

internal actual object NodeBridge {
    init {
        jsInitBridge()
    }

    actual fun hostRuntime(): WebRuntime = if (jsIsNodeJs()) WebRuntime.NODE_JS else WebRuntime.BROWSER

    actual fun currentTimeMillis(): Double = jsCurrentTimeMillis()
    actual fun nanoTime(): Double = jsNanoTime()
    actual fun sleepMillis(ms: Int) = jsSleepMillis(ms)

    actual fun pid(): Int = jsPid()
    actual fun uid(): Int = jsUid()
    actual fun availableProcessors(): Int = jsAvailableProcessors()
    actual fun osName(): String = jsOsName()
    actual fun osArch(): String = jsOsArch()
    actual fun osVersion(): String = jsOsVersion()
    actual fun fileSeparator(): String = jsFileSeparator()
    actual fun pathSeparator(): String = jsPathSeparator()
    actual fun lineSeparator(): String = jsLineSeparator()
    actual fun userDir(): String = jsUserDir()
    actual fun userHome(): String = jsUserHome()
    actual fun userName(): String = jsUserName()
    actual fun tmpDir(): String = jsTmpDir()
    actual fun hostname(): String = jsHostname()

    actual fun getenv(name: String): String? = jsGetenv(name)
    actual fun getenvAll(): String = jsGetenvAll()
    actual fun writeStdout(latin1: String) = jsWriteStdout(latin1)
    actual fun writeStderr(latin1: String) = jsWriteStderr(latin1)
    actual fun exit(status: Int) = jsExit(status)

    actual fun randomBytesLatin1(length: Int): String = jsRandomBytesLatin1(length)

    actual fun stat(path: String, followSymlinks: Boolean): String = jsStat(path, followSymlinks)
    actual fun access(path: String, mode: Int): Boolean = jsAccess(path, mode)
    actual fun chmod(path: String, mode: Int): Boolean = jsChmod(path, mode)
    actual fun utimes(path: String, atimeMs: Double, mtimeMs: Double): Boolean = jsUtimes(path, atimeMs, mtimeMs)
    actual fun mkdir(path: String): Boolean = jsMkdir(path)
    actual fun mkdirOrError(path: String): String = jsMkdirOrError(path)
    actual fun readdir(path: String): String = jsReaddir(path)
    actual fun unlinkOrRmdir(path: String): Boolean = jsUnlinkOrRmdir(path)
    actual fun deleteOrError(path: String): String = jsDeleteOrError(path)
    actual fun rename(src: String, dst: String): Boolean = jsRename(src, dst)
    actual fun renameOrError(src: String, dst: String): String = jsRenameOrError(src, dst)
    actual fun copyFileOrError(src: String, dst: String, overwrite: Boolean): String = jsCopyFileOrError(src, dst, overwrite)
    actual fun symlinkOrError(target: String, link: String): String = jsSymlinkOrError(target, link)
    actual fun readlinkOrError(path: String): String = jsReadlinkOrError(path)
    actual fun realpath(path: String): String = jsRealpath(path)
    actual fun diskSpace(path: String, kind: Int): Double = jsDiskSpace(path, kind)

    actual fun open(path: String, mode: String): Int = jsOpen(path, mode)
    actual fun lastError(): String = jsLastError()
    actual fun close(fd: Int): Int = jsClose(fd)
    actual fun read(fd: Int, length: Int, position: Double): String = jsRead(fd, length, position)
    actual fun write(fd: Int, latin1: String, position: Double): Int = jsWrite(fd, latin1, position)
    actual fun lseek(fd: Int, offset: Double, whence: Int): Double = jsLseek(fd, offset, whence)
    actual fun fstatSize(fd: Int): Double = jsFstatSize(fd)
    actual fun ftruncate(fd: Int, length: Double): Boolean = jsFtruncate(fd, length)
    actual fun fsync(fd: Int, metadata: Boolean): Boolean = jsFsync(fd, metadata)

    actual fun canResolveExecutable(cmd: String, cwd: String, pathEnv: String): Boolean =
        jsCanResolveExecutable(cmd, cwd, pathEnv)

    actual fun spawnSync(
        cmd: String,
        argsJoined: String,
        cwd: String,
        envJoined: String,
        stdinLatin1: String,
        redirectErrorStream: Boolean,
        timeoutMs: Int
    ): String = jsSpawnSync(cmd, argsJoined, cwd, envJoined, stdinLatin1, redirectErrorStream, timeoutMs)

    actual fun dnsLookupAll(host: String): String = jsDnsLookupAll(host)
    actual fun tcpBindServer(host: String, port: Int, backlog: Int, reuseAddress: Boolean): String =
        jsTcpBindServer(host, port, backlog, reuseAddress)
    actual fun tcpAccept(serverHandle: Int, timeoutMs: Int): String =
        jsTcpAccept(serverHandle, timeoutMs)
    actual fun tcpConnect(host: String, port: Int, timeoutMs: Int, localHost: String, localPort: Int): String =
        jsTcpConnect(host, port, timeoutMs, localHost, localPort)
    actual fun tcpRead(handle: Int, maxBytes: Int, timeoutMs: Int): String =
        jsTcpRead(handle, maxBytes, timeoutMs)
    actual fun tcpWrite(handle: Int, latin1: String): Int =
        jsTcpWrite(handle, latin1)
    actual fun tcpAvailable(handle: Int): Int =
        jsTcpAvailable(handle)
    actual fun tcpShutdown(handle: Int, how: Int): Boolean =
        jsTcpShutdown(handle, how)
    actual fun tcpSetOption(handle: Int, option: String, intVal: Int, boolVal: Boolean): Boolean =
        jsTcpSetOption(handle, option, intVal, boolVal)
    actual fun tcpClose(handle: Int): Boolean =
        jsTcpClose(handle)

    actual fun udpBind(host: String, port: Int, reuseAddress: Boolean): String =
        jsUdpBind(host, port, reuseAddress)
    actual fun udpSend(handle: Int, latin1: String, host: String, port: Int): Int =
        jsUdpSend(handle, latin1, host, port)
    actual fun udpReceive(handle: Int, maxBytes: Int, timeoutMs: Int): String =
        jsUdpReceive(handle, maxBytes, timeoutMs)
    actual fun udpSetBroadcast(handle: Int, on: Boolean): Boolean =
        jsUdpSetBroadcast(handle, on)
    actual fun udpClose(handle: Int): Boolean =
        jsUdpClose(handle)

    actual fun pollHandle(handle: Int, interestOps: Int): Int =
        jsPollHandle(handle, interestOps)
    actual fun selectorWait(packedEntries: String, timeoutMs: Int): Boolean =
        jsSelectorWait(packedEntries, timeoutMs)
}
