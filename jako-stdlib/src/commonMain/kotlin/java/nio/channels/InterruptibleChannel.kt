@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

expect interface InterruptibleChannel : Channel {
    override fun close()
}
