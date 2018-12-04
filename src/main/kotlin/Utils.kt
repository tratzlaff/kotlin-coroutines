import kotlin.system.measureTimeMillis

/**
 * Measure and log time to run.
 */
fun runExample(f: () -> Unit, lineBreak: Boolean = false) {
    val time = measureTimeMillis(f)
    if(lineBreak) println("")
    println("Completed in $time ms\n")
}