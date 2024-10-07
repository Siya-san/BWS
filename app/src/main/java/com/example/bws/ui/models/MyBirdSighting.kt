package com.example.bws.ui.models

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class MyBirdSighting(){
    private var uid: String? = null
    private var name: String? = null
    private var geo_point: GeoPoint? = null
    private var imageString:String?=null

    @ServerTimestamp
    private var timestamp: Date? = null
    fun UserLocation(uid: String?,name:String?, geo_point: GeoPoint?, timestamp: Date?) {
        this.uid = uid
        this.name=name
        this.geo_point = geo_point
        this.timestamp = timestamp
    }

    fun UserLocation() {}

    fun getUser(): String? {
        return uid
    }

    fun setUser(uid: String) {
        this.uid = uid
    }
    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
    fun getImageString(): String? {
        return imageString
    }

    fun setImageString(name: String) {
        this.imageString = imageString
    }
    fun getGeo_point(): GeoPoint? {
        return geo_point
    }

    fun setGeo_point(geo_point: GeoPoint?) {
        this.geo_point = geo_point
    }

    fun getTimestamp(): Date? {
        return timestamp
    }

    fun setTimestamp(timestamp: Date?) {
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "UserLocation{" +
                "userID=" + uid +
                ", geo_point=" + geo_point +
                ", timestamp=" + timestamp +
                '}'
    }
 }
