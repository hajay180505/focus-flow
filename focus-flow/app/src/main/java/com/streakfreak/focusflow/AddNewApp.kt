package com.streakfreak.focusflow

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddNewApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_new_app)

        val appSpinner = findViewById<Spinner>(R.id.spinner)

        val usernameEditText = findViewById<EditText>(R.id.usernameInput)

        val addUserButton = findViewById<Button>(R.id.add)

        val db = Database(this)

        findViewById<Button>(R.id.back).setOnClickListener{
            finish()
        }

        addUserButton.setOnClickListener {
            val selectedApp = appSpinner.selectedItem.toString().lowercase()
            Toast.makeText(this, "selected app = $selectedApp", Toast.LENGTH_SHORT).show()
            val enteredUsername = usernameEditText.text.toString().trim()

            if (enteredUsername.isNotEmpty()) {
                val result = db.addUser(enteredUsername, selectedApp)
                if (result > 0) {
                    Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show()
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
}