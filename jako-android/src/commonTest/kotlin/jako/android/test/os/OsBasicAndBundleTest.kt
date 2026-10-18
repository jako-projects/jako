package jako.android.test.os

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.PersistableBundle
import android.os.Process
import android.os.SystemClock
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsBasicAndBundleTest {

    @Test
    fun testSystemClockBuildAndProcess() {
        val t1 = SystemClock.uptimeMillis()
        val e1 = SystemClock.elapsedRealtime()
        val n1 = SystemClock.elapsedRealtimeNanos()
        SystemClock.sleep(5)
        val t2 = SystemClock.uptimeMillis()
        val n2 = SystemClock.elapsedRealtimeNanos()
        assertTrue(t2 >= t1)
        assertTrue(e1 > 0)
        assertTrue(n2 > n1)

        assertEquals(21, Build.VERSION_CODES.LOLLIPOP)
        assertEquals(29, Build.VERSION_CODES.Q)
        assertEquals("unknown", Build.UNKNOWN)

        assertTrue(Process.myPid() != 0)
        assertTrue(Process.myTid() != 0)
        assertTrue(Process.is64Bit())
        assertEquals(0, Process.THREAD_PRIORITY_DEFAULT)
    }

    @Test
    fun testBundleAndPersistableBundle() {
        val bundle = Bundle()
        assertTrue(bundle.isEmpty)
        assertEquals(0, bundle.size())

        bundle.putString("name", "Jako")
        bundle.putInt("version", 1)
        bundle.putBoolean("ready", true)
        bundle.putDouble("pi", 3.14159)
        bundle.putLong("timestamp", 1234567890L)
        bundle.putIntArray("nums", intArrayOf(1, 2, 3))
        bundle.putStringArrayList("tags", arrayListOf("kmp", "ios", "android"))

        val child = Bundle()
        child.putString("innerKey", "innerVal")
        bundle.putBundle("child", child)

        assertFalse(bundle.isEmpty)
        assertEquals(8, bundle.size())
        assertTrue(bundle.containsKey("name"))
        assertEquals("Jako", bundle.getString("name"))
        assertEquals("default", bundle.getString("missing", "default"))
        assertEquals(1, bundle.getInt("version"))
        assertEquals(42, bundle.getInt("missing", 42))
        assertTrue(bundle.getBoolean("ready"))
        assertEquals(3.14159, bundle.getDouble("pi"))
        assertEquals(1234567890L, bundle.getLong("timestamp"))
        assertContentEquals(intArrayOf(1, 2, 3), bundle.getIntArray("nums"))
        assertEquals<List<String>?>(listOf("kmp", "ios", "android"), bundle.getStringArrayList("tags"))
        assertEquals("innerVal", bundle.getBundle("child")?.getString("innerKey"))

        val copy = bundle.deepCopy()
        bundle.getIntArray("nums")!![0] = 99
        assertEquals(1, copy.getIntArray("nums")!![0])

        bundle.remove("version")
        assertFalse(bundle.containsKey("version"))

        val pb = PersistableBundle()
        pb.putString("k", "v")
        pb.putLong("l", 100L)
        assertEquals("v", pb.getString("k"))
        assertEquals(100L, pb.getLong("l"))
    }

    @Test
    fun testParcel() {
        val p = Parcel.obtain()
        p.writeInt(42)
        p.writeLong(9876543210L)
        p.writeFloat(2.5f)
        p.writeDouble(6.25)
        p.writeString("Hello Parcel")
        p.writeString(null)
        p.writeByteArray(byteArrayOf(10, 20, 30))
        p.writeIntArray(intArrayOf(7, 8, 9))

        val b = Bundle()
        b.putString("bundleKey", "bundleVal")
        b.putInt("bundleInt", 123)
        p.writeBundle(b)

        val bytes = p.marshall()
        p.recycle()

        val p2 = Parcel.obtain()
        p2.unmarshall(bytes, 0, bytes.size)
        assertEquals(42, p2.readInt())
        assertEquals(9876543210L, p2.readLong())
        assertEquals(2.5f, p2.readFloat())
        assertEquals(6.25, p2.readDouble())
        assertEquals("Hello Parcel", p2.readString())
        assertNull(p2.readString())
        assertContentEquals(byteArrayOf(10, 20, 30), p2.createByteArray())
        assertContentEquals(intArrayOf(7, 8, 9), p2.createIntArray())

        val readBundle = p2.readBundle()
        assertEquals("bundleVal", readBundle?.getString("bundleKey"))
        assertEquals(123, readBundle?.getInt("bundleInt"))
        p2.recycle()
    }
}
