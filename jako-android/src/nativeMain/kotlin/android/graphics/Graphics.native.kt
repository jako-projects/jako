package android.graphics

import android.os.Parcel
import android.os.Parcelable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

actual open class Point : Parcelable {
    actual var x: Int = 0
    actual var y: Int = 0

    actual constructor()

    actual constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    actual constructor(src: Point) {
        this.x = src.x
        this.y = src.y
    }

    actual open fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    actual final fun negate() {
        x = -x
        y = -y
    }

    actual final fun offset(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    actual final fun equals(x: Int, y: Int): Boolean = this.x == x && this.y == y

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Point
        return x == other.x && y == other.y
    }

    actual override fun hashCode(): Int = 31 * x + y

    actual override fun toString(): String = "Point($x, $y)"

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(x)
        dest.writeInt(y)
    }

    actual open fun readFromParcel(`in`: Parcel) {
        x = `in`.readInt()
        y = `in`.readInt()
    }
}

actual open class PointF : Parcelable {
    actual var x: Float = 0f
    actual var y: Float = 0f

    actual constructor()

    actual constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    actual constructor(p: Point) {
        this.x = p.x.toFloat()
        this.y = p.y.toFloat()
    }

    actual final fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    actual final fun set(p: PointF) {
        this.x = p.x
        this.y = p.y
    }

    actual final fun negate() {
        x = -x
        y = -y
    }

    actual final fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    actual final fun equals(x: Float, y: Float): Boolean = this.x == x && this.y == y

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PointF
        return other.x.compareTo(x) == 0 && other.y.compareTo(y) == 0
    }

    actual override fun hashCode(): Int {
        var result = if (x != 0.0f) x.toBits() else 0
        result = 31 * result + if (y != 0.0f) y.toBits() else 0
        return result
    }

    actual override fun toString(): String = "PointF($x, $y)"

    actual final fun length(): Float = length(x, y)

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(x)
        dest.writeFloat(y)
    }

    actual open fun readFromParcel(`in`: Parcel) {
        x = `in`.readFloat()
        y = `in`.readFloat()
    }

    actual companion object {
        actual fun length(x: Float, y: Float): Float = hypot(x.toDouble(), y.toDouble()).toFloat()
    }
}

actual final class Rect : Parcelable {
    actual var left: Int = 0
    actual var top: Int = 0
    actual var right: Int = 0
    actual var bottom: Int = 0

    actual val isEmpty: Boolean
        get() = left >= right || top >= bottom

    actual constructor()

    actual constructor(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual constructor(r: Rect?) {
        if (r == null) {
            bottom = 0
            right = 0
            top = 0
            left = 0
        } else {
            left = r.left
            top = r.top
            right = r.right
            bottom = r.bottom
        }
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Rect
        return left == other.left && top == other.top && right == other.right && bottom == other.bottom
    }

    actual override fun hashCode(): Int {
        var result = left
        result = 31 * result + top
        result = 31 * result + right
        result = 31 * result + bottom
        return result
    }

    actual override fun toString(): String = "Rect($left, $top - $right, $bottom)"

    actual fun toShortString(): String = "[$left,$top][$right,$bottom]"

    actual fun flattenToString(): String = "$left $top $right $bottom"

    actual fun isEmpty(): Boolean = left >= right || top >= bottom

    actual fun width(): Int = right - left

    actual fun height(): Int = bottom - top

    actual fun centerX(): Int = (left + right) shr 1

    actual fun centerY(): Int = (top + bottom) shr 1

    actual fun exactCenterX(): Float = (left + right) * 0.5f

    actual fun exactCenterY(): Float = (top + bottom) * 0.5f

    actual fun setEmpty() {
        bottom = 0
        top = 0
        right = 0
        left = 0
    }

    actual fun set(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual fun set(src: Rect) {
        this.left = src.left
        this.top = src.top
        this.right = src.right
        this.bottom = src.bottom
    }

    actual fun offset(dx: Int, dy: Int) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    actual fun offsetTo(newLeft: Int, newTop: Int) {
        right += newLeft - left
        bottom += newTop - top
        left = newLeft
        top = newTop
    }

    actual fun inset(dx: Int, dy: Int) {
        left += dx
        top += dy
        right -= dx
        bottom -= dy
    }

    actual fun contains(x: Int, y: Int): Boolean {
        return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom
    }

    actual fun contains(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return this.left < this.right && this.top < this.bottom &&
            this.left <= left && this.top <= top &&
            this.right >= right && this.bottom >= bottom
    }

    actual fun contains(r: Rect): Boolean {
        return this.left < this.right && this.top < this.bottom &&
            left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom
    }

    actual fun intersect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        if (this.left < right && left < this.right && this.top < bottom && top < this.bottom) {
            if (this.left < left) this.left = left
            if (this.top < top) this.top = top
            if (this.right > right) this.right = right
            if (this.bottom > bottom) this.bottom = bottom
            return true
        }
        return false
    }

    actual fun intersect(r: Rect): Boolean = intersect(r.left, r.top, r.right, r.bottom)

    actual fun setIntersect(a: Rect, b: Rect): Boolean {
        if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
            left = max(a.left, b.left)
            top = max(a.top, b.top)
            right = min(a.right, b.right)
            bottom = min(a.bottom, b.bottom)
            return true
        }
        return false
    }

    actual fun intersects(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return this.left < right && left < this.right && this.top < bottom && top < this.bottom
    }

    actual fun union(left: Int, top: Int, right: Int, bottom: Int) {
        if (left < right && top < bottom) {
            if (this.left < this.right && this.top < this.bottom) {
                if (this.left > left) this.left = left
                if (this.top > top) this.top = top
                if (this.right < right) this.right = right
                if (this.bottom < bottom) this.bottom = bottom
            } else {
                this.left = left
                this.top = top
                this.right = right
                this.bottom = bottom
            }
        }
    }

    actual fun union(r: Rect) {
        union(r.left, r.top, r.right, r.bottom)
    }

    actual fun union(x: Int, y: Int) {
        if (x < left) {
            left = x
        } else if (x > right) {
            right = x
        }
        if (y < top) {
            top = y
        } else if (y > bottom) {
            bottom = y
        }
    }

    actual fun sort() {
        if (left > right) {
            val temp = left
            left = right
            right = temp
        }
        if (top > bottom) {
            val temp = top
            top = bottom
            bottom = temp
        }
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(left)
        dest.writeInt(top)
        dest.writeInt(right)
        dest.writeInt(bottom)
    }

    actual fun readFromParcel(`in`: Parcel) {
        left = `in`.readInt()
        top = `in`.readInt()
        right = `in`.readInt()
        bottom = `in`.readInt()
    }

    actual companion object {
        actual fun intersects(a: Rect, b: Rect): Boolean {
            return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
        }

        actual fun unflattenFromString(str: String?): Rect? {
            if (str.isNullOrEmpty()) return null
            val parts = str.trim().split(Regex("\\s+"))
            if (parts.size != 4) return null
            return try {
                Rect(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
            } catch (_: NumberFormatException) {
                null
            }
        }
    }
}

actual open class RectF : Parcelable {
    actual var left: Float = 0f
    actual var top: Float = 0f
    actual var right: Float = 0f
    actual var bottom: Float = 0f

    actual val isEmpty: Boolean
        get() = left >= right || top >= bottom

    actual constructor()

    actual constructor(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual constructor(r: RectF?) {
        if (r == null) {
            bottom = 0f
            right = 0f
            top = 0f
            left = 0f
        } else {
            left = r.left
            top = r.top
            right = r.right
            bottom = r.bottom
        }
    }

    actual constructor(r: Rect?) {
        if (r == null) {
            bottom = 0f
            right = 0f
            top = 0f
            left = 0f
        } else {
            left = r.left.toFloat()
            top = r.top.toFloat()
            right = r.right.toFloat()
            bottom = r.bottom.toFloat()
        }
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as RectF
        return left == other.left && top == other.top && right == other.right && bottom == other.bottom
    }

    actual override fun hashCode(): Int {
        var result = if (left != 0.0f) left.toBits() else 0
        result = 31 * result + if (top != 0.0f) top.toBits() else 0
        result = 31 * result + if (right != 0.0f) right.toBits() else 0
        result = 31 * result + if (bottom != 0.0f) bottom.toBits() else 0
        return result
    }

    actual override fun toString(): String = "RectF($left, $top, $right, $bottom)"

    actual final fun toShortString(): String = "[$left,$top][$right,$bottom]"

    actual final fun isEmpty(): Boolean = left >= right || top >= bottom

    actual final fun width(): Float = right - left

    actual final fun height(): Float = bottom - top

    actual final fun centerX(): Float = (left + right) * 0.5f

    actual final fun centerY(): Float = (top + bottom) * 0.5f

    actual final fun setEmpty() {
        bottom = 0f
        top = 0f
        right = 0f
        left = 0f
    }

    actual open fun set(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual open fun set(src: RectF) {
        this.left = src.left
        this.top = src.top
        this.right = src.right
        this.bottom = src.bottom
    }

    actual open fun set(src: Rect) {
        this.left = src.left.toFloat()
        this.top = src.top.toFloat()
        this.right = src.right.toFloat()
        this.bottom = src.bottom.toFloat()
    }

    actual final fun offset(dx: Float, dy: Float) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    actual final fun offsetTo(newLeft: Float, newTop: Float) {
        right += newLeft - left
        bottom += newTop - top
        left = newLeft
        top = newTop
    }

    actual final fun inset(dx: Float, dy: Float) {
        left += dx
        top += dy
        right -= dx
        bottom -= dy
    }

    actual final fun contains(x: Float, y: Float): Boolean {
        return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom
    }

    actual final fun contains(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return this.left < this.right && this.top < this.bottom &&
            this.left <= left && this.top <= top &&
            this.right >= right && this.bottom >= bottom
    }

    actual final fun contains(r: RectF): Boolean {
        return this.left < this.right && this.top < this.bottom &&
            left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom
    }

    actual final fun intersect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        if (this.left < right && left < this.right && this.top < bottom && top < this.bottom) {
            if (this.left < left) this.left = left
            if (this.top < top) this.top = top
            if (this.right > right) this.right = right
            if (this.bottom > bottom) this.bottom = bottom
            return true
        }
        return false
    }

    actual final fun intersect(r: RectF): Boolean = intersect(r.left, r.top, r.right, r.bottom)

    actual final fun setIntersect(a: RectF, b: RectF): Boolean {
        if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
            left = max(a.left, b.left)
            top = max(a.top, b.top)
            right = min(a.right, b.right)
            bottom = min(a.bottom, b.bottom)
            return true
        }
        return false
    }

    actual final fun intersects(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return this.left < right && left < this.right && this.top < bottom && top < this.bottom
    }

    actual final fun round(dst: Rect) {
        dst.set(
            floor(left + 0.5f).toInt(),
            floor(top + 0.5f).toInt(),
            floor(right + 0.5f).toInt(),
            floor(bottom + 0.5f).toInt()
        )
    }

    actual final fun roundOut(dst: Rect) {
        dst.set(
            floor(left).toInt(),
            floor(top).toInt(),
            ceil(right).toInt(),
            ceil(bottom).toInt()
        )
    }

    actual final fun union(left: Float, top: Float, right: Float, bottom: Float) {
        if (left < right && top < bottom) {
            if (this.left < this.right && this.top < this.bottom) {
                if (this.left > left) this.left = left
                if (this.top > top) this.top = top
                if (this.right < right) this.right = right
                if (this.bottom < bottom) this.bottom = bottom
            } else {
                this.left = left
                this.top = top
                this.right = right
                this.bottom = bottom
            }
        }
    }

    actual final fun union(r: RectF) {
        union(r.left, r.top, r.right, r.bottom)
    }

    actual final fun union(x: Float, y: Float) {
        if (x < left) {
            left = x
        } else if (x > right) {
            right = x
        }
        if (y < top) {
            top = y
        } else if (y > bottom) {
            bottom = y
        }
    }

    actual final fun sort() {
        if (left > right) {
            val temp = left
            left = right
            right = temp
        }
        if (top > bottom) {
            val temp = top
            top = bottom
            bottom = temp
        }
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(left)
        dest.writeFloat(top)
        dest.writeFloat(right)
        dest.writeFloat(bottom)
    }

    actual open fun readFromParcel(`in`: Parcel) {
        left = `in`.readFloat()
        top = `in`.readFloat()
        right = `in`.readFloat()
        bottom = `in`.readFloat()
    }

    actual companion object {
        actual fun intersects(a: RectF, b: RectF): Boolean {
            return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
        }
    }
}

actual open class Matrix {
    private val mValues = FloatArray(9)

    actual val isIdentity: Boolean
        get() = isIdentity()

    actual val isAffine: Boolean
        get() = isAffine()

    actual constructor() {
        reset()
    }

    actual constructor(src: Matrix?) {
        if (src == null) {
            reset()
        } else {
            src.mValues.copyInto(mValues)
        }
    }

    actual open fun isIdentity(): Boolean {
        return mValues[0] == 1f && mValues[1] == 0f && mValues[2] == 0f &&
            mValues[3] == 0f && mValues[4] == 1f && mValues[5] == 0f &&
            mValues[6] == 0f && mValues[7] == 0f && mValues[8] == 1f
    }

    actual open fun isAffine(): Boolean {
        return mValues[6] == 0f && mValues[7] == 0f && mValues[8] == 1f
    }

    actual open fun rectStaysRect(): Boolean {
        if (!isAffine()) return false
        val m00 = mValues[0]
        val m01 = mValues[1]
        val m10 = mValues[3]
        val m11 = mValues[4]
        return (m00 == 0f && m11 == 0f && m01 != 0f && m10 != 0f) ||
            (m00 != 0f && m11 != 0f && m01 == 0f && m10 == 0f)
    }

    actual open fun set(src: Matrix?) {
        if (src == null) {
            reset()
        } else {
            src.mValues.copyInto(mValues)
        }
    }

    actual open fun reset() {
        mValues[0] = 1f; mValues[1] = 0f; mValues[2] = 0f
        mValues[3] = 0f; mValues[4] = 1f; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setTranslate(dx: Float, dy: Float) {
        mValues[0] = 1f; mValues[1] = 0f; mValues[2] = dx
        mValues[3] = 0f; mValues[4] = 1f; mValues[5] = dy
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setScale(sx: Float, sy: Float, px: Float, py: Float) {
        mValues[0] = sx; mValues[1] = 0f; mValues[2] = px - sx * px
        mValues[3] = 0f; mValues[4] = sy; mValues[5] = py - sy * py
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setScale(sx: Float, sy: Float) {
        mValues[0] = sx; mValues[1] = 0f; mValues[2] = 0f
        mValues[3] = 0f; mValues[4] = sy; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setRotate(degrees: Float, px: Float, py: Float) {
        val rad = degrees * (PI / 180.0)
        setSinCos(sin(rad).toFloat(), cos(rad).toFloat(), px, py)
    }

    actual open fun setRotate(degrees: Float) {
        val rad = degrees * (PI / 180.0)
        setSinCos(sin(rad).toFloat(), cos(rad).toFloat())
    }

    actual open fun setSinCos(sinValue: Float, cosValue: Float, px: Float, py: Float) {
        val oneMinusCos = 1f - cosValue
        mValues[0] = cosValue
        mValues[1] = -sinValue
        mValues[2] = sinValue * py + oneMinusCos * px
        mValues[3] = sinValue
        mValues[4] = cosValue
        mValues[5] = -sinValue * px + oneMinusCos * py
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setSinCos(sinValue: Float, cosValue: Float) {
        mValues[0] = cosValue; mValues[1] = -sinValue; mValues[2] = 0f
        mValues[3] = sinValue; mValues[4] = cosValue; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setSkew(kx: Float, ky: Float, px: Float, py: Float) {
        mValues[0] = 1f; mValues[1] = kx; mValues[2] = -kx * py
        mValues[3] = ky; mValues[4] = 1f; mValues[5] = -ky * px
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setSkew(kx: Float, ky: Float) {
        mValues[0] = 1f; mValues[1] = kx; mValues[2] = 0f
        mValues[3] = ky; mValues[4] = 1f; mValues[5] = 0f
        mValues[6] = 0f; mValues[7] = 0f; mValues[8] = 1f
    }

    actual open fun setConcat(a: Matrix, b: Matrix): Boolean {
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

    actual open fun preTranslate(dx: Float, dy: Float): Boolean {
        val t = Matrix()
        t.setTranslate(dx, dy)
        return setConcat(this, t)
    }

    actual open fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy, px, py)
        return setConcat(this, t)
    }

    actual open fun preScale(sx: Float, sy: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy)
        return setConcat(this, t)
    }

    actual open fun preRotate(degrees: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees, px, py)
        return setConcat(this, t)
    }

    actual open fun preRotate(degrees: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees)
        return setConcat(this, t)
    }

    actual open fun preSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky, px, py)
        return setConcat(this, t)
    }

    actual open fun preSkew(kx: Float, ky: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky)
        return setConcat(this, t)
    }

    actual open fun preConcat(other: Matrix): Boolean = setConcat(this, other)

    actual open fun postTranslate(dx: Float, dy: Float): Boolean {
        val t = Matrix()
        t.setTranslate(dx, dy)
        return setConcat(t, this)
    }

    actual open fun postScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy, px, py)
        return setConcat(t, this)
    }

    actual open fun postScale(sx: Float, sy: Float): Boolean {
        val t = Matrix()
        t.setScale(sx, sy)
        return setConcat(t, this)
    }

    actual open fun postRotate(degrees: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees, px, py)
        return setConcat(t, this)
    }

    actual open fun postRotate(degrees: Float): Boolean {
        val t = Matrix()
        t.setRotate(degrees)
        return setConcat(t, this)
    }

    actual open fun postSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky, px, py)
        return setConcat(t, this)
    }

    actual open fun postSkew(kx: Float, ky: Float): Boolean {
        val t = Matrix()
        t.setSkew(kx, ky)
        return setConcat(t, this)
    }

    actual open fun postConcat(other: Matrix): Boolean = setConcat(other, this)

    actual open fun setRectToRect(src: RectF, dst: RectF, stf: ScaleToFit): Boolean {
        if (src.isEmpty()) {
            reset()
            return false
        }
        if (dst.isEmpty()) {
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
                var diff: Float
                if (xLarger) {
                    diff = dst.width() - src.width() * sy
                } else {
                    diff = dst.height() - src.height() * sy
                }
                if (stf == ScaleToFit.CENTER) {
                    diff *= 0.5f
                }
                if (xLarger) {
                    tx += diff
                } else {
                    ty += diff
                }
            }
            setScale(sx, sy)
            mValues[MTRANS_X] = tx
            mValues[MTRANS_Y] = ty
        }
        return true
    }

    actual open fun invert(inverse: Matrix?): Boolean {
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

    actual open fun mapPoints(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, pointCount: Int) {
        checkPointArrays(src, srcIndex, dst, dstIndex, pointCount)
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

    actual open fun mapVectors(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, vectorCount: Int) {
        checkPointArrays(src, srcIndex, dst, dstIndex, vectorCount)
        val temp = FloatArray(vectorCount * 2)
        for (i in 0 until vectorCount) {
            val x = src[srcIndex + i * 2]
            val y = src[srcIndex + i * 2 + 1]
            temp[i * 2] = mValues[0] * x + mValues[1] * y
            temp[i * 2 + 1] = mValues[3] * x + mValues[4] * y
        }
        temp.copyInto(dst, destinationOffset = dstIndex)
    }

    actual open fun mapPoints(dst: FloatArray, src: FloatArray) {
        if (dst.size != src.size) throw IndexOutOfBoundsException()
        mapPoints(dst, 0, src, 0, dst.size shr 1)
    }

    actual open fun mapVectors(dst: FloatArray, src: FloatArray) {
        if (dst.size != src.size) throw IndexOutOfBoundsException()
        mapVectors(dst, 0, src, 0, dst.size shr 1)
    }

    actual open fun mapPoints(pts: FloatArray) {
        mapPoints(pts, 0, pts, 0, pts.size shr 1)
    }

    actual open fun mapVectors(vecs: FloatArray) {
        mapVectors(vecs, 0, vecs, 0, vecs.size shr 1)
    }

    actual open fun mapRect(dst: RectF, src: RectF): Boolean {
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

    actual open fun mapRect(rect: RectF): Boolean = mapRect(rect, rect)

    actual open fun mapRadius(radius: Float): Float {
        val vecs = floatArrayOf(radius, 0f, 0f, radius)
        mapVectors(vecs)
        val d1 = hypot(vecs[0].toDouble(), vecs[1].toDouble())
        val d2 = hypot(vecs[2].toDouble(), vecs[3].toDouble())
        return sqrt(d1 * d2).toFloat()
    }

    actual open fun getValues(values: FloatArray) {
        if (values.size < 9) throw IndexOutOfBoundsException()
        mValues.copyInto(values, endIndex = 9)
    }

    actual open fun setValues(values: FloatArray) {
        if (values.size < 9) throw IndexOutOfBoundsException()
        values.copyInto(mValues, endIndex = 9)
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        for (i in 0 until 9) {
            if (mValues[i] != other.mValues[i]) return false
        }
        return true
    }

    actual override fun hashCode(): Int = mValues.contentHashCode()

    actual override fun toString(): String = "Matrix{${toShortString()}}"

    actual open fun toShortString(): String {
        return "[${mValues[0]}, ${mValues[1]}, ${mValues[2]}][${mValues[3]}, ${mValues[4]}, ${mValues[5]}][${mValues[6]}, ${mValues[7]}, ${mValues[8]}]"
    }

    private fun checkPointArrays(
        src: FloatArray,
        srcIndex: Int,
        dst: FloatArray,
        dstIndex: Int,
        pointCount: Int
    ) {
        val srcStop = srcIndex + (pointCount shl 1)
        val dstStop = dstIndex + (pointCount shl 1)
        if ((pointCount or srcIndex or dstIndex or srcStop or dstStop) < 0 ||
            srcStop > src.size || dstStop > dst.size
        ) {
            throw IndexOutOfBoundsException()
        }
    }

    actual enum class ScaleToFit {
        FILL,
        START,
        CENTER,
        END
    }

    actual companion object {
        actual const val MSCALE_X: Int = 0
        actual const val MSKEW_X: Int = 1
        actual const val MTRANS_X: Int = 2
        actual const val MSKEW_Y: Int = 3
        actual const val MSCALE_Y: Int = 4
        actual const val MTRANS_Y: Int = 5
        actual const val MPERSP_0: Int = 6
        actual const val MPERSP_1: Int = 7
        actual const val MPERSP_2: Int = 8
    }
}

actual open class Color actual constructor() {
    actual companion object {
        actual const val BLACK: Int = -0x1000000
        actual const val DKGRAY: Int = -0xbbbbbc
        actual const val GRAY: Int = -0x777778
        actual const val LTGRAY: Int = -0x333334
        actual const val WHITE: Int = -0x1
        actual const val RED: Int = -0x10000
        actual const val GREEN: Int = -0xff0100
        actual const val BLUE: Int = -0xffff01
        actual const val YELLOW: Int = -0x100
        actual const val CYAN: Int = -0xff0001
        actual const val MAGENTA: Int = -0xff01
        actual const val TRANSPARENT: Int = 0

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

        actual fun alpha(color: Int): Int = color ushr 24

        actual fun red(color: Int): Int = (color shr 16) and 0xFF

        actual fun green(color: Int): Int = (color shr 8) and 0xFF

        actual fun blue(color: Int): Int = color and 0xFF

        actual fun rgb(red: Int, green: Int, blue: Int): Int {
            return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        }

        actual fun rgb(red: Float, green: Float, blue: Float): Int {
            return (0xFF shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        actual fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
            return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

        actual fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
            return (((alpha * 255.0f + 0.5f).toInt()) shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        actual fun parseColor(colorString: String): Int {
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
                if (color != null) {
                    return color
                }
            }
            throw IllegalArgumentException("Unknown color")
        }

        actual fun RGBToHSV(red: Int, green: Int, blue: Int, hsv: FloatArray) {
            if (hsv.size < 3) throw RuntimeException("3 components required for hsv")
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

        actual fun colorToHSV(color: Int, hsv: FloatArray) {
            RGBToHSV((color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF, hsv)
        }

        actual fun HSVToColor(hsv: FloatArray): Int = HSVToColor(0xFF, hsv)

        actual fun HSVToColor(alpha: Int, hsv: FloatArray): Int {
            if (hsv.size < 3) throw RuntimeException("3 components required for hsv")
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
