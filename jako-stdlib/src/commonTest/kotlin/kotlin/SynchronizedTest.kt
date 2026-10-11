package kotlin

import java.lang.Thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SynchronizedTest {

    @Test
    fun testBasicSynchronized() {
        val lock = Any()
        val result = synchronized(lock) {
            42
        }
        assertEquals(42, result)
    }

    @Test
    fun testSynchronizedReentrancy() {
        val lock = Any()
        var executedOuter = false
        var executedInner = false

        synchronized(lock) {
            executedOuter = true
            synchronized(lock) {
                executedInner = true
            }
        }

        assertTrue(executedOuter)
        assertTrue(executedInner)
    }

    @Test
    fun testSynchronizedConcurrency() {
        val lock = Any()
        var counter = 0
        val iterations = 1000

        val t1 = Thread {
            for (i in 0 until iterations) {
                synchronized(lock) {
                    counter++
                }
            }
        }

        val t2 = Thread {
            for (i in 0 until iterations) {
                synchronized(lock) {
                    counter++
                }
            }
        }

        t1.start()
        t2.start()
        t1.join()
        t2.join()

        assertEquals(iterations * 2, counter)
    }
}
