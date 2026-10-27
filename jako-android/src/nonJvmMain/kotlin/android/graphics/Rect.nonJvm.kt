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
        actual val CREATOR: Parcelable.Creator<Rect> = object : Parcelable.Creator<Rect> {
            override fun createFromParcel(source: Parcel): Rect {
                val r = Rect()
                r.readFromParcel(source)
                return r
            }

            override fun newArray(size: Int): Array<Rect?> = arrayOfNulls(size)
        }

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
