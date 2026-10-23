@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.IOException

/** [Nullability Decision] Java ClosedChannelException has only a non-null no-arg constructor. */
expect open class ClosedChannelException : IOException {
    constructor()
}
