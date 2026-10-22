@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect abstract class RecursiveTask<V>() : ForkJoinTask<V> {
    protected abstract fun compute(): V
    final override fun getRawResult(): V
    final override fun setRawResult(value: V)
    final override fun exec(): Boolean
}
