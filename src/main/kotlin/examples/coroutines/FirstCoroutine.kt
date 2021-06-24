package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main() {
    runExample(::firstCoroutineExample)
    runExample(::wrapInRunBlockingExample)
    runExample(::waitingForAJobExample)
    runExample(::structuredConcurrencyExample)
}

/**
 * Not ideal because we're using GlobalScope and delay.
 */
fun firstCoroutineExample() {

    // Start a coroutine
    GlobalScope.launch {
        // delay() is ike Thread.sleep(), but better:
        // it doesn't block a thread, but only suspends the coroutine itself.
        // The thread is returned to the pool while the coroutine is waiting,
        // and when the waiting is done, the coroutine resumes on a free thread in the pool.
        // delay() is a suspend function and suspend functions are only allowed to be called
        // from a coroutine or another suspend function.
        delay(1000)
        println("World!")
    }

    println("Hello,")

    // The coroutines.examples.main thread must wait until our coroutine completes, otherwise the program ends before Hello is printed.
    // We could use Thread.sleep(2000) or be explicit about blocking by using the runBlocking coroutine builder.
    runBlocking {               // this expression blocks the coroutines.examples.main thread
        delay(2000L)  // delay for 2 seconds to keep JVM alive
    }
}

/**
 * coroutines.examples.firstCoroutineExample can be rewritten in a more idiomatic way,
 * using runBlocking to wrap the execution of the coroutines.examples.main function.
 * Here, the runBlocking<Unit> {...} works as an adaptor that is used to start the top-level coroutines.examples.main coroutine.
 * We explicitly specify its Unit return type, because a well-formed coroutines.examples.main function in Kotlin has to return Unit.
 *
 * Not ideal because we are using GlobalScope and delay.
 */
fun wrapInRunBlockingExample() = runBlocking<Unit> {
    GlobalScope.launch {
        delay(1000)
        println("World!")
    }

    println("Hello,")
    delay(2000L)
}

/**
 * The above two examples both delay for a time while another coroutine is working. That is NOT a good approach.
 * Here, we explicitly wait (in a non-blocking way) until the background job that we have launched is complete.
 *
 * Not ideal because we are using GlobalScope.
 */
fun waitingForAJobExample() = runBlocking {
    val job = GlobalScope.launch { // launch new coroutine and keep a reference to its Job
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    job.join() // wait until child coroutine completes
}

/**
 * When we use GlobalScope.launch we create a top-level coroutine.
 * Even though it is light-weight, it still consumes memory resources while it runs.
 * Having to manually keep reference to all the launched coroutines and join them is error prone.
 *
 * A better solution is to use "structured concurrency".
 * Instead of launching coroutines in the GlobalScope, just like we do with threads (threads are always global),
 * we can launch coroutines in the specific scope of the operation we are performing!
 *
 * Every coroutine builder (including runBlocking) adds an instance of CoroutineScope to the scope of its code block.
 * We can launch coroutines in this scope without having to join them explicitly,
 * because an outer coroutine doesn't complete until all coroutines launched in its scope complete.
 *
 * Unlike the implementations above, we are not using GlobalScope or delay to wait on other coroutines. This is good!
 */
fun structuredConcurrencyExample() = runBlocking {
    launch { // launch new coroutine in the scope of the outer coroutine (runBlocking)
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}


