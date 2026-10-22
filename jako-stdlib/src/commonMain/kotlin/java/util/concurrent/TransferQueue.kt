@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect interface TransferQueue<E> : BlockingQueue<E> {
    fun tryTransfer(e: E): Boolean
    fun transfer(e: E)
    fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean
    fun hasWaitingConsumer(): Boolean
    fun getWaitingConsumerCount(): Int
}
