package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import runExample

fun main(args: Array<String>) {
    runExample(::pipelinePatternExample)
    runExample(::primeExample)
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

    // Since all the coroutines are launched in the scope of the main runBlocking coroutine
    // we don't have to keep an explicit list of all the coroutines we have started.
    // We use cancelChildren extension function to cancel all the children coroutines.
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

    // Since all the coroutines are launched in the scope of the main runBlocking coroutine
    // we don't have to keep an explicit list of all the coroutines we have started.
    // We use cancelChildren extension function to cancel all the children coroutines.
    coroutineContext.cancelChildren()
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





