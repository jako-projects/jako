package kotlin.collections

import java.util.Collections
import java.util.Enumeration

public fun <T> Enumeration<T>.toList(): List<T> = Collections.list(this)
