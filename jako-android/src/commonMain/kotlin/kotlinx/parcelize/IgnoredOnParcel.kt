package kotlinx.parcelize

/**
 * The property annotated with [IgnoredOnParcel] will not be stored into a [android.os.Parcel].
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class IgnoredOnParcel
