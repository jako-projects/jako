package kotlinx.parcelize

import android.os.Parcelable

/**
 * Read the CREATOR field of the given [Parcelable] class. Calls to this function with
 * a concrete class will be optimized to a direct field access.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <reified T : Parcelable> parcelableCreator(): Parcelable.Creator<T> =
    throw UnsupportedOperationException("parcelableCreator() must be intrinsified by the Parcelize compiler plugin")
