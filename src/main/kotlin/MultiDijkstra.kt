package org.example

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class MultiDijkstra {

    /*  Context Fields    */
    private val graph: GraphBuilder.Graph<Int>

    private val presenters: List<AtomicRef<MultiPresenter>>

    private val queue: MultiPriorityQueue

    private val initMutex = Mutex()

    /*  States  */

    private val isFindingInit = AtomicBoolean(false)
    private val processedVertexes = AtomicInteger(0)

    /*  Methods */
    private fun initPresenter(i: Int) = MultiPresenter(AtomicInteger(i), AtomicLong(Long.MAX_VALUE))

    private fun initPresenters() {
        graph.forEachIndexed { i, _ -> presenters[i].lazySet(initPresenter(i)) }
    }

    constructor(qn: Int, g: GraphBuilder.Graph<Int>) {
        queue = MultiPriorityQueue(qn)
        graph = g
        presenters = graph.mapIndexed { i, _ -> atomic(initPresenter(i)) }
    }

    constructor(qn: Int, path: String) {
        queue = MultiPriorityQueue(qn)
        graph = GraphBuilder.readGraph(path)
        presenters = graph.mapIndexed { i, _ -> atomic(initPresenter(i)) }
    }

    suspend fun findMinLen(startId: Int, destId: Int): Long {

        suspend fun addToQueue(idx: Int) {
            val res = presenters[idx].value.marker.compareAndSet(Marker.Free, Marker.Processed)
            if (res) {
                queue.add(presenters[idx].value)
            }
        }

        if (isFindingInit.compareAndSet(false, true)) {
            //Init condition (only first thread can get an access, only one time)
            presenters[startId].value.weightInit(0)
            addToQueue(startId)
        }

        while (processedVertexes.get() < graph.n  || !queue.isEmptyPrerequisite() ) {
            queue.remove()?.let { curPresenter ->
                val curVertex = graph[curPresenter.id]

                curVertex.edges.forEach { edge ->
                    val toPresenter = presenters[edge.to.id].value
                    toPresenter.weightUpdate(curPresenter.weight + edge.weight, ::min)

                    addToQueue(toPresenter.id)
                }
                processedVertexes.incrementAndGet()
            }
        }
        return presenters[destId].value.weight
    }
}
