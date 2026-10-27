package java.util.concurrent

import kotlin.concurrent.withLock

actual abstract class RecursiveAction actual constructor() : ForkJoinTask<java.lang.Void?>() {
    protected actual abstract fun compute()

    actual final override fun getRawResult(): java.lang.Void? = null

    actual final override fun setRawResult(mustBeNull: java.lang.Void?) {}

    actual final override fun exec(): Boolean {
        compute()
        return true
    }
}
