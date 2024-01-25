package fasit

import kotlinx.coroutines.*
import kotlin.random.Random

val tx: ThreadLocal<String> = ThreadLocal.withInitial{"NOT SET"}

fun main() {

    //WE USE SYSTEM.ERR FOR OUTPUT TO ENSURE BUFFER FLUSH

    tx.set("Main")
    System.err.println("In main, tx is ${tx.get()}")

    //Scenario 1 - naive, non-functioning coroutines
    //Observe that they are run in index-sequence, waiting for each other.
    //This happens because the work is "blocking" on the actual thread (Thread.sleep), and we are using the dispatcher
    //inherited from runblocking - which only has one thread.
    //For transactions, you will see bleeding of the threadlocal from job to job, and you will see that the last one
    // bleeds out to the main thread. This is okay for transactions as long as they are reentrant, but could be dangerous
    // if we called transactions with "requires new" and similar inside blockingwork.
    System.err.println("Scenario 1")
    runBlocking {
        (1 until 10).forEach { index -> async { blockingWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")

    //Scenario 2 - proper use of coroutines
    //Observe that we get paralellization. This is becasue in the nonblocking world, the "delay" method allows itself
    // to be suspended. (Easy to see, as calling it forces us to add the suspend keyword to the function.)
    //Having nonblocking work means transactions will bleed - you're not on the thread you think.
    System.err.println("Scenario 2")
    runBlocking {
        (1 until 10).forEach { index -> async { nonBlockingWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")

    //Scenario 3 - naive, false suspend
    //Observe that just adding "suspend" to the method we're calling does absolutely nothing. Indeed, intellij tells us
    // the suspend keyword is "redundant". We get the same result as in scenario 1.
    //Transaction situation is the same as in scenario 1.
    System.err.println("Scenario 3")
    runBlocking {
        (1 until 10).forEach { index -> async { fakeSuspendWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")

    //Scenario 4 - quickfix, run blocking work on the Default dispatcher
    //Observe that we get paralellization, but it's limited. This runs 50 tasks instead of 10, to make it visible.
    //You will see that threads that only slept for a second are printed very late in the order. This is because
    // the Default Dispatcher only has a number of threads to work with equal to your number of cores.
    //Transactions will bleed, AND crucially will sometimes be on the "NOT SET" state - meaning that we wouldn't inherit
    // the transaction of the launching task. Therefore, this "quickfix" is extremely dangeorus when you want all
    // tasks to be transactionally sound.
    System.err.println("Scenario 4")
    runBlocking {
        (1 until 50).forEach { index -> async(Dispatchers.Default) { blockingWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")

    //Scenario 5 - quickfix, run blocking work on the IO dispatcher
    //This gives us the same as with Scenario 4, but the IO dispatcher has 64 threads to work with. You are much less
    // likely to see "sleep 1"-results in the later half of the printout.
    //Transaction situation is the same as in 4, just on more threads.
    System.err.println("Scenario 5")
    runBlocking {
        (1 until 50).forEach { index -> async(Dispatchers.IO) { blockingWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")

    //Scenario 6 - naive, using launch instead of async
    //Launch will run a new coroutine, explicitly not blocking the current thread. However, you still don't get
    // paralellization, as the dispatcher is still inherited from runblocking.
    //Transactions as in scenario 1
    System.err.println("Scenario 6")
    runBlocking {
        (1 until 10).forEach { index -> launch { blockingWork(index) } }
    }

    System.err.println("In main, tx is ${tx.get()}. Resetting it to main.")
    tx.set("Main")
}

fun blockingWork(index: Int) {
    val outtertx = tx.get()
    tx.set("Blockingwork $index")
    val randomSleepSeconds = Random.nextInt(1, 5)
    Thread.sleep(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${tx.get()} within ${outtertx}")
}

suspend fun nonBlockingWork(index: Int) {
    val outtertx = tx.get()
    tx.set("nonBlockingwork $index")
    val randomSleepSeconds = Random.nextInt(1, 5)
    delay(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${tx.get()} within ${outtertx}")
}

suspend fun fakeSuspendWork(index: Int) {
    val outtertx = tx.get()
    tx.set("fakesuspendwork $index")
    val randomSleepSeconds = Random.nextInt(1, 5)
    Thread.sleep(randomSleepSeconds*1000L)
    System.err.println("Task $index slept for $randomSleepSeconds seconds. My transaction is ${tx.get()} within ${outtertx}")
}