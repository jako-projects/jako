package jako.test.lang

import java.io.StringReader
import java.lang.ArrayStoreException
import java.lang.Class
import java.lang.ClassNotFoundException
import java.lang.IllegalMonitorStateException
import java.lang.InheritableThreadLocal
import java.lang.NegativeArraySizeException
import java.lang.NoSuchFieldException
import java.lang.Readable
import java.lang.Runnable
import java.lang.SecurityException
import java.lang.StackTraceElement
import java.lang.StringIndexOutOfBoundsException
import java.lang.Thread
import java.lang.reflect.Array as ReflectArray
import java.nio.CharBuffer
import java.util.Scanner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.java
import kotlin.jvm.javaObjectType
import kotlin.jvm.javaPrimitiveType
import kotlin.synchronized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReflectAndLangSupplementsTest {

    @Test
    fun testReflectArray() {
        val intPrimeClass = Int::class.javaPrimitiveType!!
        assertTrue(intPrimeClass.isPrimitive)
        assertEquals("int", intPrimeClass.name)

        val arr = ReflectArray.newInstance(intPrimeClass, 4)
        assertTrue(arr is IntArray)
        assertEquals(4, ReflectArray.getLength(arr))

        ReflectArray.setInt(arr, 0, 10)
        ReflectArray.setShort(arr, 1, 20.toShort())
        ReflectArray.setByte(arr, 2, 30.toByte())
        ReflectArray.set(arr, 3, 40)

        assertEquals(10, ReflectArray.getInt(arr, 0))
        assertEquals(20L, ReflectArray.getLong(arr, 1))
        assertEquals(30.0, ReflectArray.getDouble(arr, 2))
        assertEquals(40, ReflectArray.get(arr, 3))

        assertFailsWith<IndexOutOfBoundsException> { ReflectArray.getInt(arr, 4) }
        assertFailsWith<NegativeArraySizeException> { ReflectArray.newInstance(intPrimeClass, -1) }
        assertFailsWith<IllegalArgumentException> { ReflectArray.getLength("notAnArray") }

        val matrix = ReflectArray.newInstance(intPrimeClass, 2, 3)
        assertEquals(2, ReflectArray.getLength(matrix))
        val row0 = ReflectArray.get(matrix, 0)!!
        assertEquals(3, ReflectArray.getLength(row0))
        ReflectArray.setInt(row0, 2, 99)
        assertEquals(99, ReflectArray.getInt(row0, 2))
    }

    @Test
    fun testClassMethods() {
        val strClass = String::class.java
        assertFalse(strClass.isPrimitive)
        assertEquals("java.lang.String", strClass.name)
        assertEquals("String", strClass.simpleName)
        assertEquals("java.lang.String", strClass.canonicalName)
        assertTrue(strClass.isInstance("hello"))
        assertFalse(strClass.isInstance(123))
        assertEquals("hello", strClass.cast("hello"))
        assertNull(strClass.cast(null))
        assertFailsWith<ClassCastException> { strClass.cast(123) }

        val intObjClass = Int::class.javaObjectType
        assertFalse(intObjClass.isPrimitive)
        assertEquals("java.lang.Integer", intObjClass.name)
        assertEquals("Integer", intObjClass.simpleName)
        assertNull(String::class.javaPrimitiveType)

        val intArrClass = IntArray::class.java
        assertTrue(intArrClass.isArray)
        assertEquals(Int::class.javaPrimitiveType, intArrClass.componentType)
        assertEquals("int[]", intArrClass.simpleName)
        assertEquals("int[]", intArrClass.canonicalName)

        assertEquals(String::class.java, Class.forName("java.lang.String"))
        assertFailsWith<ClassNotFoundException> { Class.forName("non.existent.ClassName123") }
    }

    @Test
    fun testReadableWithReaderCharBufferAndScanner() {
        val reader: Readable = StringReader("alpha beta")
        val cb = CharBuffer.allocate(16)
        val n = reader.read(cb)
        assertEquals(10, n)
        cb.flip()
        assertEquals("alpha beta", cb.toString())

        val copyBuf = CharBuffer.allocate(5)
        val n2 = (cb as Readable).read(copyBuf)
        assertEquals(5, n2)
        copyBuf.flip()
        assertEquals("alpha", copyBuf.toString())

        val scanner = Scanner(StringReader("100 200 300"))
        assertEquals(100, scanner.nextInt())
        assertEquals(200, scanner.nextInt())
        assertEquals(300, scanner.nextInt())
        scanner.close()
    }

    @Test
    fun testThreadSupplementsAndInheritableThreadLocal() {
        val lockObj = Any()
        assertFalse(Thread.holdsLock(lockObj))
        synchronized(lockObj) {
            assertTrue(Thread.holdsLock(lockObj))
        }
        assertFalse(Thread.holdsLock(lockObj))

        val inheritable = object : InheritableThreadLocal<String>() {
            override fun childValue(parentValue: String?): String? = "$parentValue-child"
        }
        inheritable.set("root")
        assertEquals("root", inheritable.get())

        val childObserved = AtomicReference<String?>()
        val caughtError = AtomicReference<Throwable?>()
        val latch = CountDownLatch(1)

        val t = Thread(Runnable {
            childObserved.set(inheritable.get())
            throw IllegalStateException("boom")
        }, "inheritable-worker")
        assertEquals(Thread.State.NEW, t.state)
        t.priority = Thread.MAX_PRIORITY
        assertEquals(Thread.MAX_PRIORITY, t.priority)
        val ueh = Thread.UncaughtExceptionHandler { _, e ->
            caughtError.set(e)
            latch.countDown()
        }
        t.setUncaughtExceptionHandler(ueh)
        assertEquals(ueh, t.getUncaughtExceptionHandler())
        try {
            t.start()
            assertTrue(latch.await(5, TimeUnit.SECONDS))
            t.join(5000)
            assertEquals(Thread.State.TERMINATED, t.state)
            assertEquals("root-child", childObserved.get())
            assertEquals("boom", caughtError.get()?.message)
        } catch (uoe: UnsupportedOperationException) {
            assertTrue(uoe.message!!.contains("java.lang.Thread.start()"))
            try {
                t.run()
            } catch (ex: Throwable) {
                t.getUncaughtExceptionHandler()?.uncaughtException(t, ex)
            }
            assertEquals("boom", caughtError.get()?.message)
        }
        inheritable.remove()
    }

    @Test
    fun testStackTraceElementAndExceptions() {
        val ste = StackTraceElement("com.example.App", "run", "App.kt", 42)
        assertEquals("com.example.App", ste.className)
        assertEquals("run", ste.methodName)
        assertEquals("App.kt", ste.fileName)
        assertEquals(42, ste.lineNumber)
        assertFalse(ste.isNativeMethod)
        assertEquals("com.example.App.run(App.kt:42)", ste.toString())

        val nativeSte = StackTraceElement("com.example.Native", "call", null, -2)
        assertTrue(nativeSte.isNativeMethod)
        assertEquals("com.example.Native.call(Native Method)", nativeSte.toString())

        val sioobe = StringIndexOutOfBoundsException(9)
        assertTrue(sioobe.message!!.contains("9"))

        assertEquals("x", NoSuchFieldException("x").message)
        assertEquals("monitor", IllegalMonitorStateException("monitor").message)
        assertEquals("sec", SecurityException("sec").message)
        assertEquals("store", ArrayStoreException("store").message)
    }

    @Test
    fun testCharacterAndMath() {
        assertTrue(java.lang.Character.isDigit('5'))
        assertTrue(java.lang.Character.isDigit('5'.code))
        assertFalse(java.lang.Character.isDigit('a'))
        assertTrue(java.lang.Character.isLetter('Z'))
        assertTrue(java.lang.Character.isLetter('Z'.code))
        assertTrue(java.lang.Character.isLetterOrDigit('9'))
        assertTrue(java.lang.Character.isLetterOrDigit('k'.code))
        assertTrue(java.lang.Character.isWhitespace(' '))
        assertTrue(java.lang.Character.isWhitespace('\n'.code))
        assertTrue(java.lang.Character.isISOControl('\u0007'))
        assertTrue(java.lang.Character.isISOControl(0x7F))
        assertEquals('a', java.lang.Character.toLowerCase('A'))
        assertEquals('a'.code, java.lang.Character.toLowerCase('A'.code))
        assertEquals('Z', java.lang.Character.toUpperCase('z'))
        assertEquals('Z'.code, java.lang.Character.toUpperCase('z'.code))
        assertEquals(15, java.lang.Character.digit('f', 16))
        assertEquals(15, java.lang.Character.digit('F'.code, 16))
        assertEquals('f', java.lang.Character.forDigit(15, 16))
        assertEquals(1, java.lang.Character.charCount(0x41))
        assertEquals(2, java.lang.Character.charCount(0x1F600))
        val surrogatePair = java.lang.Character.toChars(0x1F600)
        assertEquals(2, surrogatePair.size)
        assertEquals(0x1F600, java.lang.Character.codePointAt(surrogatePair.concatToString(), 0))
        assertFailsWith<IllegalArgumentException> { java.lang.Character.toChars(-1) }

        assertTrue(java.lang.Math.PI > 3.14159 && java.lang.Math.PI < 3.14160)
        assertTrue(java.lang.Math.E > 2.71828 && java.lang.Math.E < 2.71829)
        assertEquals(3, java.lang.Math.min(3, 7))
        assertEquals(3L, java.lang.Math.min(3L, 7L))
        assertEquals(1.5f, java.lang.Math.min(1.5f, 2.5f))
        assertEquals(1.5, java.lang.Math.min(1.5, 2.5))
        assertEquals(7, java.lang.Math.max(3, 7))
        assertEquals(7L, java.lang.Math.max(3L, 7L))
        assertEquals(2.5f, java.lang.Math.max(1.5f, 2.5f))
        assertEquals(2.5, java.lang.Math.max(1.5, 2.5))
        assertEquals(42, java.lang.Math.abs(-42))
        assertEquals(42L, java.lang.Math.abs(-42L))
        assertEquals(3.5f, java.lang.Math.abs(-3.5f))
        assertEquals(3.5, java.lang.Math.abs(-3.5))
    }
}
