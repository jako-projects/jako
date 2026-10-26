package jako.android.test.os

import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.PersistableBundle
import android.os.Process
import android.os.SystemClock
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
        assertTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        assertTrue(Build.VERSION.RELEASE.isNotEmpty())
        assertTrue(Build.VERSION.SDK.isNotEmpty())
        assertTrue(Build.VERSION.CODENAME.isNotEmpty())
        assertNotNull(Build.VERSION.INCREMENTAL)

        assertTrue(Process.myPid() != 0)
        assertTrue(Process.myTid() != 0)
        assertTrue(Process.is64Bit())
        assertEquals(0, Process.THREAD_PRIORITY_DEFAULT)
    }

    @Test
    fun testBundleAndPersistableBundle() {
        val bundle = Bundle()
        val baseBundle: BaseBundle = bundle
        assertTrue(baseBundle.isEmpty)
        assertEquals(0, baseBundle.size())

        baseBundle.putString("name", "Jako")
        baseBundle.putInt("version", 1)
        baseBundle.putBoolean("ready", true)
        baseBundle.putDouble("pi", 3.14159)
        baseBundle.putLong("timestamp", 1234567890L)
        baseBundle.putIntArray("nums", intArrayOf(1, 2, 3))
        baseBundle.putBooleanArray("bools", booleanArrayOf(true, false))
        baseBundle.putLongArray("longs", longArrayOf(10L, 20L))
        baseBundle.putDoubleArray("doubles", doubleArrayOf(1.5, 2.5))
        baseBundle.putStringArray("strs", arrayOf("x", "y"))
        bundle.putStringArrayList("tags", arrayListOf("kmp", "ios", "android"))

        val child = Bundle()
        child.putString("innerKey", "innerVal")
        bundle.putBundle("child", child)

        assertFalse(baseBundle.isEmpty)
        assertEquals(12, baseBundle.size())
        assertTrue(baseBundle.containsKey("name"))
        assertEquals("Jako", baseBundle.get("name"))
        assertTrue(baseBundle.keySet().contains("name"))
        assertEquals("Jako", baseBundle.getString("name"))
        assertEquals("default", baseBundle.getString("missing", "default"))
        assertEquals(1, baseBundle.getInt("version"))
        assertEquals(42, baseBundle.getInt("missing", 42))
        assertTrue(baseBundle.getBoolean("ready"))
        assertEquals(3.14159, baseBundle.getDouble("pi"))
        assertEquals(1234567890L, baseBundle.getLong("timestamp"))
        assertContentEquals(intArrayOf(1, 2, 3), baseBundle.getIntArray("nums"))
        assertContentEquals(booleanArrayOf(true, false), baseBundle.getBooleanArray("bools"))
        assertContentEquals(longArrayOf(10L, 20L), baseBundle.getLongArray("longs"))
        assertContentEquals(doubleArrayOf(1.5, 2.5), baseBundle.getDoubleArray("doubles"))
        assertContentEquals(arrayOf<String?>("x", "y"), baseBundle.getStringArray("strs"))
        assertEquals<List<String>?>(listOf("kmp", "ios", "android"), bundle.getStringArrayList("tags"))
        assertEquals("innerVal", bundle.getBundle("child")?.getString("innerKey"))

        val copy = bundle.deepCopy()
        bundle.getIntArray("nums")!![0] = 99
        assertEquals(1, copy.getIntArray("nums")!![0])

        baseBundle.remove("version")
        assertFalse(baseBundle.containsKey("version"))

        val pb = PersistableBundle()
        pb.putString("k", "v")
        pb.putLong("l", 100L)
        assertEquals("v", pb.getString("k"))
        assertEquals(100L, pb.getLong("l"))
    }

    @Test
    fun testParcelAndCreator() {
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

        // Test Parcelable.Creator explicitly
        val creator: Parcelable.Creator<TagItem> = object : Parcelable.Creator<TagItem> {
            override fun createFromParcel(source: Parcel): TagItem {
                return TagItem(source.readInt(), source.readString() ?: "")
            }

            override fun newArray(size: Int): Array<TagItem?> = arrayOfNulls(size)
        }
        val arr = creator.newArray(3)
        assertEquals(3, arr.size)
        val p3 = Parcel.obtain()
        p3.writeInt(77)
        p3.writeString("CreatorTag")
        p3.setDataPosition(0)
        val createdTag = creator.createFromParcel(p3)
        p3.recycle()
        assertEquals(TagItem(77, "CreatorTag"), createdTag)
    }
}
