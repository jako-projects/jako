@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect abstract class RecursiveAction() : ForkJoinTask<java.lang.Void?> {
    protected abstract fun compute()
    final override fun getRawResult(): java.lang.Void?
    final override fun setRawResult(mustBeNull: java.lang.Void?)
    final override fun exec(): Boolean
}
