@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.util.Deque

expect interface BlockingDeque<E> : BlockingQueue<E>, Deque<E>
