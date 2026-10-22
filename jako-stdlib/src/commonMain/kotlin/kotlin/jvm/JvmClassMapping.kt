@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin.jvm

import java.lang.Class
import kotlin.reflect.KClass

public expect val <T : Any> KClass<T>.java: Class<T>
public expect val <T : Any> KClass<T>.javaObjectType: Class<T>
public expect val <T : Any> KClass<T>.javaPrimitiveType: Class<T>?
public expect val <T : Any> Class<T>.kotlin: KClass<T>
public expect val <T : Any> T.javaClass: Class<T>
