package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main() {
    runExample(::globalCoroutinesAreLikeDaemonThreadsExample)
}

/**
 * This launches a long-running coroutine in GlobalScope, then returns after a delay.
 * Active coroutines that were launched in GlobalScope do NOT keep the process alive.
 * They are like daemon threads.
 */
fun globalCoroutinesAreLikeDaemonThreadsExample() = runBlocking {
    GlobalScope.launch {
        repeat(5) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    println("waiting...")
    delay(1300L) // just quit after delay
}




