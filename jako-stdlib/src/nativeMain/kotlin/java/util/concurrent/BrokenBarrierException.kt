package java.util.concurrent

actual open class BrokenBarrierException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
