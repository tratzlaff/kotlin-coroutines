package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main(args: Array<String>) {
    runExample(::extractFunctionRefactoringExample)
    runExample(::sequentialByDefaultExample)
    runExample(::concurrentUsingAsyncExample)
    runExample(::lazilyStartedAsyncExample)
    runExample(::structuredConcurrencyWithAsyncExample)
    runExample(::cancellationExample)
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


/**
 * The code in the coroutine, just like in the regular code, is sequential by default.
 */
fun sequentialByDefaultExample() = runBlocking<Unit> {
    val one = doSomethingUsefulOne()
    val two = doSomethingUsefulTwo()
    println("The answer is ${one + two}")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 10
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 5
}


/**
 * Conceptually, async is just like launch.
 * It starts a separate coroutine which is a light-weight thread that works concurrently with all the other coroutines.
 * The difference is that launch returns a Job and does not carry any resulting value,
 * while async returns a Deferred – a light-weight non-blocking future that represents a promise to provide
 * a result later. You can use .await() on a deferred value to get its eventual result,
 * but Deferred is also a Job, so you can cancel it if needed.
 *
 * Concurrency with coroutines is always explicit.
 */
fun concurrentUsingAsyncExample() = runBlocking {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    println("The answer is ${one.await() + two.await()}")
}


/**
 * There is a laziness option to async using an optional start parameter with a value of CoroutineStart.LAZY.
 * It starts coroutine only when its result is needed by some await or if a start function is invoked.
 */
fun lazilyStartedAsyncExample() = runBlocking {
    val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
    val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }

    // some computation

    one.start()
    two.start()

    // If we have called await in println and omitted start on individual coroutines,
    // then we would have got the sequential behaviour as await starts the coroutine
    // execution and waits for the execution to finish, which is not the intended use-case for laziness.
    println("The answer is ${one.await() + two.await()}")
}




fun structuredConcurrencyWithAsyncExample() = runBlocking {
    println("The answer is ${concurrentSum()}")
}

/**
 * Let us take concurrentUsingAsyncExample() and extract a function that concurrently performs
 * doSomethingUsefulOne and doSomethingUsefulTwo and returns the sum of their results.
 *
 * Because async coroutines builder is defined as extension on CoroutineScope
 * we need to have it in the scope  and that is what coroutineScope function provides.
 * This way, if something goes wrong inside the concurrentSum function and it throws an exception,
 * all the coroutines that were launched in its scope are cancelled.
 */
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}


/**
 * Cancellation is always propagated through coroutines hierarchy.
 * Both first async and awaiting parent are cancelled on the one child failure.
 */
fun cancellationExample() = runBlocking<Unit> {
    try {
        failedConcurrentSum()
    } catch(e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE) // Emulates very long computation
            42
        } finally {
            println("First child was cancelled")
        }
    }
    val two = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}
