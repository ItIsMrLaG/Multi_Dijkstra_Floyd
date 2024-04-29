package org.example.Mult

import kotlinx.coroutines.*
import org.example.DistanceFinder
import org.example.GraphBuilder

class MultiFloyd(val threadN: Int, private val graph: GraphBuilder.Graph<Int>) : DistanceFinder {

    private val matrix = graph.getMatrix()

    private fun calculatePart(from: Int, to: Int, k: Int) {
        for (i in from..<to) {
            if (matrix[i][k] == Long.MAX_VALUE) continue
            for (j in 0..<graph.n) {
                if (matrix[k][j] == Long.MAX_VALUE) continue
                val new = matrix[i][k] + matrix[k][j]
                if (new < matrix[i][j]) matrix[i][j] = new
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    override fun getMinDistance(startId: Int, destId: Int) = runBlocking {
        val linesPerThread = graph.n / threadN

        coroutineScope { // scope for coroutines
            val jobs = mutableListOf<Job>()
            for (k in 0..<graph.n) {
                repeat(threadN - 1) { ik ->
                    jobs.add(launch(newSingleThreadContext(ik.toString())) {
                        calculatePart(linesPerThread * ik, linesPerThread * (ik + 1), k)
                    })
                    jobs.add(launch(newSingleThreadContext(threadN.toString())) {
                        calculatePart(linesPerThread * (threadN - 1), graph.n, k)
                    })

                }
                jobs.joinAll()
            }
        }
        return@runBlocking matrix[startId][destId]
    }
}
