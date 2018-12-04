package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main(args: Array<String>) {
    runExample(::structuredConcurrencyWithAsyncExample)
}


fun structuredConcurrencyWithAsyncExample() = runBlocking {
    println("The answer is ${concurrentSum()}")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 10
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 5
}



