@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect fun interface Executor {
    fun execute(command: Runnable)
}
