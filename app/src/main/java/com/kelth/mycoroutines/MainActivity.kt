package com.kelth.mycoroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private lateinit var job:Job

    private lateinit var textViewLog:TextView
    private lateinit var textViewCounter:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeCtrl()
    }

    private fun initializeCtrl() {

        textViewLog = findViewById(R.id.textView_log)
        textViewCounter = findViewById(R.id.textView_counter)

        // Example launch coroutine with dispatcher IO
        findViewById<Button>(R.id.button_start_launch_dispatcher_io).setOnClickListener {
            textViewLog.text = "onButtonStartLaunchDispatcherIO start"

// Expected behaviour: Coroutine 1 & 3 executed. After 1 & 3 completed, 2 & 4 will be executed.
//            GlobalScope.launch(Dispatchers.IO) {
//                startCoroutine(1)
//                startCoroutine(2)
//            }
//            GlobalScope.launch(Dispatchers.IO) {
//                startCoroutine(3)
//                startCoroutine(4)
//            }
            // Same as above
            GlobalScope.launch(Dispatchers.IO) {
                launch { startCoroutine(1) }
                launch { startCoroutine(2) }
                launch { startCoroutine(3) }
                launch { startCoroutine(4) }
            }
        }

        // Example run blocking
        findViewById<Button>(R.id.button_start_run_blocking).setOnClickListener {

            textViewLog.text = "onButtonStartRunBlocking start"

            // Expected behaviour: UI will only show "End of run blocking"
            // Choreographer: Skipped 183 frames!  The application may be doing too much work on its main thread.
            runBlocking {
                textViewLog.text = "Start of run blocking"
                delay(3000)
                textViewLog.text = "End of run blocking"
            }
        }

        // Example cancel coroutine
        val toggleButton = findViewById<ToggleButton>(R.id.button_toggle_start_cancel)
        toggleButton.setOnClickListener {

            if (toggleButton.isChecked) {
                textViewLog.text = "onButtonToggleStart"
                job = GlobalScope.launch {
                    startCoroutine(1)
                    toggleButton.isChecked = false // Coroutine completed
                }
            } else {
                textViewLog.text = "onButtonToggleCancel"
                job.cancel()
            }
        }

        // Example async
        findViewById<Button>(R.id.button_start_async).setOnClickListener {
            textViewLog.text = "onButtonStartUsingAsync start"

            GlobalScope.launch {
                // Both will run concurrently
                val job1 = async { startCoroutine(1) }
                val job2 = async { startCoroutine(2) }
                // await will wait for coroutine to complete
                updateUILog("async coroutine ${job1.await()} completed")
                updateUILog("async coroutine ${job2.await()} completed")
            }
        }

        // Example Timeout
        findViewById<Button>(R.id.button_start_with_timeout).setOnClickListener {
            textViewLog.text = "onButtonStartWithTimeout start"
            // Timeout will be triggered after 4 secs even though second coroutine has not finished
            GlobalScope.launch(Dispatchers.IO) {
                withTimeout(4000) {
                    startCoroutine(1)
                    startCoroutine(2)
                }
            }
        }
    }

    suspend fun updateUILog(log: String) {
        // A coroutine started on Dispatchers.Main won't block the main thread while suspended
        withContext(Dispatchers.Main) {
            textViewLog.text = textViewLog.text.toString() + "\n$log"
        }
    }

    // coroutine
    suspend fun startCoroutine(index: Int) : Int {

        updateUILog("Hello from coroutine $index")
        val timeTaken = measureTimeMillis {
            delay(3000) // Stimulate doing something intensive...
        }
        updateUILog("Goodbye from coroutine $index, $timeTaken ms")
        return index
    }
}
