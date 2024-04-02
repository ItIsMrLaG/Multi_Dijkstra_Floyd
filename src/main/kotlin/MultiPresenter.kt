package org.example

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class MultiPresenter(
    private val _id: AtomicInteger,
    private val _weight: AtomicLong,
    val marker: AtomicRef<Marker> = atomic(Marker.Free),
) {

    val id: Int
        get() = _id.get()

    val weight: Long
        get() = _weight.get()

    fun weightInit(value: Long) = _weight.set(value)

    fun weightUpdate(new: Long, condF: (Long, Long) -> Long) {
        val old = _weight.get()
        while (!_weight.compareAndSet(old, condF(old, new))) {
        }
    }

    class PComparator {

        companion object : Comparator<MultiPresenter> {

            override fun compare(a: MultiPresenter, b: MultiPresenter): Int =
                -(b._weight.get()).compareTo(a._weight.get())
        }
    }
}
