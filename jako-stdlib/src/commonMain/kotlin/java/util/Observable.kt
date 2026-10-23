@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class Observable() {
    open fun addObserver(o: Observer)
    open fun deleteObserver(o: Observer?)
    open fun notifyObservers()
    open fun notifyObservers(arg: Any?)
    open fun deleteObservers()
    protected open fun setChanged()
    protected open fun clearChanged()
    open fun hasChanged(): Boolean
    open fun countObservers(): Int
}
