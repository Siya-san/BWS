package com.example.bws.ui
import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import com.example.bws.ui.models.User


class UserClient() : Application() {

    var user: User? = null
        get() {
            println("Getting count: $field")
            return field
        }
        set(value) {
            println("Setting count to $value")
            field = value
        }

    companion object {
        var user: User? = null
    }


}
