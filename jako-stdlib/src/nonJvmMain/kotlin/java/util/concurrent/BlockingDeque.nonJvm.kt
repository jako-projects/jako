package java.util.concurrent

import java.util.Deque

actual interface BlockingDeque<E> : BlockingQueue<E>, Deque<E>
