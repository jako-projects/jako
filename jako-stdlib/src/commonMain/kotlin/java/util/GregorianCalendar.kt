@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class GregorianCalendar : Calendar {
    constructor()
    constructor(tz: TimeZone)
    constructor(year: Int, month: Int, dayOfMonth: Int)
    constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int)
    constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int, second: Int)
}
