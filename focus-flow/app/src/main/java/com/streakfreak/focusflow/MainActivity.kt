package com.streakfreak.focusflow

import android.os.Bundle
import android.os.Looper
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
//    private val apiUrl = "http://192.168.1.10:8000/github/hajay180505?mock=false"
    private val apiUrl = "http://192.168.1.10:8000/duolingo/hajay180505 "
//    private val apiUrl = "https://jsonplaceholder.typicode.com/posts/1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)

        // Use CoroutineScope to make the API request
        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiRequest()
            withContext(Dispatchers.Main) {
                textView.text = response
                Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private suspend fun makeApiRequest(): String {
        val client = HttpClient(Android)
        return try {
            val response: HttpResponse = client.get(apiUrl)
            println(response.bodyAsText())
            response.bodyAsText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        } finally {
            client.close()
        }
    }
}