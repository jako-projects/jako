package android.graphics

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Test-only JVM shims for [Matrix] and [Color] (whose AOSP `android-all` counterparts
 * invoke C++ libhwui JNI methods such as `Matrix.nCreate` and `Color.nativeRGBToHSV`).
 */
open class Matrix {
    private val mValues = FloatArray(9)

    constructor() {
        reset()
    }

    constructor(src: Matrix?) {
        if (src == null) {
            reset()
        } else {
            src.mValues.copyInto(mValues)
        }
    }

    open fun isIdentity(): Boolean {
        return mValues[0] == 1f && mValues[1] == 0f && mValues[2] == 0f &&
            mValues[3] == 0f && mValues[4] == 1f && mValues[5] == 0f &&
            mValues[6] == 0f && mValues[7] == 0f && mValues[8] == 1f
    }

    open fun isAffine(): Boolean {
        return mValues[6] == 0f && mValues[7] == 0f && mValues[8] == 1f
    }

    open fun rectStaysRect(): Boolean {
        if (!isAffine()) return false
        val m00 = mValues[0]
        val m01 = mValues[1]
        val m10 = mValues[3]
        val m11 = mValues[4]
        return (m00 == 0f && m11 == 0f && m01 != 0f && m10 != 0f) ||
            (m00 != 0f && m11 != 0f && m01 == 0f && m10 == 0f)
    }

    open fun set(src: Matrix?) {
        if (src == null) {
            reset()
        } else {
            src.mValues.copyInto(mValues)
        }
    }

    open fun reset() {
        mValues[0] = 1f; mValues[1] = 0f; mValues[2] = 0f
        mValues[3] = 0f; mValues[4] = 1f; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setTranslate(dx: Float, dy: Float) {
        mValues[0] = 1f; mValues[1] = 0f; mValues[2] = dx
        mValues[3] = 0f; mValues[4] = 1f; mValues[5] = dy
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setScale(sx: Float, sy: Float, px: Float, py: Float) {
        mValues[0] = sx; mValues[1] = 0f; mValues[2] = px - sx * px
        mValues[3] = 0f; mValues[4] = sy; mValues[5] = py - sy * py
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setScale(sx: Float, sy: Float) {
        mValues[0] = sx; mValues[1] = 0f; mValues[2] = 0f
        mValues[3] = 0f; mValues[4] = sy; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setRotate(degrees: Float, px: Float, py: Float) {
        val rad = degrees * (PI / 180.0)
        setSinCos(sin(rad).toFloat(), cos(rad).toFloat(), px, py)
    }

    open fun setRotate(degrees: Float) {
        val rad = degrees * (PI / 180.0)
        setSinCos(sin(rad).toFloat(), cos(rad).toFloat())
    }

    open fun setSinCos(sinValue: Float, cosValue: Float, px: Float, py: Float) {
        val oneMinusCos = 1f - cosValue
        mValues[0] = cosValue
        mValues[1] = -sinValue
        mValues[2] = sinValue * py + oneMinusCos * px
        mValues[3] = sinValue
        mValues[4] = cosValue
        mValues[5] = -sinValue * px + oneMinusCos * py
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setSinCos(sinValue: Float, cosValue: Float) {
        mValues[0] = cosValue; mValues[1] = -sinValue; mValues[2] = 0f
        mValues[3] = sinValue; mValues[4] = cosValue; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setSkew(kx: Float, ky: Float, px: Float, py: Float) {
        mValues[0] = 1f; mValues[1] = kx; mValues[2] = -kx * py
        mValues[3] = ky; mValues[4] = 1f; mValues[5] = -ky * px
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setSkew(kx: Float, ky: Float) {
        mValues[0] = 1f; mValues[1] = kx; mValues[2] = 0f
        mValues[3] = ky; mValues[4] = 1f; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    open fun setConcat(a: Matrix, b: Matrix): Boolean {
        val av = a.mValues
        val bv = b.mValues
        val r0 = av[0] * bv[0] + av[1] * bv[3] + av[2] * bv[6]
        val r1 = av[0] * bv[1] + av[1] * bv[4] + av[2] * bv[7]
        val r2 = av[0] * bv[2] + av[1] * bv[5] + av[2] * bv[8]
        val r3 = av[3] * bv[0] + av[4] * bv[3] + av[5] * bv[6]
        val r4 = av[3] * bv[1] + av[4] * bv[4] + av[5] * bv[7]
        val r5 = av[3] * bv[2] + av[4] * bv[5] + av[5] * bv[8]
        val r6 = av[6] * bv[0] + av[7] * bv[3] + av[8] * bv[6]
        val r7 = av[6] * bv[1] + av[7] * bv[4] + av[8] * bv[7]
        val r8 = av[6] * bv[2] + av[7] * bv[5] + av[8] * bv[8]
        mValues[0] = r0; mValues[1] = r1; mValues[2] = r2
        mValues[3] = r3; mValues[4] = r4; mValues[5] = r5
        mValues[6] = r6; mValues[7] = r7; mValues[8] = r8
        return true
    }

    open fun preTranslate(dx: Float, dy: Float): Boolean {
        val t = Matrix()
        t.setTranslate(dx, dy)
        return setConcat(this, t)
    }

    open fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy, px, py)
        return setConcat(this, t)
    }

    open fun preScale(sx: Float, sy: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy)
        return setConcat(this, t)
    }

    open fun preRotate(degrees: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees, px, py)
        return setConcat(this, t)
    }

    open fun preRotate(degrees: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees)
        return setConcat(this, t)
    }

    open fun preSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky, px, py)
        return setConcat(this, t)
    }

    open fun preSkew(kx: Float, ky: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky)
        return setConcat(this, t)
    }

    open fun preConcat(other: Matrix): Boolean = setConcat(this, other)

    open fun postTranslate(dx: Float, dy: Float): Boolean {
        val t = Matrix()
        t.setTranslate(dx, dy)
        return setConcat(t, this)
    }

    open fun postScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy, px, py)
        return setConcat(t, this)
    }

    open fun postScale(sx: Float, sy: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy)
        return setConcat(t, this)
    }

    open fun postRotate(degrees: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees, px, py)
        return setConcat(t, this)
    }

    open fun postRotate(degrees: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees)
        return setConcat(t, this)
    }

    open fun postSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky, px, py)
        return setConcat(t, this)
    }

    open fun postSkew(kx: Float, ky: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky)
        return setConcat(t, this)
    }

    open fun postConcat(other: Matrix): Boolean = setConcat(other, this)

    open fun setRectToRect(src: RectF, dst: RectF, stf: ScaleToFit): Boolean {
        if (src.isEmpty) {
            reset()
            return false
        }
        if (dst.isEmpty) {
            mValues.fill(0f)
            mValues[8] = 1f
        } else {
            var tx: Float
            var sx = dst.width() / src.width()
            var sy = dst.height() / src.height()
            var xLarger = false

            if (stf != ScaleToFit.FILL) {
                if (sx > sy) {
                    xLarger = true
                    sx = sy
                } else {
                    sy = sx
                }
            }

            tx = dst.left - src.left * sx
            var ty = dst.top - src.top * sy
            if (stf == ScaleToFit.CENTER || stf == ScaleToFit.END) {
                var diff = if (xLarger) dst.width() - src.width() * sy else dst.height() - src.height() * sy
                if (stf == ScaleToFit.CENTER) {
                    diff *= 0.5f
                }
                if (xLarger) tx += diff else ty += diff
            }
            setScale(sx, sy)
            mValues[MTRANS_X] = tx
            mValues[MTRANS_Y] = ty
        }
        return true
    }

    open fun invert(inverse: Matrix?): Boolean {
        val a00 = mValues[0].toDouble()
        val a01 = mValues[1].toDouble()
        val a02 = mValues[2].toDouble()
        val a10 = mValues[3].toDouble()
        val a11 = mValues[4].toDouble()
        val a12 = mValues[5].toDouble()
        val a20 = mValues[6].toDouble()
        val a21 = mValues[7].toDouble()
        val a22 = mValues[8].toDouble()

        val c00 = a11 * a22 - a12 * a21
        val c01 = -(a10 * a22 - a12 * a20)
        val c02 = a10 * a21 - a11 * a20

        val det = a00 * c00 + a01 * c01 + a02 * c02
        if (det == 0.0) return false
        val invDet = 1.0 / det

        if (inverse != null) {
            val c10 = -(a01 * a22 - a02 * a21)
            val c11 = a00 * a22 - a02 * a20
            val c12 = -(a00 * a21 - a01 * a20)
            val c20 = a01 * a12 - a02 * a11
            val c21 = -(a00 * a12 - a02 * a10)
            val c22 = a00 * a11 - a01 * a10

            inverse.mValues[0] = (c00 * invDet).toFloat()
            inverse.mValues[1] = (c10 * invDet).toFloat()
            inverse.mValues[2] = (c20 * invDet).toFloat()
            inverse.mValues[3] = (c01 * invDet).toFloat()
            inverse.mValues[4] = (c11 * invDet).toFloat()
            inverse.mValues[5] = (c21 * invDet).toFloat()
            inverse.mValues[6] = (c02 * invDet).toFloat()
            inverse.mValues[7] = (c12 * invDet).toFloat()
            inverse.mValues[8] = (c22 * invDet).toFloat()
        }
        return true
    }

    open fun mapPoints(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, pointCount: Int) {
        val temp = FloatArray(pointCount * 2)
        for (i in 0 until pointCount) {
            val x = src[srcIndex + i * 2]
            val y = src[srcIndex + i * 2 + 1]
            var rx = mValues[0] * x + mValues[1] * y + mValues[2]
            var ry = mValues[3] * x + mValues[4] * y + mValues[5]
            if (!isAffine()) {
                val w = mValues[6] * x + mValues[7] * y + mValues[8]
                if (w != 0f) {
                    rx /= w
                    ry /= w
                }
            }
            temp[i * 2] = rx
            temp[i * 2 + 1] = ry
        }
        temp.copyInto(dst, destinationOffset = dstIndex)
    }

    open fun mapVectors(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, vectorCount: Int) {
        val temp = FloatArray(vectorCount * 2)
        for (i in 0 until vectorCount) {
            val x = src[srcIndex + i * 2]
            val y = src[srcIndex + i * 2 + 1]
            temp[i * 2] = mValues[0] * x + mValues[1] * y
            temp[i * 2 + 1] = mValues[3] * x + mValues[4] * y
        }
        temp.copyInto(dst, destinationOffset = dstIndex)
    }

    open fun mapPoints(dst: FloatArray, src: FloatArray) {
        mapPoints(dst, 0, src, 0, dst.size shr 1)
    }

    open fun mapVectors(dst: FloatArray, src: FloatArray) {
        mapVectors(dst, 0, src, 0, dst.size shr 1)
    }

    open fun mapPoints(pts: FloatArray) {
        mapPoints(pts, 0, pts, 0, pts.size shr 1)
    }

    open fun mapVectors(vecs: FloatArray) {
        mapVectors(vecs, 0, vecs, 0, vecs.size shr 1)
    }

    open fun mapRect(dst: RectF, src: RectF): Boolean {
        val pts = floatArrayOf(
            src.left, src.top,
            src.right, src.top,
            src.right, src.bottom,
            src.left, src.bottom
        )
        mapPoints(pts)
        var minX = pts[0]
        var maxX = pts[0]
        var minY = pts[1]
        var maxY = pts[1]
        for (i in 1 until 4) {
            val x = pts[i * 2]
            val y = pts[i * 2 + 1]
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
        dst.set(minX, minY, maxX, maxY)
        return rectStaysRect()
    }

    open fun mapRect(rect: RectF): Boolean = mapRect(rect, rect)

    open fun mapRadius(radius: Float): Float {
        val vecs = floatArrayOf(radius, 0f, 0f, radius)
        mapVectors(vecs)
        val d1 = hypot(vecs[0].toDouble(), vecs[1].toDouble())
        val d2 = hypot(vecs[2].toDouble(), vecs[3].toDouble())
        return sqrt(d1 * d2).toFloat()
    }

    open fun getValues(values: FloatArray) {
        mValues.copyInto(values, endIndex = 9)
    }

    open fun setValues(values: FloatArray) {
        values.copyInto(mValues, endIndex = 9)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        return mValues.contentEquals(other.mValues)
    }

    override fun hashCode(): Int = mValues.contentHashCode()

    override fun toString(): String = "Matrix{${toShortString()}}"

    open fun toShortString(): String {
        return "[${mValues[0]}, ${mValues[1]}, ${mValues[2]}][${mValues[3]}, ${mValues[4]}, ${mValues[5]}][${mValues[6]}, ${mValues[7]}, ${mValues[8]}]"
    }

    enum class ScaleToFit {
        FILL,
        START,
        CENTER,
        END
    }

    companion object {
        const val MSCALE_X: Int = 0
        const val MSKEW_X: Int = 1
        const val MTRANS_X: Int = 2
        const val MSKEW_Y: Int = 3
        const val MSCALE_Y: Int = 4
        const val MTRANS_Y: Int = 5
        const val MPERSP_0: Int = 6
        const val MPERSP_1: Int = 7
        const val MPERSP_2: Int = 8
    }
}

open class Color {
    companion object {
        const val BLACK: Int = -0x1000000
        const val DKGRAY: Int = -0xbbbbbc
        const val GRAY: Int = -0x777778
        const val LTGRAY: Int = -0x333334
        const val WHITE: Int = -0x1
        const val RED: Int = -0x10000
        const val GREEN: Int = -0xff0100
        const val BLUE: Int = -0xffff01
        const val YELLOW: Int = -0x100
        const val CYAN: Int = -0xff0001
        const val MAGENTA: Int = -0xff01
        const val TRANSPARENT: Int = 0

        private val sColorNameMap = mapOf(
            "black" to BLACK,
            "darkgray" to DKGRAY,
            "gray" to GRAY,
            "lightgray" to LTGRAY,
            "white" to WHITE,
            "red" to RED,
            "green" to GREEN,
            "blue" to BLUE,
            "yellow" to YELLOW,
            "cyan" to CYAN,
            "magenta" to MAGENTA,
            "aqua" to -0xff0001,
            "fuchsia" to -0xff01,
            "darkgrey" to DKGRAY,
            "grey" to GRAY,
            "lightgrey" to LTGRAY,
            "lime" to -0xff0100,
            "maroon" to -0x800000,
            "navy" to -0xffff80,
            "olive" to -0x7f8000,
            "purple" to -0x7fff80,
            "silver" to -0x3f3f40,
            "teal" to -0xff7f80
        )

        @JvmStatic
        fun alpha(color: Int): Int = color ushr 24

        @JvmStatic
        fun red(color: Int): Int = (color shr 16) and 0xFF

        @JvmStatic
        fun green(color: Int): Int = (color shr 8) and 0xFF

        @JvmStatic
        fun blue(color: Int): Int = color and 0xFF

        @JvmStatic
        fun rgb(red: Int, green: Int, blue: Int): Int {
            return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        }

        @JvmStatic
        fun rgb(red: Float, green: Float, blue: Float): Int {
            return (0xFF shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        @JvmStatic
        fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
            return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

        @JvmStatic
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
            return (((alpha * 255.0f + 0.5f).toInt()) shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        @JvmStatic
        fun parseColor(colorString: String): Int {
            if (colorString.isEmpty()) throw IllegalArgumentException("Unknown color")
            if (colorString[0] == '#') {
                var color = colorString.substring(1).toLongOrNull(16)
                    ?: throw IllegalArgumentException("Unknown color")
                if (colorString.length == 7) {
                    color = color or -0x1000000L
                } else if (colorString.length != 9) {
                    throw IllegalArgumentException("Unknown color")
                }
                return color.toInt()
            } else {
                val color = sColorNameMap[colorString.lowercase()]
                if (color != null) return color
            }
            throw IllegalArgumentException("Unknown color")
        }

        @JvmStatic
        fun RGBToHSV(red: Int, green: Int, blue: Int, hsv: FloatArray) {
            val rf = red / 255f
            val gf = green / 255f
            val bf = blue / 255f
            val max = maxOf(rf, gf, bf)
            val min = minOf(rf, gf, bf)
            val delta = max - min
            val v = max
            val s = if (max != 0f) delta / max else 0f
            var h = 0f
            if (s != 0f) {
                h = when (max) {
                    rf -> (gf - bf) / delta
                    gf -> 2f + (bf - rf) / delta
                    else -> 4f + (rf - gf) / delta
                }
                h *= 60f
                if (h < 0f) h += 360f
            }
            hsv[0] = h
            hsv[1] = s
            hsv[2] = v
        }

        @JvmStatic
        fun colorToHSV(color: Int, hsv: FloatArray) {
            RGBToHSV((color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF, hsv)
        }

        @JvmStatic
        fun HSVToColor(hsv: FloatArray): Int = HSVToColor(0xFF, hsv)

        @JvmStatic
        fun HSVToColor(alpha: Int, hsv: FloatArray): Int {
            val h = ((hsv[0] % 360f) + 360f) % 360f
            val s = hsv[1].coerceIn(0f, 1f)
            val v = hsv[2].coerceIn(0f, 1f)
            val c = v * s
            val x = c * (1f - abs((h / 60f) % 2f - 1f))
            val m = v - c
            val (r1, g1, b1) = when {
                h < 60f -> Triple(c, x, 0f)
                h < 120f -> Triple(x, c, 0f)
                h < 180f -> Triple(0f, c, x)
                h < 240f -> Triple(0f, x, c)
                h < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val r = ((r1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            val g = ((g1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            val b = ((b1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            return argb(alpha, r, g, b)
        }
    }
}
