import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce

fun main(args: Array<String>) {
    channelExample()
    channelCloseExample()
    pipelinePatternExample()
    primeExample()
    fanOutExample()
    fanInExample()
}

/**
 * A Channel is conceptually very similar to a BlockingQueue.
 * Instead of a blocking put operation, it has a suspending send.
 * Instead of a blocking take operation, it has a suspending receive.
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


/**
 * A pipeline is a pattern where one coroutine is producing a, possibly infinite, stream of values
 * and another coroutine (or coroutines) are consuming that stream, doing some processing, and producing other results.
 */
@ExperimentalCoroutinesApi
fun pipelinePatternExample() = runBlocking {
    val numbers = produceIntegers() // produces integers starting at 1 and never stopping
    val squares = square(numbers) // squares integers

    // print first five
    for (i in 1..5) println(squares.receive())
    println("Done!\n")

    // cancel children coroutines
    coroutineContext.cancelChildren()
}

@ExperimentalCoroutinesApi
fun CoroutineScope.produceIntegers() = produce<Int> {
    var x = 1
    while (true) send(x++) // infinite stream of integers starting from 1
}

@ExperimentalCoroutinesApi
fun CoroutineScope.square(integers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in integers) send(x * x)
}




@ExperimentalCoroutinesApi
fun primeExample() = runBlocking {
    var cur = numbersFrom(2)
    for (i in 1..10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
    }

    println("Done!\n")
    coroutineContext.cancelChildren() // cancel all children to let main finish
}

@ExperimentalCoroutinesApi
fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}

@ExperimentalCoroutinesApi
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}


/**
 * Multiple coroutines may receive from the same channel, distributing work between themselves.
 * Here we launch five processors and let them work for almost a second.
 */
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


/**
 * Multiple coroutines may send to the same channel.
 * Here we have a channel of strings, and a suspending function that repeatedly
 * sends a specified string to this channel with a specified delay.
 * We launch a couple of coroutines sending strings.
 * In this example, we launch the coroutines in the context of the main thread as main coroutine's children.
 */
fun fanInExample() = runBlocking {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "bar", 500L) }
    repeat(6) { // receive first six
        println(channel.receive())
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish

    println("Done!\n")
}

suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}





