package kotlin

import java.math.BigInteger

public operator fun BigInteger.plus(other: BigInteger): BigInteger = this.add(other)
public operator fun BigInteger.minus(other: BigInteger): BigInteger = this.subtract(other)
public operator fun BigInteger.times(other: BigInteger): BigInteger = this.multiply(other)
public operator fun BigInteger.div(other: BigInteger): BigInteger = this.divide(other)
public operator fun BigInteger.rem(other: BigInteger): BigInteger = this.remainder(other)
public operator fun BigInteger.unaryMinus(): BigInteger = this.negate()
public operator fun BigInteger.inc(): BigInteger = this.add(BigInteger.ONE)
public operator fun BigInteger.dec(): BigInteger = this.subtract(BigInteger.ONE)

public infix fun BigInteger.shl(n: Int): BigInteger = this.shiftLeft(n)
public infix fun BigInteger.shr(n: Int): BigInteger = this.shiftRight(n)
public infix fun BigInteger.and(other: BigInteger): BigInteger = this.and(other)
public infix fun BigInteger.or(other: BigInteger): BigInteger = this.or(other)
public infix fun BigInteger.xor(other: BigInteger): BigInteger = this.xor(other)
public fun BigInteger.inv(): BigInteger = this.not()
