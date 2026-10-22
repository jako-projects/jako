@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect abstract class CountedCompleter<T> : ForkJoinTask<T> {
    protected constructor(completer: CountedCompleter<*>?, initialPendingCount: Int)
    protected constructor(completer: CountedCompleter<*>?)
    protected constructor()

    abstract fun compute()
    open fun onCompletion(caller: CountedCompleter<*>)
    open fun onExceptionalCompletion(ex: Throwable, caller: CountedCompleter<*>): Boolean
    fun getCompleter(): CountedCompleter<*>?
    fun getPendingCount(): Int
    fun setPendingCount(count: Int)
    fun addToPendingCount(delta: Int)
    fun compareAndSetPendingCount(expected: Int, count: Int): Boolean
    fun decrementPendingCountUnlessZero(): Int
    fun getRoot(): CountedCompleter<*>
    fun tryComplete()
    fun propagateCompletion()
    override fun complete(rawResult: T)
    fun firstComplete(): CountedCompleter<*>?
    fun nextComplete(): CountedCompleter<*>?
    fun quietlyCompleteRoot()
    fun helpComplete(maxTasks: Int)
    final override fun exec(): Boolean
    override fun getRawResult(): T
    override fun setRawResult(t: T)
}
