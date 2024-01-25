import kotlinx.coroutines.*
import kotlin.random.Random

val tx: ThreadLocal<String> = ThreadLocal.withInitial{"NOT SET"}

fun main() {
    tx.set("Main")
    System.err.println("In main, tx is ${tx.get()}")

    runBlocking(Dispatchers.Default) {
        (1 until 100).forEach { index -> //Or 200?
            async {
                blockingWorkWithTL(index)
            }
        }
    }

    System.err.println("In main, tx is ${tx.get()}.")
}

fun blockingWorkWithTL(index: Int) {
    val outtertx = tx.get()
    tx.set("Blockingwork $index")

    val randomSleepSeconds = Random.nextInt(0, 3)
    Thread.sleep(randomSleepSeconds*1000L)

    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${tx.get()} within ${outtertx}")
}

suspend fun nonBlockingWorkWithTL(index: Int) {
    val outtertx = tx.get()
    tx.set("nonBlockingwork $index")

    val randomSleepSeconds = Random.nextInt(0, 3)
    delay(randomSleepSeconds*1000L)

    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${tx.get()} within ${outtertx}")
}

