package java.util.concurrent.atomic

actual open class AtomicStampedReference<V> actual constructor(
    initialRef: V?,
    initialStamp: Int
) {
    private class Pair<T>(val reference: T?, val stamp: Int)

    private val pairRef = kotlin.concurrent.AtomicReference(Pair(initialRef, initialStamp))

    actual open fun getReference(): V? = pairRef.value.reference

    actual open fun getStamp(): Int = pairRef.value.stamp

    actual open val reference: V? get() = getReference()

    actual open val stamp: Int get() = getStamp()

    actual open fun get(stampHolder: IntArray): V? {
        val current = pairRef.value
        stampHolder[0] = current.stamp
        return current.reference
    }

    actual open fun weakCompareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedStamp: Int,
        newStamp: Int
    ): Boolean = compareAndSet(expectedReference, newReference, expectedStamp, newStamp)

    actual open fun compareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedStamp: Int,
        newStamp: Int
    ): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            expectedStamp == current.stamp &&
            ((newReference === current.reference && newStamp == current.stamp) ||
                pairRef.compareAndSet(current, Pair(newReference, newStamp)))
    }

    actual open fun set(newReference: V?, newStamp: Int) {
        val current = pairRef.value
        if (newReference !== current.reference || newStamp != current.stamp) {
            pairRef.value = Pair(newReference, newStamp)
        }
    }

    actual open fun attemptStamp(expectedReference: V?, newStamp: Int): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            (newStamp == current.stamp ||
                pairRef.compareAndSet(current, Pair(expectedReference, newStamp)))
    }
}
