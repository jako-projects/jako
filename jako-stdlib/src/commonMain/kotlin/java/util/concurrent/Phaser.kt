@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class Phaser {
    constructor()
    constructor(parties: Int)
    constructor(parent: Phaser?)
    constructor(parent: Phaser?, parties: Int)

    open fun register(): Int
    open fun bulkRegister(parties: Int): Int
    open fun arrive(): Int
    open fun arriveAndDeregister(): Int
    open fun arriveAndAwaitAdvance(): Int
    open fun awaitAdvance(phase: Int): Int
    open fun awaitAdvanceInterruptibly(phase: Int): Int
    open fun awaitAdvanceInterruptibly(phase: Int, timeout: Long, unit: TimeUnit): Int
    open fun forceTermination()
    open fun getPhase(): Int
    open fun getRegisteredParties(): Int
    open fun getArrivedParties(): Int
    open fun getUnarrivedParties(): Int
    open fun getParent(): Phaser?
    open fun getRoot(): Phaser
    open fun isTerminated(): Boolean
    protected open fun onAdvance(phase: Int, registeredParties: Int): Boolean
}
