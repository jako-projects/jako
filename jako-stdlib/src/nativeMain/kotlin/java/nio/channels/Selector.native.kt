package java.nio.channels

import java.io.Closeable
import kotlin.concurrent.withLock

actual abstract class Selector protected actual constructor() : Closeable {
    actual abstract fun isOpen(): Boolean
    actual abstract fun keys(): MutableSet<SelectionKey>
    actual abstract fun selectedKeys(): MutableSet<SelectionKey>
    actual abstract fun selectNow(): Int
    actual abstract fun select(timeout: Long): Int
    actual abstract fun select(): Int
    actual abstract fun wakeup(): Selector
    actual abstract override fun close()

    actual companion object {
        actual fun open(): Selector = SelectorImpl()
    }
}
