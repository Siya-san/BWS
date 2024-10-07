package com.example.bws.ui

import android.app.Application
import com.example.bws.ui.models.User
import com.example.bws.ui.models.UserSettings

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

    var userSettings: UserSettings? = null
        get() {
            println("Getting userSettings: $field")
            return field
        }
        set(value) {
            println("Setting userSettings to $value")
            field = value
        }

    companion object {
        @JvmStatic
        fun getInstance(application: Application): UserClient {
            return application as UserClient
        }
    }
}
