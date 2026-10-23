package java.util.concurrent

import kotlin.concurrent.withLock

actual abstract class RecursiveTask<V> actual constructor() : ForkJoinTask<V>() {
    @Suppress("UNCHECKED_CAST")
    private var result: V = null as V

    protected actual abstract fun compute(): V

    actual final override fun getRawResult(): V = result

    actual final override fun setRawResult(value: V) {
        result = value
    }

    actual final override fun exec(): Boolean {
        result = compute()
        return true
    }
}
