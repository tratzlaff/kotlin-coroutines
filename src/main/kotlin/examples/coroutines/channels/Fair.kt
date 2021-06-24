package examples.coroutines.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import runExample

fun main() {
    runExample(::fairExample)
}

/**
 * Send and receive operations to channels are fair with respect to the order of their invocation from multiple coroutines.
 * They are served in first-in first-out order, e.g. the first coroutine to invoke receive gets the element.
 * In the following example two coroutines "ping" and "pong" are receiving the "ball" object from the shared "table" channel.
 *
 * The "ping" coroutine is started first, so it is the first one to receive the ball.
 * Even though "ping" coroutine immediately starts receiving the ball again after sending it back to the table,
 * the ball gets received by the "pong" coroutine, because it was already waiting for it.
 */
fun fairExample() = runBlocking {
    val table = Channel<Ball>() // a shared table
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) // serve the ball
    delay(1000)

    coroutineContext.cancelChildren() // game over, cancel them
}

data class Ball(var hits: Int)

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { // receive the ball in a loop
        ball.hits++
        println("$name $ball")
        delay(300)
        table.send(ball) // send the ball back
    }
}
