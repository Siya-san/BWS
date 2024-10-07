package com.example.bws.ui


import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle

import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.bws.MainActivity
import com.example.bws.ui.models.UserSettings
import com.example.myapplication2.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Login : AppCompatActivity(), View.OnClickListener {
    private val tag = "LoginActivity"

    // widgets
    private var email: EditText? = null
    private var password: EditText? = null
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var loginButton: Button
    private lateinit var goRegisterButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressBar: ProgressBar // Add a ProgressBar variable

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.editTextEmailL)
        password = findViewById(R.id.editTextPassword)
        firebaseAuth = FirebaseAuth.getInstance()
        loginButton = findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener(this)
        goRegisterButton = findViewById(R.id.buttonGoRegistration)
        goRegisterButton.setOnClickListener(this)
        fireStore = FirebaseFirestore.getInstance()
       // progressBar = findViewById(R.id.progressBar) // Initialize ProgressBar

       // hideSoftKeyboard()
    }

    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun signIn() {
        if (email?.text.toString().isNotEmpty() && password?.text.toString().isNotEmpty()) {
            Log.d(tag, "onClick: attempting to authenticate.")

            // Show the ProgressBar
           // progressBar.visibility = View.VISIBLE

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email?.text.toString(),
                password?.text.toString()
            ).addOnCompleteListener { task ->
                // Hide the ProgressBar after login attempt
                //progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(this@Login, "Welcome Back", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                    getUserSettings { userSettings -> userSettings?.let { settings ->
                        (applicationContext as UserClient).userSettings = settings
                    }}
                } else {
                    Toast.makeText(this@Login, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@Login, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getUserSettings(onSettingsReceived: (UserSettings?) -> Unit) {
        val locationsRef = fireStore.collection(getString(R.string.collection_user_settings)).document(FirebaseAuth.getInstance().uid!!)

        locationsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // Convert Firestore document to UserLocation object
                    val userSettings = document.toObject(UserSettings::class.java)
                    Log.d(ContentValues.TAG, "User Settings retrieved: $userSettings")
                    onSettingsReceived(userSettings) // Pass the userPosition back
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                    onSettingsReceived(null)
                }
            } else {
                Log.e(ContentValues.TAG, "Error getting document: ${task.exception}")
                onSettingsReceived(null)
            }
        }
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonGoRegistration -> {
                val intent = Intent(
                    this@Login,
                    Register::class.java
                )
                startActivity(intent)
            }

            R.id.buttonLogin -> {
                signIn()

            }
        }
    }
}