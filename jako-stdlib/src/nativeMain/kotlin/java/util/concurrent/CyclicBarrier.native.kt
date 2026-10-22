package java.util.concurrent

import java.lang.Runnable
import java.util.concurrent.locks.ReentrantLock

actual open class CyclicBarrier {
    private val lock = ReentrantLock()
    private val trip = lock.newCondition()
    private val partiesCount: Int
    private val barrierCommand: Runnable?
    private var count: Int
    private var broken: Boolean = false
    private var generation: Int = 0

    actual constructor(parties: Int, barrierAction: Runnable?) {
        if (parties <= 0) throw IllegalArgumentException()
        this.partiesCount = parties
        this.count = parties
        this.barrierCommand = barrierAction
    }

    actual constructor(parties: Int) : this(parties, null)

    actual open fun getParties(): Int = partiesCount
    actual open fun isBroken(): Boolean {
        lock.lock()
        try {
            return broken
        } finally {
            lock.unlock()
        }
    }

    actual open fun getNumberWaiting(): Int {
        lock.lock()
        try {
            return partiesCount - count
        } finally {
            lock.unlock()
        }
    }

    actual open val parties: Int get() = getParties()
    actual open val isBroken: Boolean get() = isBroken()
    actual open val numberWaiting: Int get() = getNumberWaiting()

    actual open fun reset() {
        lock.lock()
        try {
            breakBarrier()
            nextGeneration()
        } finally {
            lock.unlock()
        }
    }

    actual open fun await(): Int = await(0, TimeUnit.MILLISECONDS)

    actual open fun await(timeout: Long, unit: TimeUnit): Int {
        lock.lock()
        try {
            val g = generation
            if (broken) throw BrokenBarrierException()

            val index = --count
            if (index == 0) {
                var ranAction = false
                try {
                    barrierCommand?.run()
                    ranAction = true
                    nextGeneration()
                    return 0
                } finally {
                    if (!ranAction) breakBarrier()
                }
            }

            var nanos = if (timeout > 0) unit.toNanos(timeout) else -1L
            while (true) {
                try {
                    if (nanos < 0) {
                        trip.await()
                    } else {
                        if (nanos <= 0) {
                            breakBarrier()
                            throw TimeoutException()
                        }
                        nanos = trip.awaitNanos(nanos)
                    }
                } catch (ie: InterruptedException) {
                    if (g == generation && !broken) {
                        breakBarrier()
                        throw ie
                    }
                }

                if (broken) throw BrokenBarrierException()
                if (g != generation) return index
            }
        } finally {
            lock.unlock()
        }
    }

    private fun nextGeneration() {
        trip.signalAll()
        count = partiesCount
        broken = false
        generation++
    }

    private fun breakBarrier() {
        broken = true
        count = partiesCount
        trip.signalAll()
    }
}
