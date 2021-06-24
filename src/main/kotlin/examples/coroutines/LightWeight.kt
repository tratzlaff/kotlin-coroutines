package examples.coroutines

import kotlinx.coroutines.*
import runExample
import kotlin.concurrent.thread

fun main() {
    runExample(::threadsExample, true)
    runExample(::coroutinesExample, true)
}

/**
 * This will launch 100k threads and have each print a dot.
 * Not fast.
 */
fun threadsExample() {
    repeat (100_000) {
        thread(start = true) {
            print(".")
        }
    }
}

/**
 * This will launch 100k coroutines and have each print a dot.
 * Fast.
 */
fun coroutinesExample() = runBlocking {
    repeat(100_000) {
        launch {
            print(".")
        }
    }
}
