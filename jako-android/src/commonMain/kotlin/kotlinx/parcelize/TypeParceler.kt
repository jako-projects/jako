package kotlinx.parcelize

/**
 * Specifies what [Parceler] should be used for a particular type [T].
 */
@Retention(AnnotationRetention.SOURCE)
@Repeatable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class TypeParceler<T, P : Parceler<in T>>
