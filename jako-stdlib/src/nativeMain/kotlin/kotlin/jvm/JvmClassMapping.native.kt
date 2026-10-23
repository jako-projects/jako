package kotlin.jvm

import java.lang.Class
import kotlin.reflect.KClass

public actual val <T : Any> KClass<T>.java: Class<T>
    get() = Class(this, primitiveFlag = Class.isPrimitiveKClass(this))

public actual val <T : Any> KClass<T>.javaObjectType: Class<T>
    get() = Class(this, primitiveFlag = false)

public actual val <T : Any> KClass<T>.javaPrimitiveType: Class<T>?
    get() = if (Class.isPrimitiveKClass(this)) Class(this, primitiveFlag = true) else null

@Suppress("UNCHECKED_CAST")
public actual val <T : Any> Class<T>.kotlin: KClass<T>
    get() = kClass as KClass<T>

@Suppress("UNCHECKED_CAST")
public actual val <T : Any> T.javaClass: Class<T>
    get() = Class(this::class as KClass<T>, primitiveFlag = false)
