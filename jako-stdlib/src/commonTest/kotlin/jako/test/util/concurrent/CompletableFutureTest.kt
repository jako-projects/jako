package jako.test.util.concurrent

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompletableFutureTest {

    @Test
    fun testCompletedFuture() {
        val cf = CompletableFuture.completedFuture("Hello Jako")
        assertTrue(cf.isDone)
        assertFalse(cf.isCancelled)
        assertEquals("Hello Jako", cf.get())
        assertEquals("Hello Jako", cf.getNow("default"))
        assertEquals("Hello Jako", cf.join())
    }

    @Test
    fun testThenApplyAndAccept() {
        val cf = CompletableFuture<Int>()
        var acceptedValue = 0

        val mapped = cf.thenApply(Function<Int, String> { "Result: $it" })
        mapped.thenAccept { acceptedValue = it.length }

        cf.complete(42)

        assertEquals("Result: 42", mapped.get())
        assertEquals("Result: 42".length, acceptedValue)
    }

    @Test
    fun testThenCombine() {
        val cf1 = CompletableFuture.completedFuture(10)
        val cf2 = CompletableFuture.completedFuture(20)

        val combined = cf1.thenCombine(cf2, BiFunction<Int, Int, Int> { a, b -> a + b })
        assertEquals(30, combined.get())
    }

    @Test
    fun testExceptionally() {
        val cf = CompletableFuture<String>()
        val fallback = cf.exceptionally(Function<Throwable, String> { ex -> "Recovered from: ${ex.message}" })

        cf.completeExceptionally(IllegalArgumentException("Bad Input"))

        assertEquals("Recovered from: Bad Input", fallback.get())
    }

    @Test
    fun testHandle() {
        val cf = CompletableFuture<Int>()
        val handled = cf.handle(BiFunction<Int?, Throwable?, String> { res, ex ->
            if (ex != null) "Error" else "Success: $res"
        })

        cf.complete(100)
        assertEquals("Success: 100", handled.get())
    }

    @Test
    fun testAllOf() {
        val cf1 = CompletableFuture.completedFuture("A")
        val cf2 = CompletableFuture.completedFuture("B")
        val all = CompletableFuture.allOf(cf1, cf2)

        assertTrue(all.isDone)
    }
}
