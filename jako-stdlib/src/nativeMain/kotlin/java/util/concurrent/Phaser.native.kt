package java.util.concurrent

import java.lang.InterruptedException
import java.lang.Thread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class Phaser actual constructor(
    private val parent: Phaser?,
    parties: Int
) {
    private val root: Phaser = parent?.getRoot() ?: this
    private val lock = ReentrantLock()
    private val phaseCond = lock.newCondition()

    private var phaseNum: Int = 0
    private var registered: Int = 0
    private var arrived: Int = 0
    private var terminated: Boolean = false

    actual constructor() : this(null, 0)
    actual constructor(parties: Int) : this(null, parties)
    actual constructor(parent: Phaser?) : this(parent, 0)

    init {
        if (parties < 0 || parties > 0xFFFF) {
            throw IllegalArgumentException("Illegal number of parties: $parties")
        }
        registered = parties
        if (parent != null && parties > 0) {
            parent.register()
        }
    }

    actual open fun register(): Int = bulkRegister(1)

    actual open fun bulkRegister(parties: Int): Int {
        if (parties < 0) throw IllegalArgumentException("parties < 0")
        if (parties == 0) return getPhase()
        return lock.withLock {
            if (terminated) return@withLock phaseNum
            val wasZero = registered == 0
            registered += parties
            if (parent != null && wasZero) {
                parent.register()
            }
            phaseNum
        }
    }

    actual open fun arrive(): Int = doArrive(deregister = false)

    actual open fun arriveAndDeregister(): Int = doArrive(deregister = true)

    private fun doArrive(deregister: Boolean): Int {
        return lock.withLock {
            if (terminated) return@withLock phaseNum
            val unarrived = registered - arrived
            if (unarrived <= 0) {
                throw IllegalStateException("Attempted arrival of unregistered party")
            }
            val currentPhase = phaseNum
            arrived++
            if (deregister) {
                registered--
                arrived--
            }
            if (arrived == registered) {
                val nextPhase = (currentPhase + 1) and Int.MAX_VALUE
                val shouldTerminate = onAdvance(currentPhase, registered) || registered == 0
                arrived = 0
                if (shouldTerminate) {
                    terminated = true
                    phaseNum = nextPhase or Int.MIN_VALUE
                    if (parent != null) {
                        parent.arriveAndDeregister()
                    }
                } else {
                    phaseNum = nextPhase
                }
                phaseCond.signalAll()
            }
            currentPhase
        }
    }

    actual open fun arriveAndAwaitAdvance(): Int {
        lock.withLock {
            if (terminated) return phaseNum
            val unarrived = registered - arrived
            if (unarrived <= 0) {
                throw IllegalStateException("Attempted arrival of unregistered party")
            }
            val currentPhase = phaseNum
            arrived++
            if (arrived == registered) {
                val nextPhase = (currentPhase + 1) and Int.MAX_VALUE
                val shouldTerminate = onAdvance(currentPhase, registered) || registered == 0
                arrived = 0
                if (shouldTerminate) {
                    terminated = true
                    phaseNum = nextPhase or Int.MIN_VALUE
                    if (parent != null) {
                        parent.arriveAndDeregister()
                    }
                } else {
                    phaseNum = nextPhase
                }
                phaseCond.signalAll()
                return phaseNum
            }
            while (!terminated && phaseNum == currentPhase) {
                phaseCond.await()
            }
            return phaseNum
        }
    }

    actual open fun awaitAdvance(phase: Int): Int {
        if (phase < 0) return phase
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                phaseCond.await()
            }
            phaseNum
        }
    }

    actual open fun awaitAdvanceInterruptibly(phase: Int): Int {
        if (phase < 0) return phase
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                if (Thread.currentThread().isInterrupted()) {
                    throw InterruptedException()
                }
                phaseCond.await()
            }
            phaseNum
        }
    }

    actual open fun awaitAdvanceInterruptibly(phase: Int, timeout: Long, unit: TimeUnit): Int {
        if (phase < 0) return phase
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (!terminated && phaseNum == phase) {
                if (Thread.currentThread().isInterrupted()) {
                    throw InterruptedException()
                }
                if (nanos <= 0L) {
                    throw TimeoutException()
                }
                nanos = phaseCond.awaitNanos(nanos)
            }
            phaseNum
        }
    }

    actual open fun forceTermination() {
        lock.withLock {
            if (!terminated) {
                terminated = true
                if (phaseNum >= 0) {
                    phaseNum = phaseNum or Int.MIN_VALUE
                }
                phaseCond.signalAll()
            }
        }
    }

    actual open fun getPhase(): Int = lock.withLock { phaseNum }

    actual open fun getRegisteredParties(): Int = lock.withLock { registered }

    actual open fun getArrivedParties(): Int = lock.withLock { if (terminated) 0 else arrived }

    actual open fun getUnarrivedParties(): Int = lock.withLock { if (terminated) 0 else registered - arrived }

    actual open fun getParent(): Phaser? = parent

    actual open fun getRoot(): Phaser = root

    actual open fun isTerminated(): Boolean = lock.withLock { terminated }

    protected actual open fun onAdvance(phase: Int, registeredParties: Int): Boolean =
        registeredParties == 0
}
