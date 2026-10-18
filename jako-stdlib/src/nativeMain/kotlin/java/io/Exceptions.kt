package java.io

actual open class IOException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class FileNotFoundException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class EOFException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class InterruptedIOException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class SyncFailedException : IOException {
    actual constructor(message: String?) : super(message)
}

actual open class UncheckedIOException : RuntimeException {
    private val ioCause: IOException

    actual constructor(message: String?, cause: IOException) : super(message, cause) {
        this.ioCause = cause
    }

    actual constructor(cause: IOException) : super(cause) {
        this.ioCause = cause
    }

    actual override val cause: IOException?
        get() = ioCause
}

actual open class UnsupportedEncodingException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class UTFDataFormatException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class CharConversionException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual abstract class ObjectStreamException : IOException {
    actual protected constructor(classname: String?) : super(classname)
    actual protected constructor() : super()
}

actual open class InvalidClassException : ObjectStreamException {
    actual var classname: String? = null
    private val reasonMsg: String?

    actual constructor(reason: String?) : super(reason) {
        this.reasonMsg = reason
    }

    actual constructor(cname: String?, reason: String?) : super(reason) {
        this.classname = cname
        this.reasonMsg = reason
    }

    actual override val message: String?
        get() = if (classname != null) "$classname; $reasonMsg" else reasonMsg
}

actual open class NotSerializableException : ObjectStreamException {
    actual constructor(classname: String?) : super(classname)
    actual constructor() : super()
}

actual open class InvalidObjectException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
}

actual open class StreamCorruptedException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
    actual constructor() : super()
}

actual open class NotActiveException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
    actual constructor() : super()
}

actual open class WriteAbortedException : ObjectStreamException {
    actual var detail: Exception?

    actual constructor(s: String?, ex: Exception?) : super(s) {
        this.detail = ex
    }

    actual override val message: String?
        get() = if (detail == null) super.message else "${super.message}; $detail"

    actual override val cause: Throwable?
        get() = detail
}
