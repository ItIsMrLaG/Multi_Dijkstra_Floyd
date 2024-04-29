package org.example.Mult

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import org.example.DistanceFinder
import org.example.GraphBuilder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class MultiDijkstra : DistanceFinder {

    /*  Context Fields    */
    private val graph: GraphBuilder.Graph<Int>

    private val presenters: List<AtomicRef<MultiPresenter>>

    private val queue: MultiPriorityQueue

    /*  States  */
    private val isFindingInit = AtomicBoolean(false)

    private val nodesInQueue = AtomicInteger(0)

    /*  Methods */
    private fun initPresenter(i: Int) = MultiPresenter(AtomicInteger(i), AtomicLong(Long.MAX_VALUE))

    fun clear() = presenters.forEach { presenter ->
        presenter.value.weightInit(Long.MAX_VALUE)
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

    private suspend fun findMinLenPerThread(startId: Int, destId: Int): Long {

        suspend fun addToQueue(idx: Int) {
            nodesInQueue.incrementAndGet()
            queue.add(presenters[idx].value)
        }

        fun isNewLessThenOld(new: Long, old: Long) = new < old

        if (isFindingInit.compareAndSet(false, true)) {
            //Init condition (only first thread can get an access, only one time)
            presenters[startId].value.weightInit(0)
            addToQueue(startId)
        }

        while (true) {
            queue.remove()?.let { curPresenter ->
                val curVertex = graph[curPresenter.id]

                curVertex.edges.forEach { edge ->
                    val toPresenter = presenters[edge.to.id].value
                    if (toPresenter.weightUpdate(curPresenter.weight + edge.weight, ::isNewLessThenOld))
                        addToQueue(toPresenter.id)
                }
                nodesInQueue.decrementAndGet()
            }
            if (nodesInQueue.get() == 0) return presenters[destId].value.weight
            if (nodesInQueue.get() < 0) throw Exception("Impossible case: processed nodes > added for processing nodes")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    override fun getMinDistance(startId: Int, destId: Int) = runBlocking {
        val jobs = mutableListOf<Deferred<Long>>()
        coroutineScope { // scope for coroutines
            repeat(queue.getN()) { it ->
                jobs.add(async(newSingleThreadContext(it.toString())) {
                    findMinLenPerThread(startId, destId)
                })
            }
        }
        jobs.awaitAll()
        val res = jobs.map { it.await() }
        if (res.foldRight(true) { it, acc -> acc && it == res[0] }) return@runBlocking res[0]
        throw Exception("Different results (developer error): $res")
    }
}
