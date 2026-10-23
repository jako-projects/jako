package java.io

actual class FileDescriptor actual constructor() {
    var fd: Int = -1

    constructor(fd: Int) : this() {
        this.fd = fd
    }

    actual fun valid(): Boolean = fd != -1

    actual fun sync() {
        if (!valid()) {
            throw SyncFailedException("sync failed")
        }
    }

    actual companion object {
        actual val `in`: FileDescriptor = FileDescriptor(0)
        actual val out: FileDescriptor = FileDescriptor(1)
        actual val err: FileDescriptor = FileDescriptor(2)
    }
}

internal expect fun nativeGetPosixMode(path: String): Int

internal expect fun nativeSetPosixMode(path: String, mode: Int): Boolean
