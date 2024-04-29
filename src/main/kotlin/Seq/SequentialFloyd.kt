package org.example.Seq

import org.example.DistanceFinder
import org.example.GraphBuilder

class SequentialFloyd(private val graph: GraphBuilder.Graph<Int>) : DistanceFinder {

    private val matrix = graph.getMatrix()

    override fun getMinDistance(startId: Int, destId: Int): Long {
        val n = graph.n
        for (k in 0..<n) {
            for (i in 0..<n) {
                if (matrix[i][k] == Long.MAX_VALUE) continue
                for (j in 0..<n) {
                    if (matrix[k][j] == Long.MAX_VALUE) continue
                    val new = matrix[i][k] + matrix[k][j]
                    if (new < matrix[i][j]) matrix[i][j] = new
                }
            }
        }
        return matrix[startId][destId]
    }
}
