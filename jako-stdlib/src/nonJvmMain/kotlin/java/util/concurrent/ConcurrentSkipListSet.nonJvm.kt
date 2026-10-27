package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.util.NavigableSet
import kotlin.concurrent.withLock

actual open class ConcurrentSkipListSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    private val m: ConcurrentSkipListMap<E, Boolean>

    actual constructor() : super() {
        m = ConcurrentSkipListMap()
    }

    actual constructor(comparator: Comparator<in E>?) : super() {
        m = ConcurrentSkipListMap(comparator)
    }

    actual constructor(c: Collection<E>) : super() {
        m = ConcurrentSkipListMap()
        addAll(c)
    }

    actual override val size: Int get() = m.size
    actual override fun isEmpty(): Boolean = m.isEmpty()
    actual override fun contains(element: E): Boolean = m.containsKey(element)
    actual override fun add(element: E): Boolean = m.putIfAbsent(element, true) == null
    actual override fun remove(element: E): Boolean = m.remove(element, true)
    actual override fun clear() = m.clear()
    actual override fun iterator(): MutableIterator<E> = m.keys.iterator()

    actual override fun descendingIterator(): MutableIterator<E> {
        val snapshot = m.snapshotKeysDescending()
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastKey: E? = null
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val k = it.next()
                lastKey = k
                return k
            }
            override fun remove() {
                val k = lastKey ?: throw IllegalStateException()
                lastKey = null
                m.remove(k)
            }
        }
    }

    actual override fun comparator(): Comparator<in E>? = m.comparator()
    actual override fun first(): E = m.firstKey()
    actual override fun last(): E = m.lastKey()
    actual open fun lower(e: E): E? = m.lowerKey(e)
    actual open fun floor(e: E): E? = m.floorKey(e)
    actual open fun ceiling(e: E): E? = m.ceilingKey(e)
    actual open fun higher(e: E): E? = m.higherKey(e)
    actual open fun pollFirst(): E? = m.pollFirstEntry()?.key
    actual open fun pollLast(): E? = m.pollLastEntry()?.key
}
