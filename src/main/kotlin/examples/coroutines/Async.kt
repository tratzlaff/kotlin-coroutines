package examples.coroutines

import kotlinx.coroutines.*
import runExample
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

fun main() {
    runExample(::coroutinesMightNotFinishBeforeResultIsOutputExample)
    runExample(::asyncExample)
}

/**
 * This example completes fast, but prints arbitrary number because some coroutines don't finish before coroutines.examples.main() prints result.
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
