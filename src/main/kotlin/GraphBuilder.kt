package org.example

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import java.io.File

object GraphBuilder {

    private data class Node(val fromIdx: Int, val toIdx: Int, val weight: Int)

    data class Edge<T>(val to: Vertex<T>, val weight: Int) {

        override fun equals(other: Any?): Boolean {
            return (other is Edge<*>) && (other.to == to)
        }

        override fun hashCode(): Int {
            return to.hashCode()
        }
    }

    data class Vertex<T>(val id: Int, val data: T) {

        private val _edges = mutableSetOf<Edge<T>>()

        val edges: HashSet<Edge<T>>
            get() = _edges.toHashSet()

        private fun add(v: Vertex<T>, w: Int) = _edges.add(Edge(v, w))

        fun connectTo(v: Vertex<T>, w: Int) {
            add(v, w)
            v.add(this, w)
        }

        override fun toString(): String {
            var s = "id: $id\nvalue: $data\n"
            _edges.forEach { s += "(${it.to.id} [${it.weight}]) | " }
            s += "\n"
            return s
        }
    }

    class Graph<T>(values: List<T>) : Iterable<Vertex<T>> {

        private val vertex: List<Vertex<T>> = values.mapIndexed { i, it -> Vertex(i, it) }
        val n = vertex.size

        operator fun get(i: Int) = vertex[i]
        override fun iterator(): Iterator<Vertex<T>> {
            return vertex.iterator()
        }

        override fun toString(): String {
            var s = "N: $n\n"
            vertex.forEach { s += it.toString() }
            return s
        }
    }

    private fun <T> createGraph(info: List<Node>, vls: List<T>): Graph<T> {
        val graph = Graph(vls)

        info.forEach { ej ->
            graph[ej.fromIdx].connectTo(graph[ej.toIdx], ej.weight)
        }
        return graph
    }

    private fun parseNodes(info: List<String>) = info.map { it ->
        val edge = it.split(" ").map { it.toInt() }
        Node(edge[0], edge[1], edge[2])
    }

    fun readGraph(path: String): Graph<Int> {
        val info = File(path).useLines { it.toList() }
        val n = info[0].toInt()
        val nodes = parseNodes(info.subList(1, info.size))

        return createGraph(nodes, (0..<n).toList())
    }

    fun generateGraph(n: Int, maxEdgeN: Int, maxW: Int, minW: Int = 0): Graph<Int> {
        if (maxEdgeN - (n - 1) < 0) throw RuntimeException("maxEdgeN should be greater then (n-1)")
        if (n <= 0) throw RuntimeException("n should be greater then 0")

        val info = mutableListOf<String>()
        val unlinked = (1..<n).toMutableSet()
        val linked = mutableSetOf(0)

        // Create random spanning tree
        (1..<n).forEach { _ ->
            val prev = linked.random()
            val new = unlinked.random()
            info.add("$prev $new ${(minW..maxW).random()}")
            linked.add(new)
            unlinked.remove(new)
        }

        // Add random edges
        (0..<(maxEdgeN - (n - 1))).forEach { _ ->
            val range = 0..<n
            val firstV = range.random()
            val secondV = range.random().let {
                if (it == firstV) {
                    (firstV + 1) % (n - 1)
                } else it
            }
            info.add("$firstV $secondV ${(minW..maxW).random()}")
        }

        val nodes = parseNodes(info)
        return createGraph(nodes, (0..<n).toList())
    }

    fun <T> saveToFile(graph: Graph<T>, path: String) {
        File(path).printWriter().use { out ->
            out.println(graph.n)
            graph.forEach { vertex ->
                val toIdx = vertex.id
                vertex.edges.forEach { edge ->
                    out.println("$toIdx ${edge.to.id} ${edge.weight}")
                }
            }
        }
    }
}
