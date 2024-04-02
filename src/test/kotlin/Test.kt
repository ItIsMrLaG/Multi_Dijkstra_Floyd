import kotlinx.coroutines.*
import kotlin.test.assertEquals
import org.example.GraphBuilder
import org.example.MultiDijkstra
import org.example.SequentialDijkstra
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class SampleTest {

    @ParameterizedTest
    @CsvSource(
        "./src/test/resources/samples/test5_0_6.test, 5, 0, 6",
        "./src/test/resources/samples/test17_0_4.test, 17, 0, 4"
    )
    fun `checking the correctness of the sequential version`(path: String, expected: Long, from: Int, to: Int) {
        val g = GraphBuilder.readGraph(path)
        val result = SequentialDijkstra(g).findMinLen(from, to)
        assertEquals(expected, result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @CsvSource(
        "./src/test/resources/samples/test5_0_6.test, 5, 0, 6",
        "./src/test/resources/samples/test17_0_4.test, 17, 0, 4"
    )
    fun `checking the correctness of the sequential multi-version 1 thread`(
        path: String,
        expected: Long,
        from: Int,
        to: Int,
    ) {
        val g = GraphBuilder.readGraph(path)
        val stand = MultiDijkstra(1, g)
        val result = runBlocking {
            coroutineScope { // scope for coroutines
                stand.findMinLen(from, to)
            }
        }
        assertEquals(expected, result)
    }
}
