package org.example.Seq

import org.example.GraphBuilder
import java.util.*
import kotlin.math.min

class SequentialDijkstra {

    private val graph: GraphBuilder.Graph<Int>

    private var presenters: List<SeqPresenter>

    private val queue = PriorityQueue(SeqPresenter.SeqPresenterComparator)

    fun clear() = presenters.forEach { presenter ->
        presenter.mark = Marker.Free
        presenter.weight = Long.MAX_VALUE
    }

    constructor(g: GraphBuilder.Graph<Int>) {
        graph = g
        presenters = graph.mapIndexed { i, _ -> SeqPresenter(i, Long.MAX_VALUE) }
    }

    constructor(path: String) {
        graph = GraphBuilder.readGraph(path)
        presenters = graph.mapIndexed { i, _ -> SeqPresenter(i, Long.MAX_VALUE) }
    }

    private fun initPesenters() {
        presenters = graph.mapIndexed { i, _ -> SeqPresenter(i, Long.MAX_VALUE) }
    }

    fun getMinDistance(startId: Int, destId: Int): Long {

        presenters[startId].weight = 0
        presenters[startId].mark = Marker.Processed
        queue.add(presenters[startId])

        while (queue.isNotEmpty()) {
            val curPresenter = queue.remove()
            val curVertex = graph[curPresenter.id]

            curVertex.edges.forEach { edge ->
                val toPresenter = presenters[edge.to.id]
                toPresenter.weight = min(toPresenter.weight, curPresenter.weight + edge.weight)

                if (toPresenter.mark == Marker.Free) {
                    queue.add(toPresenter)
                    toPresenter.mark = Marker.Processed
                }
            }
        }

        val ans = presenters[destId].weight
        initPesenters()

        return ans
    }
}
