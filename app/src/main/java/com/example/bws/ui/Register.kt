package com.example.bws.ui


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bws.ui.models.User
import com.example.myapplication2.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Register : AppCompatActivity(), View.OnClickListener {
    private val tag = "RegisterActivity"


    private var email: EditText? = null
    private var password: EditText? = null
    private var confirmPassword: EditText? = null
    private  var username : EditText?=null
    private lateinit var registerButton: Button



    private var mDb: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        email =  findViewById(R.id.editTextEmail)
        username = findViewById(R.id.editTextUsername)
        password =  findViewById(R.id.editTextPassword)
        confirmPassword = findViewById(R.id.editTextPassword2)


registerButton=  findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener(this)

        mDb = FirebaseFirestore.getInstance()

        hideSoftKeyboard()
    }
    private fun registerNewEmail(email: String, username:String?, password: String?) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password!!)
            .addOnCompleteListener { task ->
                Log.d(tag, "createUserWithEmail:onComplete:" + task.isSuccessful)
                if (task.isSuccessful) {
                    Log.d(
                        tag, "onComplete: AuthState: " + FirebaseAuth.getInstance().currentUser!!
                            .uid
                    )


                    val user = User().apply {
                        this.email = email
                        this.username = username
                        userId = FirebaseAuth.getInstance().uid
                    }



                    val newUserRef = mDb
                        ?.collection(getString(R.string.collection_users))
                        ?.document(FirebaseAuth.getInstance().uid!!)
                    newUserRef?.set(user)?.addOnCompleteListener {

                        if (task.isSuccessful) {
                            redirectLoginScreen()
                        } else {
                            val parentLayout = findViewById<View>(android.R.id.content)
                            Snackbar.make(
                                parentLayout,
                                "Something went wrong.",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {
                    val parentLayout = findViewById<View>(android.R.id.content)
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT)
                        .show()

                }


            }
    }
    private fun redirectLoginScreen() {
        Log.d(tag, "redirectLoginScreen: redirecting to login screen.")
        val intent = Intent(
            this@Register,
            Login::class.java
        )
        startActivity(intent)
        finish()
    }




    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonRegister -> {
                Log.d(tag, "onClick: attempting to register.")

                //check for null valued EditText fields
                if (email?.text.toString().isNotEmpty()
                    && username?.text.toString().isNotEmpty()
                    && password?.text.toString().isNotEmpty()
                    && confirmPassword?.text.toString().isNotEmpty()
                ) {

                    //check if passwords match
                    if (password?.text.toString() == confirmPassword?.text.toString()) {

                        //Initiate registration task
                        registerNewEmail(
                            email?.text.toString(),
                            username?.text.toString(),
                            password?.text.toString()
                        )
                    } else {
                        Toast.makeText(
                            this@Register,
                            "Passwords do not Match",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@Register,
                        "You must fill out all the fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}