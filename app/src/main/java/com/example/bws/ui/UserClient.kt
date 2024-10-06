package com.example.bws.ui

import android.app.Application
import com.example.bws.ui.models.User

class UserClient : Application() {

    var user: User? = null
        get() {
            println("Getting user: $field")
            return field
        }
        set(value) {
            println("Setting user to $value")
            field = value
        }

    companion object {
        fun getInstance(application: Application): UserClient {
            return application as UserClient
        }
    }
}
