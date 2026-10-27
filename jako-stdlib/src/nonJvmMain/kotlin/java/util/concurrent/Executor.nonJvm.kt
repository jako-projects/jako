package java.util.concurrent

import java.lang.Runnable
import kotlin.concurrent.withLock

actual fun interface Executor {
    actual fun execute(command: Runnable)
}
