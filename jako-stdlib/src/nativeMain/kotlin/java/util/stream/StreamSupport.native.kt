package java.util.stream

import java.util.Spliterator
import java.util.Spliterators
import java.util.function.Supplier

actual class StreamSupport private constructor() {
    actual companion object {
        actual fun <T> stream(spliterator: Spliterator<T>, parallel: Boolean): Stream<T> {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.next())
            }
            return ReferenceStream(seq, StreamState(parallel = parallel))
        }

        actual fun <T> stream(
            supplier: Supplier<out Spliterator<T>>,
            characteristics: Int,
            parallel: Boolean
        ): Stream<T> {
            val seq = sequence {
                val iter = Spliterators.iterator(supplier.get())
                while (iter.hasNext()) yield(iter.next())
            }
            return ReferenceStream(seq, StreamState(parallel = parallel))
        }

        actual fun intStream(spliterator: Spliterator.OfInt, parallel: Boolean): IntStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextInt())
            }
            return NativeIntStream(seq, StreamState(parallel = parallel))
        }

        actual fun longStream(spliterator: Spliterator.OfLong, parallel: Boolean): LongStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextLong())
            }
            return NativeLongStream(seq, StreamState(parallel = parallel))
        }

        actual fun doubleStream(spliterator: Spliterator.OfDouble, parallel: Boolean): DoubleStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextDouble())
            }
            return NativeDoubleStream(seq, StreamState(parallel = parallel))
        }
    }
}
