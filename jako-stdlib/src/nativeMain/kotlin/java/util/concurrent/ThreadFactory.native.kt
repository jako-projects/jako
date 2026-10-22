package java.util.concurrent

import java.lang.Runnable
import java.lang.Thread
import kotlin.concurrent.withLock

actual fun interface ThreadFactory {
    actual fun newThread(r: Runnable): Thread
}
