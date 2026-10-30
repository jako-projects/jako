package kotlin.concurrent

import com.bennyhuo.jako.annotations.WebSupport

@WebSupport(
    reason = "JavaScript and WebAssembly runtimes are single-threaded and do not support preemptive shared-memory threads",
    alternative = "kotlinx.coroutines or java.util.concurrent.Executors / HandlerThread"
)
public fun thread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    name: String? = null,
    priority: Int = -1,
    block: () -> Unit
): java.lang.Thread {
    val thread = object : java.lang.Thread() {
        override fun run() {
            block()
        }
    }
    if (name != null) thread.setName(name)
    if (isDaemon) thread.setDaemon(true)
    if (priority > 0) thread.setPriority(priority)
    if (start) thread.start()
    return thread
}
