package java.util.concurrent

import kotlin.concurrent.withLock

actual interface Delayed : Comparable<Delayed> {
    actual fun getDelay(unit: TimeUnit): Long
}
