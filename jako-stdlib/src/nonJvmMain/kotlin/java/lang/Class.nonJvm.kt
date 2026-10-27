package java.lang

import java.io.Serializable
import kotlin.reflect.KClass

/**
 * [Nullability Decision] Relaxed from `T : Any` to `T` to match JDK 8 Class<T>
 * where T can be any type including nullable projections.
 */
@Suppress("UPPER_BOUND_CANNOT_BE_ARRAY")
actual class Class<T>(
    val kClass: KClass<out Any>,
    private val primitiveFlag: Boolean = false
) : Serializable {

    actual fun getName(): String {
        if (primitiveFlag) {
            return when (kClass) {
                Boolean::class -> "boolean"
                Byte::class -> "byte"
                Short::class -> "short"
                Char::class -> "char"
                Int::class -> "int"
                Long::class -> "long"
                Float::class -> "float"
                Double::class -> "double"
                Unit::class -> "void"
                else -> kClass.simpleName ?: "Unknown"
            }
        }
        return when (kClass) {
            Boolean::class -> "java.lang.Boolean"
            Byte::class -> "java.lang.Byte"
            Short::class -> "java.lang.Short"
            Char::class -> "java.lang.Character"
            Int::class -> "java.lang.Integer"
            Long::class -> "java.lang.Long"
            Float::class -> "java.lang.Float"
            Double::class -> "java.lang.Double"
            String::class -> "java.lang.String"
            Any::class -> "java.lang.Object"
            Number::class -> "java.lang.Number"
            Throwable::class -> "java.lang.Throwable"
            Exception::class -> "java.lang.Exception"
            RuntimeException::class -> "java.lang.RuntimeException"
            Error::class -> "java.lang.Error"
            Void::class -> "java.lang.Void"
            BooleanArray::class -> "[Z"
            ByteArray::class -> "[B"
            ShortArray::class -> "[S"
            CharArray::class -> "[C"
            IntArray::class -> "[I"
            LongArray::class -> "[J"
            FloatArray::class -> "[F"
            DoubleArray::class -> "[D"
            kotlin.Array::class -> "[Ljava.lang.Object;"
            java.io.File::class -> "java.io.File"
            else -> kClass.simpleName ?: "Unknown"
        }
    }

    actual fun getSimpleName(): String {
        if (primitiveFlag) {
            return getName()
        }
        return when (kClass) {
            Int::class -> "Integer"
            Char::class -> "Character"
            Any::class -> "Object"
            BooleanArray::class -> "boolean[]"
            ByteArray::class -> "byte[]"
            ShortArray::class -> "short[]"
            CharArray::class -> "char[]"
            IntArray::class -> "int[]"
            LongArray::class -> "long[]"
            FloatArray::class -> "float[]"
            DoubleArray::class -> "double[]"
            kotlin.Array::class -> "Object[]"
            else -> kClass.simpleName ?: "Unknown"
        }
    }

    actual fun getCanonicalName(): String? {
        if (primitiveFlag) return getName()
        return when (kClass) {
            BooleanArray::class -> "boolean[]"
            ByteArray::class -> "byte[]"
            ShortArray::class -> "short[]"
            CharArray::class -> "char[]"
            IntArray::class -> "int[]"
            LongArray::class -> "long[]"
            FloatArray::class -> "float[]"
            DoubleArray::class -> "double[]"
            kotlin.Array::class -> "java.lang.Object[]"
            else -> getName()
        }
    }

    actual fun isPrimitive(): Boolean = primitiveFlag

    actual fun isArray(): Boolean = !primitiveFlag && when (kClass) {
        BooleanArray::class,
        ByteArray::class,
        ShortArray::class,
        CharArray::class,
        IntArray::class,
        LongArray::class,
        FloatArray::class,
        DoubleArray::class,
        kotlin.Array::class -> true
        else -> false
    }

    actual fun isInstance(obj: Any?): Boolean {
        if (obj == null || primitiveFlag) return false
        return kClass.isInstance(obj)
    }

    actual fun getComponentType(): Class<*>? {
        if (primitiveFlag) return null
        return when (kClass) {
            BooleanArray::class -> Class<Boolean>(Boolean::class, primitiveFlag = true)
            ByteArray::class -> Class<Byte>(Byte::class, primitiveFlag = true)
            ShortArray::class -> Class<Short>(Short::class, primitiveFlag = true)
            CharArray::class -> Class<Char>(Char::class, primitiveFlag = true)
            IntArray::class -> Class<Int>(Int::class, primitiveFlag = true)
            LongArray::class -> Class<Long>(Long::class, primitiveFlag = true)
            FloatArray::class -> Class<Float>(Float::class, primitiveFlag = true)
            DoubleArray::class -> Class<Double>(Double::class, primitiveFlag = true)
            kotlin.Array::class -> Class<Any>(Any::class, primitiveFlag = false)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual fun cast(obj: Any?): T? {
        if (obj == null) return null
        if (!isInstance(obj)) {
            throw ClassCastException("Cannot cast ${obj::class.simpleName} to $name")
        }
        return obj as T
    }

    actual val name: String get() = getName()
    actual val simpleName: String get() = getSimpleName()
    actual val canonicalName: String? get() = getCanonicalName()
    actual val isPrimitive: Boolean get() = isPrimitive()
    actual val isArray: Boolean get() = isArray()
    actual val componentType: Class<*>? get() = getComponentType()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Class<*>) return false
        return kClass == other.kClass && primitiveFlag == other.primitiveFlag
    }

    override fun hashCode(): Int = 31 * kClass.hashCode() + if (primitiveFlag) 1 else 0
    override fun toString(): String = if (primitiveFlag) name else "class $name"

    actual companion object {
        internal fun isPrimitiveKClass(kClass: KClass<*>): Boolean = when (kClass) {
            Boolean::class,
            Byte::class,
            Short::class,
            Char::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class,
            Unit::class -> true
            else -> false
        }

        actual fun forName(className: String): Class<*> {
            return when (className) {
                "java.lang.String" -> Class<String>(String::class)
                "java.lang.Object" -> Class<Any>(Any::class)
                "java.lang.Boolean" -> Class<Boolean>(Boolean::class)
                "java.lang.Byte" -> Class<Byte>(Byte::class)
                "java.lang.Short" -> Class<Short>(Short::class)
                "java.lang.Character" -> Class<Char>(Char::class)
                "java.lang.Integer" -> Class<Int>(Int::class)
                "java.lang.Long" -> Class<Long>(Long::class)
                "java.lang.Float" -> Class<Float>(Float::class)
                "java.lang.Double" -> Class<Double>(Double::class)
                "java.lang.Number" -> Class<Number>(Number::class)
                "java.lang.Throwable" -> Class<Throwable>(Throwable::class)
                "java.lang.Exception" -> Class<Exception>(Exception::class)
                "java.lang.RuntimeException" -> Class<RuntimeException>(RuntimeException::class)
                "java.lang.Void" -> Class<Void>(Void::class)
                "java.io.File" -> Class<java.io.File>(java.io.File::class)
                else -> throw ClassNotFoundException(className)
            }
        }
    }
}
