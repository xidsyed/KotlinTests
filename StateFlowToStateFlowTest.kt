import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class StateFlowTest {
    /**
     * This code showcases Hot Flows can be produced, transformed and consumed
     * */

    private lateinit var _foo: MutableStateFlow<List<Int>>
    private lateinit var foo: StateFlow<List<Int>>

    private lateinit var _bar: MutableStateFlow<List<Int>>
    private lateinit var bar: StateFlow<List<Int>>

    @Before
    fun setup() {
        _foo = MutableStateFlow(listOf(1, 2, 3))
        foo = _foo

        _bar = MutableStateFlow(emptyList())
        bar = _bar
    }


    private suspend fun transformToBar() {
        // transform one stateflow into another stateflow
        foo.map { list -> list.map { it * 10 } }.collect {
            println("transforming to bar")
            _bar.emit(it)
        }

    }

    private suspend fun producer() {
        var i = 0
        while (true) {
            _foo.emit(listOf(i, i+1, i+2))
            i++
            delay(1000)
        }
    }

    // Notably the consumer will keep consuming as long as the producer is producing.
    private suspend fun consumer(flowName: String, flow: StateFlow<List<Int>>) {
        flow.collect { println("$flowName : $it") }
    }

    @Test
    fun main() = runBlocking {
        async { producer() }

        async { transformToBar() }

       async { consumer("Foo", foo) }

       async { consumer("Bar", bar) }

        delay(10000)
    }

    suspend fun async(action: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            action()
        }
    }


}