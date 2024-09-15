package com.example.bws.ui
import android.app.Application
import com.example.bws.ui.models.User


class UserClient : Application() {

    var user: User? = null
        private set

    fun setUser(user: User?) {
        this.user = user
    }
}
