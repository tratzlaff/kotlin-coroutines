package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import runExample

fun main(args: Array<String>) {
    runExample(::channelExample)
    runExample(::channelCloseExample)
}

/**
 * A Channel is conceptually very similar to a BlockingQueue.
 * Instead of a blocking put operation, it has a suspending send.
 * Instead of a blocking take operation, it has a suspending receive.
 *
 * Unbuffered channels transfer elements when sender and receiver meet each other (aka rendezvous).
 * If send is invoked first, then it is suspended until receive is invoked.
 * If receive is invoked first, it is suspended until send is invoked.
 */
fun channelExample() = runBlocking {

    val channel = Channel<Int>()

    launch {
        // sending five integers
        for (x in 1..5) channel.send(x)
    }

    // printing five received integers:
    repeat(5) { println(channel.receive()) }

    println("Done!\n")
}

/**
 * Unlike a queue, a channel can be closed to indicate that no more elements are coming.
 */
fun channelCloseExample() = runBlocking {

    val channel = Channel<Int>()

    launch {
        // sending five integers
        for (x in 1..5) channel.send(x)

        // A close is like sending a special close token to the channel.
        // The iteration stops as soon as this close token is received,
        // so there is a guarantee that all previously sent elements before the close are received.
        channel.close()

        // This won't be printed because the channel is closed.
        channel.send(6)
    }

    // printing received values using `for` loop (until the channel is closed)
    for (y in channel) println(y)

    println("Done!\n")
}