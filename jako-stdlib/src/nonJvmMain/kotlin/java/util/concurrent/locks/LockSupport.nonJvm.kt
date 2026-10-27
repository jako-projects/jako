package java.util.concurrent.locks

import java.lang.Thread
import java.lang.lockSupportParkInternal
import java.lang.parkBlockerValue
import java.lang.unparkInternal

actual object LockSupport {
    actual fun unpark(thread: Thread?) {
        thread?.unparkInternal()
    }

    actual fun park(blocker: Any?) {
        val t = Thread.currentThread()
        t.parkBlockerValue = blocker
        try {
            t.lockSupportParkInternal(false, 0L)
        } finally {
            t.parkBlockerValue = null
        }
    }

    actual fun parkNanos(blocker: Any?, nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.parkBlockerValue = blocker
            try {
                t.lockSupportParkInternal(false, nanos)
            } finally {
                t.parkBlockerValue = null
            }
        }
    }

    actual fun parkUntil(blocker: Any?, deadline: Long) {
        val t = Thread.currentThread()
        t.parkBlockerValue = blocker
        try {
            t.lockSupportParkInternal(true, deadline)
        } finally {
            t.parkBlockerValue = null
        }
    }

    actual fun getBlocker(t: Thread?): Any? {
        if (t == null) throw NullPointerException()
        return t.parkBlockerValue
    }

    actual fun park() {
        val t = Thread.currentThread()
        t.lockSupportParkInternal(false, 0L)
    }

    actual fun parkNanos(nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.lockSupportParkInternal(false, nanos)
        }
    }

    actual fun parkUntil(deadline: Long) {
        val t = Thread.currentThread()
        t.lockSupportParkInternal(true, deadline)
    }
}

