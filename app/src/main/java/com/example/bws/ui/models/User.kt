package com.example.bws.ui.models


import android.os.Parcel
import android.os.Parcelable

data class User(
    var email: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var avatar: String? = null

) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(userId)
        parcel.writeString(username)
        parcel.writeString(avatar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "User(email=$email, user_id=$userId, username=$username, avatar=$avatar)"
    }
}


