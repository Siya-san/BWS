package com.example.bws.ui.models

import android.os.Parcel
import android.os.Parcelable

data class BirdSighting(var comName: String? = null,var sciName: String? = null,var locName: String? = null,var obsDt: String? = null,) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),

        ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comName)
        parcel.writeString(sciName)
        parcel.writeString(locName)
        parcel.writeString(obsDt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BirdSighting> {
        override fun createFromParcel(parcel: Parcel): BirdSighting {
            return BirdSighting(parcel)
        }

        override fun newArray(size: Int): Array<BirdSighting?> {
            return arrayOfNulls(size)
        }
    }
}

