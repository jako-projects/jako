package java.lang

/**
 * Marker interface matching JDK 8 java.lang.Cloneable.
 * On JVM, clone() is inherited from Object; on Native, we declare it here
 * so interfaces extending Cloneable can override it.
 */
public actual interface Cloneable
