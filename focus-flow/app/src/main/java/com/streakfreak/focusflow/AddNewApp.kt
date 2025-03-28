package com.streakfreak.focusflow

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
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
import java.util.Arrays

class AddNewApp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_new_app)

        val appSpinner = findViewById<Spinner>(R.id.spinner)
        // Replace with your Spinner ID

        // Sample data (replace with your actual data)
        val items = Arrays.asList("Github", "Leetcode", "Duolingo")

        // Create and set the custom adapter
        val adapter = CustomSpinnerAdapter(this, items)
        appSpinner.adapter = adapter

        val usernameEditText = findViewById<EditText>(R.id.usernameInput)

        val addUserButton = findViewById<Button>(R.id.add)

        var isContentSet = false
        var isChecked = false

        val db = Database(this)

        findViewById<Button>(R.id.check).setOnClickListener{
            if (isContentSet){
                val selectedApp = appSpinner.selectedItem.toString().lowercase()
                val enteredUsername = usernameEditText.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    val response = makeApiRequest(selectedApp, enteredUsername)
                    println("response $response")
                    withContext(Dispatchers.Main) {
                       if (response == 404){
                           isChecked = false
                           isContentSet = false
                           usernameEditText.text.clear()

                           Toast.makeText(this@AddNewApp, "User with $enteredUsername does not exist in $selectedApp", Toast.LENGTH_LONG).show()
                       }
                       else{
                             isChecked = true
                           Toast.makeText(this@AddNewApp, "Please click add to add the user to database.", Toast.LENGTH_LONG).show()
                       }
                    }
                }
            }
        }

        addUserButton.setOnClickListener {
            val selectedApp = appSpinner.selectedItem.toString().lowercase()
//            Toast.makeText(this, "selected app = $selectedApp", Toast.LENGTH_SHORT).show()
            val enteredUsername = usernameEditText.text.toString().trim()

            if (enteredUsername.isNotEmpty()) {
                isContentSet = true
                val exists = db.getUsernameAppPairs(db.readableDatabase)
                for (item in exists){
                    if (item.first == enteredUsername && item.second == selectedApp){
                        Toast.makeText(this, "Username $enteredUsername already exists in app $selectedApp!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                if(!isChecked){
                    Toast.makeText(this, "Please click check", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val result = db.addUser(enteredUsername, selectedApp)
                if (result > 0) {
                    Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show()
                    CoroutineScope(Dispatchers.IO).launch {
                        val response :Int = makeApiRequestToGetStreak(selectedApp, enteredUsername)
                        println("response $response")
                        withContext(Dispatchers.Main) {
                            db.setStreakWithUserNameAndApp(enteredUsername, selectedApp, response)
                        }
                    }
                    finish() // Close the activity after success
                } else {
                    Toast.makeText(this, "Failed to add user.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid username.", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private suspend fun makeApiRequest(app: String, username :String ): Int {
        val client = HttpClient(Android)
        return try {
            var requestUrl = "${App.API_URL.value}$app/$username"
            println(requestUrl)
            val response: HttpResponse = client.get(requestUrl)
            println("Response :"+response.toString())
            response.status.value
        }
        catch (e: Exception) {
            200
        }
        finally {
            client.close()
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
                parseResponse(response.bodyAsText())
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



    private fun parseResponse(response: String): Int {
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