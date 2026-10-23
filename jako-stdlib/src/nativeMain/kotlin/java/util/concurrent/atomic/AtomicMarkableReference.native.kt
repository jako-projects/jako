package java.util.concurrent.atomic

actual open class AtomicMarkableReference<V> actual constructor(
    initialRef: V?,
    initialMark: Boolean
) {
    private class Pair<T>(val reference: T?, val mark: Boolean)

    private val pairRef = kotlin.concurrent.AtomicReference(Pair(initialRef, initialMark))

    actual open fun getReference(): V? = pairRef.value.reference

    actual open fun isMarked(): Boolean = pairRef.value.mark

    actual open val reference: V? get() = getReference()

    actual open val isMarked: Boolean get() = isMarked()

    actual open fun get(markHolder: BooleanArray): V? {
        val current = pairRef.value
        markHolder[0] = current.mark
        return current.reference
    }

    actual open fun weakCompareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedMark: Boolean,
        newMark: Boolean
    ): Boolean = compareAndSet(expectedReference, newReference, expectedMark, newMark)

    actual open fun compareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedMark: Boolean,
        newMark: Boolean
    ): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            expectedMark == current.mark &&
            ((newReference === current.reference && newMark == current.mark) ||
                pairRef.compareAndSet(current, Pair(newReference, newMark)))
    }

    actual open fun set(newReference: V?, newMark: Boolean) {
        val current = pairRef.value
        if (newReference !== current.reference || newMark != current.mark) {
            pairRef.value = Pair(newReference, newMark)
        }
    }

    actual open fun attemptMark(expectedReference: V?, newMark: Boolean): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            (newMark == current.mark ||
                pairRef.compareAndSet(current, Pair(expectedReference, newMark)))
    }
}
