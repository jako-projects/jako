package kotlin

import java.math.BigDecimal

public operator fun BigDecimal.plus(other: BigDecimal): BigDecimal = this.add(other)
public operator fun BigDecimal.minus(other: BigDecimal): BigDecimal = this.subtract(other)
public operator fun BigDecimal.times(other: BigDecimal): BigDecimal = this.multiply(other)
public operator fun BigDecimal.div(other: BigDecimal): BigDecimal = this.divide(other)
public operator fun BigDecimal.rem(other: BigDecimal): BigDecimal = this.remainder(other)
public operator fun BigDecimal.unaryMinus(): BigDecimal = this.negate()
public operator fun BigDecimal.inc(): BigDecimal = this.add(BigDecimal.ONE)
public operator fun BigDecimal.dec(): BigDecimal = this.subtract(BigDecimal.ONE)
