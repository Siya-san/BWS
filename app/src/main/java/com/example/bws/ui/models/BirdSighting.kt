package com.example.bws.ui.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class BirdSighting(
    var user: String? = null,
    var name: String? = null,
    var geo_point: GeoPoint? = null,
    var imageString: String? = null,

    @ServerTimestamp
    var timestamp: Date? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        user = parcel.readString(),
        name = parcel.readString(),
        geo_point = GeoPoint(parcel.readDouble(), parcel.readDouble()), // Manually reading GeoPoint
        imageString = parcel.readString(),
        timestamp = Date(parcel.readLong()) // Manually reading Date as long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user)
        parcel.writeString(name)

        // Manually writing GeoPoint values
        parcel.writeDouble(geo_point?.latitude ?: 0.0)
        parcel.writeDouble(geo_point?.longitude ?: 0.0)

        parcel.writeString(imageString)

        // Writing Date as long
        timestamp?.let {
            parcel.writeLong(it.time)
        } ?: parcel.writeLong(0L)
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

    override fun toString(): String {
        return "BirdSighting(uid=$user, geo_point=$geo_point, timestamp=$timestamp)"
    }
}
