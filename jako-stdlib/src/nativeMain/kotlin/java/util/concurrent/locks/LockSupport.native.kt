package java.util.concurrent.locks

import java.lang.Thread
import kotlin.concurrent.withLock

actual object LockSupport {
    actual fun unpark(thread: Thread?) {
        thread?.unpark()
    }

    actual fun park(blocker: Any?) {
        val t = Thread.currentThread()
        t.parkBlocker.value = blocker
        try {
            t.lockSupportPark(false, 0L)
        } finally {
            t.parkBlocker.value = null
        }
    }

    actual fun parkNanos(blocker: Any?, nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.parkBlocker.value = blocker
            try {
                t.lockSupportPark(false, nanos)
            } finally {
                t.parkBlocker.value = null
            }
        }
    }

    actual fun parkUntil(blocker: Any?, deadline: Long) {
        val t = Thread.currentThread()
        t.parkBlocker.value = blocker
        try {
            t.lockSupportPark(true, deadline)
        } finally {
            t.parkBlocker.value = null
        }
    }

    actual fun getBlocker(t: Thread?): Any? {
        if (t == null) throw NullPointerException()
        return t.parkBlocker.value
    }

    actual fun park() {
        val t = Thread.currentThread()
        t.lockSupportPark(false, 0L)
    }

    actual fun parkNanos(nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.lockSupportPark(false, nanos)
        }
    }

    actual fun parkUntil(deadline: Long) {
        val t = Thread.currentThread()
        t.lockSupportPark(true, deadline)
    }
}
