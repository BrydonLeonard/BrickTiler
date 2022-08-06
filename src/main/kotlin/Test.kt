import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        repeat(1000) {
            launch {
                delay(10)
                println(it)
            }
        }

        println("running!")
    }

    println("out!")
}