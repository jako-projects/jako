package java.lang

actual open class ClassNotFoundException : Exception {
    private val ex: Throwable?

    actual constructor() : super(null as Throwable?) {
        this.ex = null
    }

    actual constructor(s: String?) : super(s, null) {
        this.ex = null
    }

    actual constructor(s: String?, ex: Throwable?) : super(s, null) {
        this.ex = ex
    }

    actual open fun getException(): Throwable? = ex

    override val cause: Throwable?
        get() = ex
}
