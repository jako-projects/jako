@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
expect annotation class SafeVarargs()
