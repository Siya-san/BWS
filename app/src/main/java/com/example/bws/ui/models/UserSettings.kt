package com.example.bws.ui.models

import android.annotation.SuppressLint


class UserSettings(  ) {
 private var user: User? = null
 private var distance: String? = null
private var unit: String? = null


 @SuppressLint("NotConstructor")
 fun UserSettings(user: User, unit: String?, distance: String?) {
  this.user = user
  this.unit = unit
  this.distance = distance
 }

 fun UserSettings() {}

 fun getUser(): User? {
  return user
 }

 fun setUser(user: User?) {
  this.user = user
 }

 fun getUnit(): String? {
  return unit
 }

 fun setUnit(unit: String?) {
  this.unit = unit
 }

 fun getDistance(): String? {
  return distance
 }

 fun setDistance(distance: String?) {
  this.distance = distance
 }

 override fun toString(): String {
  return "UserLocation{" +
          "user=" + user +
          ", units=" + unit +
          ", distance=" + distance +
          '}'
 }


}