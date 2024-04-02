package org.example

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue

class MultiPriorityQueue(private val n: Int) {

    inner class QueueItem {

        private val q = PriorityQueue(MultiPresenter.PComparator)
        private val mutex = Mutex()
        private var _size = AtomicInteger(0)

        val size: Int
            get() = _size.get()

        suspend fun addElement(value: MultiPresenter) = mutex.withLock {
            q.add(value)
            _size.incrementAndGet()
        }

        suspend fun removeElement(): MultiPresenter? = mutex.withLock {
            //TODO: Мог получить тут что-то плохое (мб deadlock)
            var l = _size.decrementAndGet()
            while (l < 0) {
                _size.compareAndSet(l, 0)
                l = _size.get()
            }
            return@withLock try {
                q.remove()
            } catch (el: NoSuchElementException) {
                null
            }
        }
    }

    private val multiQueueItems = Array(n) { atomic(QueueItem()) }

    private val random = Random()

    private fun getInd() = (random.nextInt() % n).absoluteValue

    private fun getIndS(): Pair<Int, Int> =
        Pair(getInd(), getInd())

    suspend fun remove(): MultiPresenter? {
        val (idx1, idx2) = getIndS()
        val (q1, q2) = Pair(multiQueueItems[idx1].value, multiQueueItems[idx2].value)
        val (v1, v2) = Pair(q1.removeElement(), q2.removeElement())
        if (v1 == null && v2 == null) return null
        if (v1 == null) return v2
        if (v2 == null) return v1
        if (MultiPresenter.PComparator.compare(
                v1,
                v2
            ) < 0
        ) { //TODO: Потенциальная ошибка (знак мог не в ту сторону поставить)
            q2.addElement(v2)
            return v1
        } else {
            q1.addElement(v1)
            return v2
        }
    }

    suspend fun add(value: MultiPresenter) {
        val idx = getInd()
        val q = multiQueueItems[idx].value
        q.addElement(value)
    }

    fun isEmptyPrerequisite(): Boolean {
        for (el in multiQueueItems) {
            val q = el.value
            if (q.size != 0) return false
        }
        return true
    }
}
