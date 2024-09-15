package com.example.bws.ui


import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle

import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.myapplication2.MainActivity
import com.example.myapplication2.R

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseUser


class Login : AppCompatActivity(),
View.OnClickListener{
    private val tag = "LoginActivity"
    //Firebase
   // private var mAuthListener: AuthStateListener? = null

    // widgets
    private var email: EditText? = null
    private var password: EditText? = null
    private lateinit var loginButton: Button
    private lateinit var goRegisterButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        email=findViewById(R.id.editTextEmailL)
        password=findViewById(R.id.editTextPassword)
        firebaseAuth = FirebaseAuth.getInstance()
       loginButton= findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener(this)
        goRegisterButton = findViewById(R.id.buttonGoRegistration)
        goRegisterButton.setOnClickListener(this)

        hideSoftKeyboard()


    }


    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }



     override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
        }
    }

 /*   override fun onStop() {
        super.onStop()
        if (firebaseAuth != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
        }
    }*/

    private fun signIn() {
        if (email?.text.toString().isNotEmpty()  && password?.text.toString().isNotEmpty()) {
            Log.d(tag, "onClick: attempting to authenticate.")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email?.text.toString(),
                password?.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this@Login,"Welcome Back",Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())

                } else {
                    Toast.makeText(this@Login, "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Toast.makeText(
                this@Login,
                "You didn't fill in all the fields.",
                Toast.LENGTH_SHORT
            ).show()

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