package java.util

actual open class GregorianCalendar : Calendar {
    actual constructor() : super() {
        timeInMillis = System.currentTimeMillis()
    }
    actual constructor(tz: TimeZone) : super() {
        setTimeZone(tz)
        timeInMillis = System.currentTimeMillis()
    }
    actual constructor(year: Int, month: Int, dayOfMonth: Int) : super() {
        set(year, month, dayOfMonth)
    }
    actual constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int) : super() {
        set(year, month, dayOfMonth, hourOfDay, minute)
    }
    actual constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int, second: Int) : super() {
        set(year, month, dayOfMonth, hourOfDay, minute, second)
    }
}
