package bricktiler

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.util.*

class Logger {
    companion object {
        val logger = Logger()
        val job = GlobalScope.launch { logger.run() }
    }

    private var shouldLog = true
    private val buffer: Channel<String> = Channel<String>(10000)
    fun log(s: String) {
        runBlocking {
            buffer.send(s)
        }
    }

    suspend fun run() {
        coroutineScope {
            val writer = File("./log.txt").bufferedWriter()
            launch(Dispatchers.IO) {
                try  {
                    while (shouldLog) {
                        writer.write("${buffer.receive()}\n")
                    }
                } catch (e: Exception) { // If the channel's closed
                    println("Stopped logging with exception: $e")
                }
            }
        }
    }

    fun stopLogging() {
        shouldLog = false
    }
}