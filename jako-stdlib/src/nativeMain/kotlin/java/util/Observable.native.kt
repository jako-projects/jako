package java.util

actual open class Observable actual constructor() {
    private var changed = false
    private val obs = ArrayList<Observer>()

    actual open fun addObserver(o: Observer) {
        if (!obs.contains(o)) {
            obs.add(o)
        }
    }

    actual open fun deleteObserver(o: Observer?) {
        if (o != null) {
            obs.remove(o)
        }
    }

    actual open fun notifyObservers() {
        notifyObservers(null)
    }

    actual open fun notifyObservers(arg: Any?) {
        if (!changed) return
        val snapshot = ArrayList(obs)
        clearChanged()
        for (i in snapshot.size - 1 downTo 0) {
            snapshot[i].update(this, arg)
        }
    }

    actual open fun deleteObservers() {
        obs.clear()
    }

    protected actual open fun setChanged() {
        changed = true
    }

    protected actual open fun clearChanged() {
        changed = false
    }

    actual open fun hasChanged(): Boolean = changed

    actual open fun countObservers(): Int = obs.size
}
