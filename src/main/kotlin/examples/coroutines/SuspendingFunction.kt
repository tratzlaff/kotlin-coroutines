package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main(args: Array<String>) {
    runExample(::extractFunctionRefactoringExample)
}

fun extractFunctionRefactoringExample() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}

/**
 * Suspending functions can be used inside coroutines just like regular functions,
 * but their additional feature is that they can use other suspending functions (like delay)
 * to suspend execution of a coroutine.
 */
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}
