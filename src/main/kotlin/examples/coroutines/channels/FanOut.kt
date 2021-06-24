package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import runExample

@ExperimentalCoroutinesApi
fun main() {
    runExample(::fanOutExample)
}

/**
 * Multiple coroutines may receive from the same channel, distributing work between themselves.
 * Here we launch five processors and let them work for almost a second.
 */
@ExperimentalCoroutinesApi
fun fanOutExample() = runBlocking<Unit> {
    val producer = produceNumbers()
    repeat(5) { launchProcessor(it, producer) }
    delay(950)

    // Cancelling a producer coroutine closes its channel,
    // thus eventually terminating the iteration over the channel which is being done by the processor coroutines.
    producer.cancel()

    println("Done!\n")
}

/** This producer coroutine periodically produces integers (ten per second). */
@ExperimentalCoroutinesApi
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1 // start from 1
    while (true) {
        send(x++) // produce next
        delay(100) // wait 0.1s
    }
}

/** We can have several processor coroutines. */
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    // This for loop pattern is safe to use from multiple coroutines (unlike consumeEach).
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}





