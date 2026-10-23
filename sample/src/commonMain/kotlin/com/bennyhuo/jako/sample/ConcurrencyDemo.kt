package com.bennyhuo.jako.sample

import java.lang.Thread
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.jvm.Synchronized

object ConcurrencyDemo {

    private class SynchronizedLedger {
        private var total: Int = 0
        private var lockVerified: Boolean = true

        @Synchronized
        fun record(amount: Int) {
            if (!Thread.holdsLock(this)) {
                lockVerified = false
            }
            total += amount
        }

        @Synchronized
        fun snapshot(): Pair<Int, Boolean> {
            return total to (lockVerified && Thread.holdsLock(this))
        }
    }

    data class ConcurrencyReport(
        val totalProcessed: Int,
        val bucketCount: Int,
        val snapshotSum: Int,
        val synchronizedLedgerSum: Int,
        val monitorLockVerified: Boolean
    )

    fun run(workerCount: Int = 4, tasksPerWorker: Int = 10): ConcurrencyReport {
        val pool = Executors.newFixedThreadPool(workerCount)
        val latch = CountDownLatch(workerCount * tasksPerWorker)
        val counter = AtomicInteger(0)
        val map = ConcurrentHashMap<String, Int>()
        val rwLock = ReentrantReadWriteLock()
        val ledger = SynchronizedLedger()
        var sharedAccumulator = 0

        try {
            for (w in 0 until workerCount) {
                for (t in 0 until tasksPerWorker) {
                    pool.execute {
                        try {
                            val seq = counter.incrementAndGet()
                            val key = "worker-$w"
                            rwLock.write {
                                map[key] = (map[key] ?: 0) + 1
                                sharedAccumulator += seq
                            }
                            ledger.record(seq)
                        } finally {
                            latch.countDown()
                        }
                    }
                }
            }

            check(latch.await(10, TimeUnit.SECONDS)) { "Timed out waiting for worker tasks" }

            val snapshotSum = rwLock.read {
                sharedAccumulator
            }
            val (ledgerSum, lockVerified) = ledger.snapshot()

            return ConcurrencyReport(
                totalProcessed = counter.get(),
                bucketCount = map.size,
                snapshotSum = snapshotSum,
                synchronizedLedgerSum = ledgerSum,
                monitorLockVerified = lockVerified
            )
        } finally {
            pool.shutdown()
            pool.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}
