import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    runExample(::firstCoroutineExample)
    runExample(::wrapInRunBlockingExample)
    runExample(::waitingForAJobExample)
    runExample(::structuredConcurrencyExample)
    runExample(::scopeBuilderExample)
    runExample(::extractFunctionRefactoringExample)
    runExample(::coroutinesAreLightWeightExample, true)
    runExample(::globalCoroutinesAreLikeDaemonThreadsExample)
    runExample(::notFastWithThreadsExample)
    runExample(::coroutinesMightNotFinishBeforeResultIsOutputExample)
    runExample(::asyncExample)
    runExample(::structuredConcurrencyWithAsyncExample)
}

fun runExample(f: () -> Unit, lineBreak: Boolean = false) {
    val time = measureTimeMillis(f)
    if(lineBreak) println("")
    println("Completed in $time ms\n")
}

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

    // The main thread must wait until our coroutine completes, otherwise the program ends before Hello is printed.
    // We could use Thread.sleep(2000) or be explicit about blocking by using the runBlocking coroutine builder.
    runBlocking {               // this expression blocks the main thread
        delay(2000L)  // delay for 2 seconds to keep JVM alive
    }
}


/**
 * firstCoroutineExample can be rewritten in a more idiomatic way, using runBlocking to wrap the execution of the main function.
 * Here, the runBlocking<Unit> {...} works as an adaptor that is used to start the top-level main coroutine.
 * We explicitly specify its Unit return type, because a well-formed main function in Kotlin has to return Unit.
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
 * The above two examples both delay for a time while another coroutine is working. That is not a good approach.
 * Here, we explicitly wait (in a non-blocking way) until the background job that we have launched is complete.
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
 * A better solution is to use structured concurrency.
 * Instead of launching coroutines in the GlobalScope, just like we do with threads (threads are always global),
 * we can launch coroutines in the specific scope of the operation we are performing!
 *
 * Every coroutine builder (including runBlocking) adds an instance of CoroutineScope to the scope of its code block.
 * We can launch coroutines in this scope without having to join them explicitly,
 * because an outer coroutine doesn't complete until all coroutines launched in its scope complete.
 *
 */
fun structuredConcurrencyExample() = runBlocking {
    launch { // launch new coroutine in the scope of the outer coroutine (runBlocking)
        delay(1000L)
        println("World!")
    }
    println("Hello,")
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

    coroutineScope { // Creates a new coroutine scope
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // This line will be printed before nested launch
    }

    println("Coroutine scope is over") // This line is not printed until nested launch completes
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
 * This will launch 100k coroutines and have each print a dot.
 */
fun coroutinesAreLightWeightExample() = runBlocking {
    repeat(100_000) { // launch a lot of coroutines
        launch {
            delay(1000L)
            print(".")
        }
    }
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



/**
 * Runs a million threads each of which adds to a common counter.
 * Not fast.
 */
fun notFastWithThreadsExample() {
    val c = AtomicLong()

    for (i in 1..300_000) {
        thread(start = true) {
            c.addAndGet(i.toLong())
        }
    }

    println(c.get())
}

/**
 * This example completes fast, but prints arbitrary number because some coroutines don't finish before main() prints result.
 */
fun coroutinesMightNotFinishBeforeResultIsOutputExample() {
    val c = AtomicLong()

    for (i in 1..999_999) {
        GlobalScope.launch {
            c.addAndGet(i.toLong())
        }
    }

    println(c.get())
}


fun asyncExample() = runBlocking {

    // This would eat up 277 hours, if not run in parallel.
    val deferred = (1..999_999).map { n ->
        async {
            delay(1000)
            n
        }
    }
    val sum = deferred.sumBy { it.await() }
    println("Sum: $sum")
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
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}



