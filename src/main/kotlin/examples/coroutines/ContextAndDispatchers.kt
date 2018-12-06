package examples.coroutines

import kotlinx.coroutines.*
import runExample

fun main(args: Array<String>) {
    runExample(::dispatchersAndThreadsExample)

}

/**
 * Coroutine context includes a coroutine dispatcher (see CoroutineDispatcher)
 * that determines what thread or threads the corresponding coroutine uses for its execution.
 * Coroutine dispatcher can confine coroutine execution to a specific thread,
 * dispatch it to a thread pool, or let it run unconfined.
 *
 * All coroutine builders like launch and async accept an optional CoroutineContext parameter
 * that can be used to explicitly specify the dispatcher for new coroutine and other context elements.
 */
fun dispatchersAndThreadsExample() = runBlocking<Unit> {

    // When launch { ... } is used without parameters, it inherits the context (and thus dispatcher)
    // from the CoroutineScope that it is being launched from. In this case, it inherits the context
    // of the main runBlocking coroutine which runs in the main thread.
    // The default dispatcher for runBlocking coroutine, in particular, is confined to the invoker thread,
    // so inheriting it has the effect of confining execution to this thread with a predictable FIFO scheduling.
    launch {
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking      : After delay in thread ${Thread.currentThread().name}")
    }

    // The Dispatchers.Unconfined coroutine dispatcher starts coroutine in the caller thread,
    // but only until the first suspension point. After suspension it resumes in the thread that
    // is fully determined by the suspending function that was invoked. Unconfined dispatcher is
    // appropriate when coroutine does not consume CPU time nor updates any shared data
    // (like UI) that is confined to a specific thread.
    // WARNING: Unconfined dispatcher should not be used in general code.
    launch(Dispatchers.Unconfined) {
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined            : After delay in thread ${Thread.currentThread().name}")
    }

    // The default dispatcher, that is used when coroutines are launched in GlobalScope,
    // is represented by Dispatchers.Default and uses shared background pool of threads,
    // so launch(Dispatchers.Default) { ... } uses the same dispatcher as GlobalScope.launch { ... }.
    launch(Dispatchers.Default) {
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }

    // newSingleThreadContext creates a new thread for the coroutine to run.
    // A dedicated thread is a very expensive resource. In a real application it must be either released, when
    // no longer needed, using close function, or stored in a top-level variable and reused throughout the application.
    launch(newSingleThreadContext("MyOwnThread")) {
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}



