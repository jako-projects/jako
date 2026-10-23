package kotlinx.parcelize

/**
 * Write the corresponding property value with [android.os.Parcel.writeValue].
 * Serialization may fail at runtime, depending on actual property value type.
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class RawValue
