package java.io

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
