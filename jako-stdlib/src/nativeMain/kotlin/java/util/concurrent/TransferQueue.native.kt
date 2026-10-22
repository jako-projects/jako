package java.util.concurrent

import kotlin.concurrent.withLock

actual interface TransferQueue<E> : BlockingQueue<E> {
    actual fun tryTransfer(e: E): Boolean
    actual fun transfer(e: E)
    actual fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean
    actual fun hasWaitingConsumer(): Boolean
    actual fun getWaitingConsumerCount(): Int
}
