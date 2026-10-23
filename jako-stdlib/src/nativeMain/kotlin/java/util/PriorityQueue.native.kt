package java.util

import java.io.Serializable

/**
 * Binary min-heap implementation of java.util.PriorityQueue matching JDK 8 semantics.
 */
actual open class PriorityQueue<E> : AbstractQueue<E>, Serializable {
    private val comp: Comparator<in E>?
    private val heap: ArrayList<E>

    actual constructor() : this(11, null)

    actual constructor(initialCapacity: Int) : this(initialCapacity, null)

    actual constructor(comparator: Comparator<in E>?) : this(11, comparator)

    actual constructor(initialCapacity: Int, comparator: Comparator<in E>?) : super() {
        if (initialCapacity < 1) throw IllegalArgumentException()
        this.comp = comparator
        this.heap = ArrayList(initialCapacity)
    }

    @Suppress("UNCHECKED_CAST")
    actual constructor(c: Collection<E>) : super() {
        when (c) {
            is SortedSet<*> -> {
                this.comp = (c as SortedSet<E>).comparator()
                this.heap = ArrayList(c.size)
                for (e in c) {
                    if (e == null) throw NullPointerException()
                    this.heap.add(e)
                }
            }
            is PriorityQueue<*> -> {
                val pq = c as PriorityQueue<E>
                this.comp = pq.comparator()
                this.heap = ArrayList(pq.heap)
            }
            else -> {
                this.comp = null
                this.heap = ArrayList(c.size)
                for (e in c) {
                    if (e == null) throw NullPointerException()
                    this.heap.add(e)
                }
                heapify()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual constructor(c: PriorityQueue<out E>) : super() {
        this.comp = c.comparator() as Comparator<in E>?
        this.heap = ArrayList(c.size)
        for (e in c) {
            this.heap.add(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual constructor(c: SortedSet<out E>) : super() {
        this.comp = c.comparator() as Comparator<in E>?
        this.heap = ArrayList(c.size)
        for (e in c) {
            if (e == null) throw NullPointerException()
            this.heap.add(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun compareElements(a: E, b: E): Int {
        val c = comp
        return if (c != null) {
            c.compare(a, b)
        } else {
            (a as Comparable<E>).compareTo(b)
        }
    }

    private fun siftUp(kIndex: Int, x: E) {
        var k = kIndex
        while (k > 0) {
            val parent = (k - 1) ushr 1
            val e = heap[parent]
            if (compareElements(x, e) >= 0) break
            heap[k] = e
            k = parent
        }
        heap[k] = x
    }

    private fun siftDown(kIndex: Int, x: E) {
        var k = kIndex
        val n = heap.size
        val half = n ushr 1
        while (k < half) {
            var child = (k shl 1) + 1
            var c = heap[child]
            val right = child + 1
            if (right < n && compareElements(c, heap[right]) > 0) {
                child = right
                c = heap[child]
            }
            if (compareElements(x, c) <= 0) break
            heap[k] = c
            k = child
        }
        heap[k] = x
    }

    private fun heapify() {
        for (i in (heap.size ushr 1) - 1 downTo 0) {
            siftDown(i, heap[i])
        }
    }

    actual override val size: Int get() = heap.size

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        val i = heap.size
        heap.add(e)
        if (i > 0) {
            siftUp(i, e)
        }
        return true
    }

    actual override fun peek(): E? = if (heap.isEmpty()) null else heap[0]

    actual override fun poll(): E? {
        if (heap.isEmpty()) return null
        val result = heap[0]
        val lastIndex = heap.size - 1
        val x = heap.removeAt(lastIndex)
        if (lastIndex > 0) {
            siftDown(0, x)
        }
        return result
    }

    private fun removeAt(i: Int): E? {
        val s = heap.size - 1
        if (s == i) {
            heap.removeAt(i)
        } else {
            val moved = heap.removeAt(s)
            siftDown(i, moved)
            if (heap[i] === moved) {
                siftUp(i, moved)
                if (heap[i] !== moved) return moved
            }
        }
        return null
    }

    actual override fun remove(element: E): Boolean {
        val idx = heap.indexOf(element)
        if (idx == -1) return false
        removeAt(idx)
        return true
    }

    actual override fun contains(element: E): Boolean = heap.contains(element)

    actual override fun clear() {
        heap.clear()
    }

    actual open fun comparator(): Comparator<in E>? = comp

    actual override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
        private var cursor = 0
        private var lastRet = -1

        override fun hasNext(): Boolean = cursor < heap.size

        override fun next(): E {
            if (cursor >= heap.size) throw NoSuchElementException()
            lastRet = cursor
            return heap[cursor++]
        }

        override fun remove() {
            if (lastRet < 0) throw IllegalStateException()
            removeAt(lastRet)
            cursor = lastRet
            lastRet = -1
        }
    }
}
