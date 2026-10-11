package jako.test.util.concurrent

import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FutureTaskTest {

    @Test
    fun testFutureTaskCallableExecution() {
        val task = FutureTask {
            "Hello Future"
        }
        assertFalse(task.isDone)
        assertFalse(task.isCancelled)

        task.run()

        assertTrue(task.isDone)
        assertEquals("Hello Future", task.get())
        assertEquals("Hello Future", task.get(1, TimeUnit.SECONDS))
    }

    @Test
    fun testFutureTaskRunnableWithResult() {
        var sideEffect = 0
        val task = FutureTask({ sideEffect = 42 }, 100)

        task.run()

        assertTrue(task.isDone)
        assertEquals(42, sideEffect)
        assertEquals(100, task.get())
    }

    @Test
    fun testFutureTaskCancellation() {
        val task = FutureTask { "Value" }
        val cancelled = task.cancel(true)
        assertTrue(cancelled)
        assertTrue(task.isCancelled)
        assertTrue(task.isDone)

        assertFailsWith<CancellationException> {
            task.get()
        }
    }

    @Test
    fun testFutureTaskExecutionException() {
        val task = FutureTask<String> {
            throw IllegalStateException("Failure in task")
        }
        task.run()
        assertTrue(task.isDone)

        val ex = assertFailsWith<ExecutionException> {
            task.get()
        }
        assertEquals("Failure in task", ex.cause?.message)
    }

    @Test
    fun testExecutorServiceSubmit() {
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit(Callable { 1 + 2 })
        assertEquals(3, future.get(5, TimeUnit.SECONDS))

        val runnableFuture = executor.submit({ /* noop */ }, "done")
        assertEquals("done", runnableFuture.get(5, TimeUnit.SECONDS))

        executor.shutdown()
        assertTrue(executor.isShutdown())
    }
}
