@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin.jvm

import java.lang.Class
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
public expect annotation class JvmField()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
public expect annotation class JvmName(val name: String)

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
public expect annotation class JvmOverloads()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
public expect annotation class JvmStatic()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
public expect annotation class Synchronized()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
public expect annotation class JvmSuppressWildcards(val suppress: Boolean = true)

public expect val <T : Any> KClass<T>.java: Class<T>
public expect val <T : Any> KClass<T>.javaObjectType: Class<T>
public expect val <T : Any> KClass<T>.javaPrimitiveType: Class<T>?
public expect val <T : Any> Class<T>.kotlin: KClass<T>
public expect val <T : Any> T.javaClass: Class<T>
