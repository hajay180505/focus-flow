package com.streakfreak.focusflow

import android.os.Bundle
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val API_URL = "http://10.0.2.2:8000/"
//    private val apiUrl = "http://192.168.1.10:8000/github/hajay180505?mock=false"
//    private val apiUrl = "https://jsonplaceholder.typicode.com/posts/1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

    val githubTextView = findViewById<TextView>(R.id.githubTextView)
    val leetcodeTextView = findViewById<TextView>(R.id.leetcodeTextView)
    val duolingoTextView = findViewById<TextView>(R.id.duolingoTextView)

    findViewById<Button>(R.id.button).setOnClickListener {
        githubTextView.text = "Loading..."
        leetcodeTextView.text = "Loading..."
        duolingoTextView.text = "Loading..."

        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiRequest(App.GITHUB.value, "hajay180505", "?weekly=false", "mock=true")
            withContext(Dispatchers.Main) {
                githubTextView.text = response
                Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiRequest(App.LEETCODE.value, "Ajay_180505")
            withContext(Dispatchers.Main) {
                leetcodeTextView.text = response
                Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiRequest(App.DUOLINGO.value, "hajay180505")
            withContext(Dispatchers.Main) {
                duolingoTextView.text = response
                Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
            }
        }

    }
        // Use CoroutineScope to make the API request
//        CoroutineScope(Dispatchers.IO).launch {
//            val response = makeApiRequest(App.GITHUB.value, "hajay180505", "?weekly=true")
//            withContext(Dispatchers.Main) {
//                textView.text = response
//                Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
//            }
//        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private suspend fun makeApiRequest(app: String, username :String, vararg params : String ): String {
        val client = HttpClient(Android)
        return try {
            var requestUrl = "$API_URL$app/$username" + params.joinToString(separator = "&")
            val response: HttpResponse = client.get(requestUrl)
            response.bodyAsText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        } finally {
            client.close()
        }
    }
}