package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import runExample

fun main(args: Array<String>) {
    runExample(::fanInExample)
}

/**
 * Multiple coroutines may send to the same channel.
 * Here we have a channel of strings, and a suspending function that repeatedly
 * sends a specified string to this channel with a specified delay.
 * We launch a couple of coroutines sending strings.
 * In this example, we launch the coroutines in the context of the coroutines.examples.main thread as coroutines.examples.main coroutine's children.
 */
fun fanInExample() = runBlocking {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "bar", 500L) }
    repeat(6) { // receive first six
        println(channel.receive())
    }
    coroutineContext.cancelChildren() // cancel all children to let coroutines.examples.main finish

    println("Done!\n")
}

suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}





