package android.graphics

import android.os.Parcel
import android.os.Parcelable
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

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
        actual val CREATOR: Parcelable.Creator<RectF> = object : Parcelable.Creator<RectF> {
            override fun createFromParcel(source: Parcel): RectF {
                val r = RectF()
                r.readFromParcel(source)
                return r
            }

            override fun newArray(size: Int): Array<RectF?> = arrayOfNulls(size)
        }

        actual fun intersects(a: RectF, b: RectF): Boolean {
            return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
        }
    }
}
