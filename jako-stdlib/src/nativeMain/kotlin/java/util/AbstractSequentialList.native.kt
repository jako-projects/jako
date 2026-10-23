package java.util

actual abstract class AbstractSequentialList<E> protected actual constructor() : AbstractMutableList<E>() {
    actual override fun get(index: Int): E {
        try {
            return listIterator(index).next()
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun set(index: Int, element: E): E {
        try {
            val e = listIterator(index)
            val oldVal = e.next()
            e.set(element)
            return oldVal
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun add(index: Int, element: E) {
        try {
            listIterator(index).add(element)
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun removeAt(index: Int): E {
        try {
            val out = listIterator(index)
            val outCast = out.next()
            out.remove()
            return outCast
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        try {
            var modified = false
            val e1 = listIterator(index)
            for (e in elements) {
                e1.add(e)
                modified = true
            }
            return modified
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun iterator(): MutableIterator<E> = listIterator()

    actual abstract override fun listIterator(index: Int): MutableListIterator<E>
}
