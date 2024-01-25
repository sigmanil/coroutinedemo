import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

val executorService = Executors.newFixedThreadPool(8)
val transaction: ThreadLocal<String> = ThreadLocal.withInitial{"NOT SET"}

fun main() {

    transaction.set("Main")
    System.err.println("In main, tx is ${transaction.get()}")

    val futures = (1 until 5).map { index ->
        CompletableFuture.supplyAsync({
            simpleReturningWork(index)
        }, executorService)
    }

    CompletableFuture.allOf(*futures.toTypedArray())

    futures.forEach { it.get() }

    System.err.println("In main, tx is ${transaction.get()}")

    executorService.shutdown()
}

fun simpleReturningWork(index: Int): String {
    val outtertx = transaction.get()
    transaction.set("Blockingwork $index")

    val randomSleepSeconds = Random.nextInt(1, 5)
    Thread.sleep(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${transaction.get()} within ${outtertx}.")
    return "Task $index result"
}