package kotlin.jvm.optionals

import java.util.Optional

public fun <T : Any> Optional<T>.getOrNull(): T? = if (isPresent()) get() else null

public fun <T : Any> Optional<T>.getOrDefault(defaultValue: T): T = if (isPresent()) get() else defaultValue

public inline fun <T : Any> Optional<T>.getOrElse(onEmpty: () -> T): T = if (isPresent()) get() else onEmpty()

public fun <T : Any> Optional<T>.toList(): List<T> = if (isPresent()) listOf(get()) else emptyList()

public fun <T : Any> Optional<T>.asSequence(): Sequence<T> = if (isPresent()) sequenceOf(get()) else emptySequence()

