@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * A pair of channels that implements a unidirectional pipe.
 */
expect abstract class Pipe {
    protected constructor()

    abstract class SourceChannel : SelectableChannel, ReadableByteChannel, ScatteringByteChannel, InterruptibleChannel {
        override fun validOps(): Int
    }
    abstract class SinkChannel : SelectableChannel, WritableByteChannel, GatheringByteChannel, InterruptibleChannel {
        override fun validOps(): Int
    }

    abstract fun source(): SourceChannel
    abstract fun sink(): SinkChannel

    companion object {
        fun open(): Pipe
    }
}
