import kotlinx.coroutines.*
import kotlin.random.Random

fun main() {
    runBlocking { //Or some other way to get to a coroutinescope
        val results = (1 until 5).map { index ->
            async { //or launch
                simpleWork(index)
            }
        }

        System.err.println(awaitAll(*results.toTypedArray()))
    }

}

fun simpleWork(index: Int): String { //Suspend?
    val randomSleepSeconds = Random.nextInt(1, 5)
    Thread.sleep(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds.")

    return "Result of $index"
}





























suspend fun simpleNonBlockingWork(index: Int):String {
    val randomSleepSeconds = Random.nextInt(1, 5)
    delay(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds.")
    return "Result of $index"
}

























suspend fun slightlyLessSimpleWork(index: Int): String {
    val randomSleepSeconds = Random.nextInt(1, 5)
    suspendSleep(randomSleepSeconds)
    System.err.println("Task $index slept for $randomSleepSeconds seconds.")

    return "Result of $index"
}

suspend fun suspendSleep(randomSleepSeconds: Int) {
    suspendCancellableCoroutine { continuation ->
        Thread.sleep(randomSleepSeconds*1000L)
        continuation.resume( value ="Done", onCancellation = {})
    }
}




