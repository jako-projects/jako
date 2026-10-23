@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable
import java.lang.Thread

expect fun interface ThreadFactory {
    fun newThread(r: Runnable): Thread
}
