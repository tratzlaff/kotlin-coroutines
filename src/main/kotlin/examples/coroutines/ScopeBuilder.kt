package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main(args: Array<String>) {
    runExample(::scopeBuilderExample)
}

/**
 * In addition to the coroutine scope provided by different builders,
 * you can declare your own scope using coroutineScope builder.
 * It creates a new coroutine scope and does not complete until all launched children complete.
 *
 * runBlocking blocks the current thread while waiting for children to complete.
 * coroutineScope does NOT block the current thread while waiting for children to complete.
 */
fun scopeBuilderExample() = runBlocking {
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    // Creates a new coroutine scope. Does NOT block current thread.
    coroutineScope {
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // This line will be printed before nested launch
    }

    println("Coroutine scope is over") // This line is not printed until nested launch completes
}



