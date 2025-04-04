package com.streakfreak.focusflow

import android.content.Context
import android.content.Intent
import android.icu.util.TimeUnit
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.PeriodicWorkRequestBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Duration

class MainActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listView: ListView
    private lateinit var itemAdapter: ItemAdapter
    private var itemList = mutableListOf<ListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)


        if(!checkForInternet(this)){
            swipeRefreshLayout.visibility = View.GONE
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            findViewById<ConstraintLayout>(R.id.noInternet).visibility = View.VISIBLE
        }
        val listView = findViewById<ListView>(R.id.listView)

        refetch()

        itemAdapter = ItemAdapter(this, itemList)
        listView.adapter = itemAdapter
        listView.visibility = View.VISIBLE

        swipeRefreshLayout.setOnRefreshListener {
            refetch()
            itemAdapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
            startActivity(Intent(this, AddNewApp::class.java))
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    fun refetch() {
        val db = Database(this)
        val usernameAppPairs = db.getUsernameAppPairs(db.readableDatabase)

        CoroutineScope(Dispatchers.IO).launch {
            val newList = mutableListOf<ListItem>()
            usernameAppPairs.forEach { pair ->
                val app = pair.second.lowercase()
                val username = pair.first
                var response : String
                if(app == "github"){
                    response = makeApiRequest(app, username, "?mock=false")
                }
                else{
                    response = makeApiRequest(app, username)

                }

                    val response1 :Int = makeApiRequestToGetStreak(app, username)
                    Log.d("responsemine",app + " " + username + " " + response1.toString())
                    withContext(Dispatchers.Main) {
                        db.setStreakWithUserNameAndApp(username, app,response1)
                    }

                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val (streak, detail) = parseResponse(response)
                        println("FF :" + streak.toString() + detail)
                        when (app) {
                            "github" -> {
                                newList.add(ListItem(
                                    image = R.drawable.github,
                                    title = "Github",
                                    subtitle = username,
                                    color = if (streak) R.color.darkerGreen  else R.color.alertColor,
                                    streak = detail
                                ))

                            }
                            "leetcode" -> {
                                newList.add(ListItem(
                                    image = R.drawable.code,
                                    title = "Leetcode",
                                    subtitle = username,
                                    color = if (streak) R.color.darkerGreen  else R.color.alertColor,
                                    streak = detail
                                ))
                            }
                            "duolingo" -> {
                                newList.add(ListItem(
                                    image = R.drawable.duo,
                                    title = "Duolingo",
                                    subtitle = username,
                                    color = if (streak) R.color.darkerGreen  else R.color.alertColor,
                                    streak = detail
                                ))
                            }
                        }
                    }
                }
            }
            itemList.clear()
            itemList.addAll(newList)
        }
        Toast.makeText(this, "${itemList.size > 0} ", Toast.LENGTH_SHORT).show()
    }

    private suspend fun makeApiRequest(app: String, username :String, vararg params : String ): String {
        val client = HttpClient(Android)
        return try {
            var requestUrl = "${App.API_URL.value}$app/$username" + params.joinToString(separator = "&")
            println(requestUrl)
            val response: HttpResponse = client.get(requestUrl)
            println("Response :"+response.toString())
            response.bodyAsText()
        }
        catch (e: Exception) {
            when{
                app == App.DUOLINGO.value ->{
                    return "{" +
                            "  \"streak\": false," +
                            "  \"days\": 1692" +
                            "}"
                }
                app== App.GITHUB.value ->{
                    return "{" +
                            "  \"streak\": true," +
                            "  \"contributions\": 1" +
                            "}"
                }
                app== App.LEETCODE.value ->{
                    return "{" +
                            "  \"streak\": true," +
                            "  \"days\": 2" +
                            "}"

                }
                else -> "Error: ${e.message}"
            }
        }
        finally {
            client.close()
        }
    }

    private fun parseResponse(response: String): Pair<Boolean, String> {
        return try {
            val json = JSONObject(response) // Parse the response as JSON
            Log.d("TAG:","r"+response)

            val streak = json.optBoolean("streak", false) // Extract "streak" (default to false if missing)
            val detail = when {
                json.has("contributions") -> json.optString("contributions", "Unknown")
                json.has("days") -> json.optString("days", "Unknown")
                else -> "Unknown"
            }
            Pair(streak, detail) // Return the parsed streak and detail as a pair
        } catch (e: Exception) {
            println(e)

            Pair(false, "Error parsing response") // Handle parsing errors gracefully
        }
    }

    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }

    private suspend fun makeApiRequestToGetStreak(app: String, username :String ): Int {
        val client = HttpClient(Android)
        return try {
            var requestUrl = "${App.API_URL.value}$app/$username"
            println(requestUrl)
            val response: HttpResponse = client.get(requestUrl)
            println("Response :"+response.toString())
            if (response.status.value ==200){
                parseStreakResponse(response.bodyAsText())
            }
            else{
                0
            }
        }
        catch (e: Exception) {
            200
        }
        finally {
            client.close()
        }
    }



    private fun parseStreakResponse(response: String): Int {
        return try {
            val json = JSONObject(response) // Parse the response as JSON
            Log.d("TAG1:","r"+response)

            val streak = json.optBoolean("streak", false) // Extract "streak" (default to false if missing)
            // Return the parsed streak and detail as a pair
            if (streak) 1 else 0
        } catch (e: Exception) {
            println(e)
            0
// Handle parsing errors gracefully
        }
    }

}