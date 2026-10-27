package java.io

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
