package jako.android.test.os

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OsMessagingTest {
    @Test
    fun testMessageObtainAndCopy() {
        val m1 = Message.obtain()
        m1.what = 42
        m1.arg1 = 10
        m1.arg2 = 20
        m1.obj = "payload"
        m1.data.putString("k", "v")

        val m2 = Message.obtain(m1)
        assertEquals(42, m2.what)
        assertEquals(10, m2.arg1)
        assertEquals(20, m2.arg2)
        assertEquals("payload", m2.obj)
        assertEquals("v", m2.peekData()?.getString("k"))

        m1.recycle()
        m2.recycle()
    }

    @Test
    fun testHandlerThreadAndHandlerDispatch() {
        val ht = HandlerThread("worker-1")
        ht.start()
        val looper = ht.looper
        assertNotNull(looper)
        assertSame(ht, looper.thread)

        val received = mutableListOf<String>()
        val latch = CountDownLatch(3)

        val handler = Handler(looper, Handler.Callback { msg ->
            when (msg.what) {
                1 -> {
                    received.add("what=1:${msg.arg1}")
                    latch.countDown()
                    true
                }
                else -> false
            }
        })

        // Post runnable
        handler.post(Runnable {
            assertTrue(looper.isCurrentThread)
            assertSame(looper, Looper.myLooper())
            received.add("runnable")
            latch.countDown()
        })

        // Send message
        val msg = handler.obtainMessage(1, 99, 0)
        assertTrue(handler.sendMessage(msg))

        // Send message that will be removed before dispatch
        handler.sendEmptyMessageDelayed(2, 5000L)
        assertTrue(handler.hasMessages(2))
        handler.removeMessages(2)
        assertFalse(handler.hasMessages(2))

        // Post delayed runnable
        handler.postDelayed(Runnable {
            received.add("delayed")
            latch.countDown()
        }, 30L)

        assertTrue(latch.await(3, TimeUnit.SECONDS))
        assertEquals(listOf("runnable", "what=1:99", "delayed"), received)

        assertTrue(ht.quitSafely())
        ht.join(2000L)
        assertNull(ht.looper)
    }

    @Test
    fun testIdleHandlerAndSendAtFrontOfQueue() {
        val ht = HandlerThread("worker-2")
        ht.start()
        val looper = ht.looper!!
        val handler = Handler(looper)

        val order = mutableListOf<Int>()
        val idleCalled = AtomicInteger(0)
        val latch = CountDownLatch(1)

        handler.post(Runnable {
            looper.queue.addIdleHandler {
                idleCalled.incrementAndGet()
                latch.countDown()
                false // remove after first idle
            }
            handler.sendMessage(handler.obtainMessage(10))
            handler.sendMessageAtFrontOfQueue(handler.obtainMessage(20))
        })

        val subClassHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                order.add(msg.what)
            }
        }

        val orderLatch = CountDownLatch(2)
        subClassHandler.post(Runnable {
            subClassHandler.sendEmptyMessage(1)
            subClassHandler.sendEmptyMessageAtTime(2, 1L) // front
            subClassHandler.post(Runnable { orderLatch.countDown() })
            subClassHandler.postAtFrontOfQueue(Runnable { orderLatch.countDown() })
        })

        assertTrue(latch.await(3, TimeUnit.SECONDS))
        assertTrue(orderLatch.await(3, TimeUnit.SECONDS))
        assertEquals(1, idleCalled.get())

        val threadRef = AtomicReference<Thread>()
        val syncLatch = CountDownLatch(1)
        handler.post(Runnable {
            threadRef.set(Thread.currentThread())
            syncLatch.countDown()
        })
        assertTrue(syncLatch.await(2, TimeUnit.SECONDS))
        assertSame(ht, threadRef.get())

        ht.quit()
        ht.join(2000L)
    }
}
