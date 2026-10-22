package kotlin

public fun Throwable.initCause(cause: Throwable?): Throwable = this

public val Throwable.suppressed: Array<Throwable>
    get() = suppressedExceptions.toTypedArray()

public fun Throwable.getSuppressed(): Array<Throwable> = suppressedExceptions.toTypedArray()
