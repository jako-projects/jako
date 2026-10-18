package kotlin.jvm

import java.lang.Class
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmField actual constructor()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmName actual constructor(actual val name: String)

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmOverloads actual constructor()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
public actual annotation class JvmStatic actual constructor()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
public actual annotation class Synchronized actual constructor()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmSuppressWildcards actual constructor(actual val suppress: Boolean)

public actual val <T : Any> KClass<T>.java: Class<T> get() = Class(this, primitiveFlag = Class.isPrimitiveKClass(this))
public actual val <T : Any> KClass<T>.javaObjectType: Class<T> get() = Class(this, primitiveFlag = false)
public actual val <T : Any> KClass<T>.javaPrimitiveType: Class<T>? get() = if (Class.isPrimitiveKClass(this)) Class(this, primitiveFlag = true) else null
@Suppress("UNCHECKED_CAST")
public actual val <T : Any> Class<T>.kotlin: KClass<T> get() = kClass as KClass<T>
@Suppress("UNCHECKED_CAST")
public actual val <T : Any> T.javaClass: Class<T> get() = Class(this::class as KClass<T>, primitiveFlag = false)

