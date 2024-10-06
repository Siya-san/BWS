package com.example.bws.ui.models

import android.os.Parcel
import android.os.Parcelable

data class MyBirdSighting(var comName: String? = null,
                          var latitude: Double = 0.0,
                          var longitude: Double = 0.0,
                          var date: String? = null,
                          var UID: String? = null,
                          var image_url: String? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comName)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(date)
        parcel.writeString(UID)
        parcel.writeString(image_url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyBirdSighting> {
        override fun createFromParcel(parcel: Parcel): MyBirdSighting {
            return MyBirdSighting(parcel)
        }

        override fun newArray(size: Int): Array<MyBirdSighting?> {
            return arrayOfNulls(size)
        }
    }


}
