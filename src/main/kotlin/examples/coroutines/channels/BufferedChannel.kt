package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import runExample

fun main(args: Array<String>) {
    runExample(::bufferedChannelExample)
}

/**
 * Both Channel() factory function and produce builder take an optional capacity parameter to specify buffer size.
 * Buffer allows senders to send multiple elements before suspending, similar to the BlockingQueue with a specified
 * capacity, which blocks when buffer is full.
 */
fun bufferedChannelExample() = runBlocking {

    val channel = Channel<Int>(4) // create buffered channel

    val sender = launch { // launch sender coroutine

        // This will only print to 4 (because of buffer capacity)
        repeat(10) {
            println("Sending $it")
            channel.send(it) // will suspend when buffer is full
        }
    }

    // don't receive anything... just wait....
    delay(1000)

    sender.cancel() // cancel sender coroutine
}