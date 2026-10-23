@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

/**
 * [Nullability Decision] AtomicStampedReference reference is nullable (`V?`), stampHolder is non-null `IntArray` of length >= 1.
 */
expect open class AtomicStampedReference<V>(initialRef: V?, initialStamp: Int) {
    open fun getReference(): V?
    open fun getStamp(): Int
    open val reference: V?
    open val stamp: Int
    open fun get(stampHolder: IntArray): V?
    open fun weakCompareAndSet(expectedReference: V?, newReference: V?, expectedStamp: Int, newStamp: Int): Boolean
    open fun compareAndSet(expectedReference: V?, newReference: V?, expectedStamp: Int, newStamp: Int): Boolean
    open fun set(newReference: V?, newStamp: Int)
    open fun attemptStamp(expectedReference: V?, newStamp: Int): Boolean
}
