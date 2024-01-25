import kotlinx.coroutines.*
import kotlin.random.Random

fun main() {
    runBlocking { //Dispatcher
        (1 until 100).forEach { index ->
            async(Dispatchers.Default) { //IO?
                simpleWork(index)
            }
        }
    }
}





