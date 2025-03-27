package com.streakfreak.focusflow

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
//    private val API_URL = "http://10.0.2.2:8000/"
    private val API_URL = "http://192.168.1.10:8000/"
//    private val apiUrl = "http://192.168.1.10:8000/github/hajay180505?mock=false"
//    private val apiUrl = "https://jsonplaceholder.typicode.com/posts/1"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.switchButton).setOnClickListener{
        startActivity(Intent(this, CleanMainActivity::class.java))
    }
    val addApp = findViewById<Button>(R.id.addApp)

   /* val githubTextView = findViewById<TextView>(R.id.githubTextView)
    val leetcodeTextView = findViewById<TextView>(R.id.leetcodeTextView)
    val duolingoTextView = findViewById<TextView>(R.id.duolingoTextView)
*/
    //val outputTextView = findViewById<TextView>(R.id.outputTextView) // Placeholder for displaying all outputs

    val githubIcon = findViewById<ImageView>(R.id.githubIcon)
    val githubValue = findViewById<TextView>(R.id.githubValue)
    val githubDeleteButton = findViewById<Button>(R.id.delete1)

    val leetcodeIcon = findViewById<ImageView>(R.id.leetcodeIcon)
    val leetcodeValue = findViewById<TextView>(R.id.leetcodeValue)
    val leetcodeDeleteButton = findViewById<Button>(R.id.delete2)

    val duolingoIcon = findViewById<ImageView>(R.id.duolingoIcon)
    val duolingoValue = findViewById<TextView>(R.id.duolingoValue)
    val duolingoDeleteButton = findViewById<Button>(R.id.delete3)

    // Hide labels by default
    githubIcon.visibility = ImageView.GONE
    githubValue.visibility = TextView.GONE
    githubDeleteButton.visibility = Button.GONE

    leetcodeIcon.visibility = TextView.GONE
    leetcodeValue.visibility = TextView.GONE
    leetcodeDeleteButton.visibility = Button.GONE

    duolingoIcon.visibility = TextView.GONE
    duolingoValue.visibility = TextView.GONE
    duolingoDeleteButton.visibility = Button.GONE

    val db = Database(this)
    val usernameAppPairs = db.getUsernameAppPairs(db.readableDatabase)

//    usernameAppPairs.forEach { pair ->
//        Log.d("UsernameAppPairs", "Username: ${pair.first}, App: ${pair.second}")
//    }

    addApp.setOnClickListener {
        val intent = Intent(this, AddNewApp::class.java)
        startActivity(intent)
        refreshUI(db)
    }

    githubDeleteButton.setOnClickListener {
        val isDeleted = db.deleteByApp("github")
        githubIcon.visibility = TextView.GONE
        githubValue.visibility = TextView.GONE
        githubDeleteButton.visibility = Button.GONE
        refreshUI(db)
    }

    leetcodeDeleteButton.setOnClickListener {
        val isDeleted = db.deleteByApp("leetcode")
        leetcodeIcon.visibility = TextView.GONE
        leetcodeValue.visibility = TextView.GONE
        leetcodeDeleteButton.visibility = Button.GONE
        //refreshUI(db)
    }

    duolingoDeleteButton.setOnClickListener {
        val isDeleted = db.deleteByApp("duolingo")
        duolingoIcon.visibility = TextView.GONE
        duolingoValue.visibility = TextView.GONE
        duolingoDeleteButton.visibility = Button.GONE
        //refreshUI(db)
    }

    findViewById<Button>(R.id.button).setOnClickListener {
        /*githubTextView.text = "Loading..."
        leetcodeTextView.text = "Loading..."
        duolingoTextView.text = "Loading..."*/

        CoroutineScope(Dispatchers.IO).launch {
            usernameAppPairs.forEach { pair ->
                val app = pair.second.lowercase()
                val username = pair.first
                val response = makeApiRequest(app, username)

                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val (streak, detail) = parseResponse(response)

                        when (app) {
                            "github" -> {
                                githubIcon.visibility = ImageView.VISIBLE
                                githubValue.visibility = TextView.VISIBLE
                                githubDeleteButton.visibility = Button.VISIBLE
                                githubValue.text = "Contributions: $detail"
                                githubValue.setTextColor(
                                    if (streak)  R.color.darkerGreen
                                    else Color.RED
                                )
                            }
                            "leetcode" -> {
                                leetcodeIcon.visibility = TextView.VISIBLE
                                leetcodeValue.visibility = TextView.VISIBLE
                                leetcodeDeleteButton.visibility = TextView.VISIBLE
                                leetcodeValue.text = "Days: $detail" // Display only the `days` value
                                leetcodeValue.setTextColor(
                                    if (streak) ContextCompat.getColor(applicationContext, R.color.darkerGreen)
                                    else Color.RED
                                )
                            }
                            "duolingo" -> {
                                duolingoIcon.visibility = TextView.VISIBLE
                                duolingoValue.visibility = TextView.VISIBLE
                                duolingoDeleteButton.visibility = TextView.VISIBLE
                                duolingoValue.text = "Days: $detail" // Display only the `days` value
                                duolingoValue.setTextColor(
                                    if (streak) ContextCompat.getColor(applicationContext, R.color.darkerGreen)
                                    else Color.RED
                                )
                            }
                        }
                    }
                }
            }
        }
        /*CoroutineScope(Dispatchers.IO).launch {
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
        }*/

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

    private fun parseResponse(response: String): Pair<Boolean, String> {
        return try {
            val json = JSONObject(response) // Parse the response as JSON
            val streak = json.optBoolean("streak", false) // Extract "streak" (default to false if missing)
            val detail = when {
                json.has("contributions") -> json.optString("contributions", "Unknown")
                json.has("days") -> json.optString("days", "Unknown")
                else -> "Unknown"
            }
            Pair(streak, detail) // Return the parsed streak and detail as a pair
        } catch (e: Exception) {
            Pair(false, "Error parsing response") // Handle parsing errors gracefully
        }
    }

    private fun refreshUI(db: Database) {

        val githubIcon = findViewById<ImageView>(R.id.githubIcon)
        val githubValue = findViewById<TextView>(R.id.githubValue)
        val githubDeleteButton = findViewById<Button>(R.id.delete1)

        val leetcodeIcon = findViewById<ImageView>(R.id.leetcodeIcon)
        val leetcodeValue = findViewById<TextView>(R.id.leetcodeValue)
        val leetcodeDeleteButton = findViewById<Button>(R.id.delete2)

        val duolingoIcon = findViewById<ImageView>(R.id.duolingoIcon)
        val duolingoValue = findViewById<TextView>(R.id.duolingoValue)
        val duolingoDeleteButton = findViewById<Button>(R.id.delete3)

        // Fetch updated data from the database
        val updatedUsernameAppPairs = db.getUsernameAppPairs(db.readableDatabase)

        // Reset visibility of all UI components
        githubIcon.visibility = ImageView.GONE
        githubValue.visibility = TextView.GONE
        githubDeleteButton.visibility = Button.GONE

        leetcodeIcon.visibility = TextView.GONE
        leetcodeValue.visibility = TextView.GONE
        leetcodeDeleteButton.visibility = Button.GONE

        duolingoIcon.visibility = TextView.GONE
        duolingoValue.visibility = TextView.GONE
        duolingoDeleteButton.visibility = Button.GONE

        // Rebuild the UI with data fetched from the database
        updatedUsernameAppPairs.forEach { pair ->
            val app = pair.second.lowercase()
            val username = pair.first

            CoroutineScope(Dispatchers.IO).launch {
                val response = makeApiRequest(app, username)
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val (streak, detail) = parseResponse(response)

                        when (app) {
                            "github" -> {
                                githubIcon.visibility = TextView.VISIBLE
                                githubValue.visibility = TextView.VISIBLE
                                githubDeleteButton.visibility = Button.VISIBLE
                                githubValue.text = "Contributions: $detail"
                                githubValue.setTextColor(if (streak) Color.GREEN else Color.RED)
                            }
                            "leetcode" -> {
                                leetcodeIcon.visibility = TextView.VISIBLE
                                leetcodeValue.visibility = TextView.VISIBLE
                                leetcodeDeleteButton.visibility = Button.VISIBLE
                                leetcodeValue.text = "Days: $detail"
                                leetcodeValue.setTextColor(if (streak) Color.GREEN else Color.RED)
                            }
                            "duolingo" -> {
                                duolingoIcon.visibility = TextView.VISIBLE
                                duolingoValue.visibility = TextView.VISIBLE
                                duolingoDeleteButton.visibility = Button.VISIBLE
                                duolingoValue.text = "Days: $detail"
                                duolingoValue.setTextColor(if (streak) Color.GREEN else Color.RED)
                            }
                        }
                    }
                }
            }
        }
    }
}