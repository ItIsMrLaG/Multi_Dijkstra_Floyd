package org.example.Mult

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class MultiPresenter(
    private val _id: AtomicInteger,
    private val _weight: AtomicLong,
) {

    val id: Int
        get() = _id.get()

    val weight: Long
        get() = _weight.get()

    fun weightInit(value: Long) = _weight.set(value)

    fun weightUpdate(new: Long, condF: (Long, Long) -> Boolean): Boolean {
        while (true) {
            val old = _weight.get()
            if (!condF(new, old)) return false
            if (_weight.compareAndSet(old, new)) return true
        }
    }

    class PComparator {

        companion object : Comparator<MultiPresenter> {

            override fun compare(a: MultiPresenter, b: MultiPresenter): Int =
                -(b._weight.get()).compareTo(a._weight.get())
        }
    }
}
