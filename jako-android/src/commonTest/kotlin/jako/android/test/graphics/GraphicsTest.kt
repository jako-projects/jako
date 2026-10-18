package jako.android.test.graphics

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcel
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphicsTest {
    @Test
    fun testPointAndPointF() {
        val p = Point(3, 4)
        assertEquals(3, p.x)
        assertEquals(4, p.y)
        assertTrue(p.equals(3, 4))
        assertEquals(Point(3, 4), p)

        p.offset(2, -1)
        assertEquals(Point(5, 3), p)
        p.negate()
        assertEquals(Point(-5, -3), p)

        val parcel = Parcel.obtain()
        p.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val p2 = Point()
        p2.readFromParcel(parcel)
        assertEquals(p, p2)
        parcel.recycle()

        val pf = PointF(3f, 4f)
        assertEquals(5f, pf.length(), 1e-4f)
        assertEquals(5f, PointF.length(3f, 4f), 1e-4f)
        pf.offset(1f, 2f)
        assertEquals(PointF(4f, 6f), pf)
        pf.negate()
        assertEquals(PointF(-4f, -6f), pf)
    }

    @Test
    fun testRect() {
        val r = Rect(10, 20, 50, 80)
        assertFalse(r.isEmpty())
        assertEquals(40, r.width())
        assertEquals(60, r.height())
        assertEquals(30, r.centerX())
        assertEquals(50, r.centerY())
        assertEquals(30f, r.exactCenterX())
        assertEquals(50f, r.exactCenterY())

        assertTrue(r.contains(10, 20))
        assertTrue(r.contains(49, 79))
        assertFalse(r.contains(50, 80))
        assertTrue(r.contains(Rect(15, 25, 45, 75)))

        val copy = Rect(r)
        copy.offset(5, 10)
        assertEquals(Rect(15, 30, 55, 90), copy)

        copy.offsetTo(0, 0)
        assertEquals(Rect(0, 0, 40, 60), copy)

        copy.inset(5, 10)
        assertEquals(Rect(5, 10, 35, 50), copy)

        val a = Rect(0, 0, 20, 20)
        val b = Rect(10, 10, 30, 30)
        assertTrue(Rect.intersects(a, b))
        val inter = Rect()
        assertTrue(inter.setIntersect(a, b))
        assertEquals(Rect(10, 10, 20, 20), inter)

        val u = Rect(0, 0, 10, 10)
        u.union(20, 20, 30, 30)
        assertEquals(Rect(0, 0, 30, 30), u)

        val flat = r.flattenToString()
        val unflat = Rect.unflattenFromString(flat)
        assertNotNull(unflat)
        assertEquals(r, unflat)
        assertNull(Rect.unflattenFromString("invalid"))
    }

    @Test
    fun testRectF() {
        val rf = RectF(10.2f, 20.4f, 50.6f, 80.8f)
        assertFalse(rf.isEmpty())
        assertEquals(40.4f, rf.width(), 1e-4f)
        assertEquals(60.4f, rf.height(), 1e-4f)

        val rounded = Rect()
        rf.round(rounded)
        assertEquals(Rect(10, 20, 51, 81), rounded)

        val roundOut = Rect()
        rf.roundOut(roundOut)
        assertEquals(Rect(10, 20, 51, 81), roundOut)

        assertTrue(rf.contains(15f, 25f))
        val inter = RectF(rf)
        assertTrue(inter.intersect(20f, 30f, 40f, 50f))
        assertEquals(RectF(20f, 30f, 40f, 50f), inter)
    }

    @Test
    fun testMatrix() {
        val m = Matrix()
        assertTrue(m.isIdentity())
        assertTrue(m.isAffine())
        assertTrue(m.rectStaysRect())

        m.setTranslate(10f, 20f)
        assertFalse(m.isIdentity())
        val pts = floatArrayOf(1f, 2f, 3f, 4f)
        m.mapPoints(pts)
        assertEquals(11f, pts[0], 1e-4f)
        assertEquals(22f, pts[1], 1e-4f)
        assertEquals(13f, pts[2], 1e-4f)
        assertEquals(24f, pts[3], 1e-4f)

        // preScale: M' = M * S -> first scale by (2, 3), then translate by (10, 20)
        m.preScale(2f, 3f)
        val p2 = floatArrayOf(5f, 6f)
        m.mapPoints(p2)
        assertEquals(20f, p2[0], 1e-4f) // 5 * 2 + 10
        assertEquals(38f, p2[1], 1e-4f) // 6 * 3 + 20

        // invert
        val inv = Matrix()
        assertTrue(m.invert(inv))
        inv.mapPoints(p2)
        assertEquals(5f, p2[0], 1e-4f)
        assertEquals(6f, p2[1], 1e-4f)

        // mapRect & setRectToRect
        val src = RectF(0f, 0f, 100f, 50f)
        val dst = RectF(0f, 0f, 200f, 200f)
        val fitMatrix = Matrix()
        assertTrue(fitMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER))
        val mapped = RectF()
        fitMatrix.mapRect(mapped, src)
        assertEquals(0f, mapped.left, 1e-4f)
        assertEquals(50f, mapped.top, 1e-4f)
        assertEquals(200f, mapped.right, 1e-4f)
        assertEquals(150f, mapped.bottom, 1e-4f)
    }

    @Test
    fun testColor() {
        val c = Color.argb(0x80, 0x12, 0x34, 0x56)
        assertEquals(0x80, Color.alpha(c))
        assertEquals(0x12, Color.red(c))
        assertEquals(0x34, Color.green(c))
        assertEquals(0x56, Color.blue(c))

        assertEquals(Color.RED, Color.parseColor("#FF0000"))
        assertEquals(Color.RED, Color.parseColor("#FFFF0000"))
        assertEquals(Color.BLUE, Color.parseColor("blue"))

        val hsv = FloatArray(3)
        Color.colorToHSV(Color.RED, hsv)
        assertEquals(0f, hsv[0], 1e-3f)
        assertEquals(1f, hsv[1], 1e-3f)
        assertEquals(1f, hsv[2], 1e-3f)

        val backToRed = Color.HSVToColor(hsv)
        assertEquals(Color.RED, backToRed)
        assertTrue(abs(Color.green(backToRed)) == 0)
    }
}
