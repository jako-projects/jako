package java.nio

actual abstract class Buffer(
    private var cap: Int,
    private var lim: Int
) {
    private var pos: Int = 0
    private var mrk: Int = -1

    actual open fun capacity(): Int = cap
    actual open fun position(): Int = pos

    actual open fun position(newPosition: Int): Buffer {
        if (newPosition < 0 || newPosition > lim) throw IllegalArgumentException("Bad position: $newPosition")
        pos = newPosition
        if (mrk > pos) mrk = -1
        return this
    }

    actual open fun limit(): Int = lim

    actual open fun limit(newLimit: Int): Buffer {
        if (newLimit < 0 || newLimit > cap) throw IllegalArgumentException("Bad limit: $newLimit")
        lim = newLimit
        if (pos > lim) pos = lim
        if (mrk > lim) mrk = -1
        return this
    }

    actual open fun mark(): Buffer {
        mrk = pos
        return this
    }

    actual open fun reset(): Buffer {
        if (mrk < 0) throw IllegalStateException("Mark not set")
        pos = mrk
        return this
    }

    actual open fun clear(): Buffer {
        pos = 0
        lim = cap
        mrk = -1
        return this
    }

    actual open fun flip(): Buffer {
        lim = pos
        pos = 0
        mrk = -1
        return this
    }

    actual open fun rewind(): Buffer {
        pos = 0
        mrk = -1
        return this
    }

    actual open fun remaining(): Int = lim - pos

    actual open fun hasRemaining(): Boolean = pos < lim

    actual abstract fun isReadOnly(): Boolean
    actual abstract fun hasArray(): Boolean
    actual abstract fun array(): Any
    actual abstract fun arrayOffset(): Int
    actual abstract fun isDirect(): Boolean

    actual abstract val isReadOnly: Boolean
    actual abstract val isDirect: Boolean
}
