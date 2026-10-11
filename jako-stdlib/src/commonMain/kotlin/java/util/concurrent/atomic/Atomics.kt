@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable

expect class AtomicInteger(initialValue: Int) : Number, Serializable {
    constructor()
    fun get(): Int
    fun set(newValue: Int)
    fun getAndSet(newValue: Int): Int
    fun compareAndSet(expect: Int, update: Int): Boolean
    fun getAndIncrement(): Int
    fun getAndDecrement(): Int
    fun getAndAdd(delta: Int): Int
    fun incrementAndGet(): Int
    fun decrementAndGet(): Int
    fun addAndGet(delta: Int): Int

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}

expect class AtomicBoolean(initialValue: Boolean) : Serializable {
    constructor()
    fun get(): Boolean
    fun set(newValue: Boolean)
    fun getAndSet(newValue: Boolean): Boolean
    fun compareAndSet(expect: Boolean, update: Boolean): Boolean
}

/**
 * [Nullability Decision] Java `AtomicReference.get/set` accept and return a nullable referent
 * (the no-arg constructor stores `null`). Kotlin signatures use `V?`.
 */
expect class AtomicReference<V>(initialValue: V?) : Serializable {
    constructor()
    fun get(): V?
    fun set(newValue: V?)
    fun getAndSet(newValue: V?): V?
    fun compareAndSet(expect: V?, update: V?): Boolean
}

expect class AtomicLong(initialValue: Long) : Number, Serializable {
    constructor()
    fun get(): Long
    fun set(newValue: Long)
    fun getAndSet(newValue: Long): Long
    fun compareAndSet(expect: Long, update: Long): Boolean
    fun getAndIncrement(): Long
    fun getAndDecrement(): Long
    fun getAndAdd(delta: Long): Long
    fun incrementAndGet(): Long
    fun decrementAndGet(): Long
    fun addAndGet(delta: Long): Long

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}

