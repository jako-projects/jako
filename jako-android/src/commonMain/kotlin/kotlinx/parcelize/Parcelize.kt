package kotlinx.parcelize

/**
 * Instructs the Jako Kotlin compiler plugin to automatically generate
 * [android.os.Parcelable] `describeContents()`, `writeToParcel(dest, flags)`,
 * and `CREATOR` implementations for this class across JVM and Kotlin/Native targets.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Parcelize
