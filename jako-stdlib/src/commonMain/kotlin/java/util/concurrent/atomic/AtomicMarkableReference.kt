@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

/**
 * [Nullability Decision] AtomicMarkableReference reference is nullable (`V?`), markHolder is non-null `BooleanArray` of length >= 1.
 */
expect open class AtomicMarkableReference<V>(initialRef: V?, initialMark: Boolean) {
    open fun getReference(): V?
    open fun isMarked(): Boolean
    open val reference: V?
    open val isMarked: Boolean
    open fun get(markHolder: BooleanArray): V?
    open fun weakCompareAndSet(expectedReference: V?, newReference: V?, expectedMark: Boolean, newMark: Boolean): Boolean
    open fun compareAndSet(expectedReference: V?, newReference: V?, expectedMark: Boolean, newMark: Boolean): Boolean
    open fun set(newReference: V?, newMark: Boolean)
    open fun attemptMark(expectedReference: V?, newMark: Boolean): Boolean
}
