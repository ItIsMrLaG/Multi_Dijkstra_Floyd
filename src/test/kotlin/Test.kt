import org.example.GraphBuilder
import org.example.Mult.MultiDijkstra
import org.example.Seq.SequentialDijkstra
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals


internal class SeqTest {

    @ParameterizedTest
    @CsvSource(
        "./src/test/resources/samples/test5_0_6.test,5,0,6",
        "./src/test/resources/samples/test17_0_4.test,17,0,4"
    )
    fun `checking the correctness of the sequential version`(path: String, expected: Long, from: Int, to: Int) {
        val g = GraphBuilder.readGraph(path)
        val result = SequentialDijkstra(g).getMinDistance(from, to)
        assertEquals(expected, result)
    }
}

internal class ParallelismTests {

    @ParameterizedTest
    @CsvSource(
        "./src/test/resources/samples/test5_0_6.test,5,0,6",
        "./src/test/resources/samples/test17_0_4.test,17,0,4"
    )
    fun `checking the correctness of the sequential multi-version 1 thread + 4 queues`(
        path: String,
        expected: Long,
        from: Int,
        to: Int,
    ) {
        val g = GraphBuilder.readGraph(path)
        val result = MultiDijkstra(4, g).getMinDistance(from, to)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource(
        "1",
        "2",
        "12",
        "20",
        "40",
    )
    fun `checking correctness of the multi-version`(threadN: Int) {
        val g = GraphBuilder.readGraph("./src/test/resources/samples/components_1000.test")
        val mult = MultiDijkstra(threadN, g)
        val seq = SequentialDijkstra(g)

        val r1 = seq.getMinDistance(1, 99999)

        val r2 = mult.getMinDistance(1, 99999)

        assertEquals(r1, r2)
    }
}
