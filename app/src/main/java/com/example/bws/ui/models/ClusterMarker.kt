package com.example.bws.ui.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker(
    private var position: LatLng, // required field
    private var title: String,    // required field
    private var snippet: String,  // required field
    private var iconPicture: Int,
    private var user: User
) : ClusterItem {

    // Getter and setter for iconPicture
    fun getIconPicture(): Int {
        return iconPicture
    }

    fun setIconPicture(iconPicture: Int) {
        this.iconPicture = iconPicture
    }

    // Getter and setter for user
    fun getUser(): User {
        return user
    }

    fun setUser(user: User) {
        this.user = user
    }

    // Getter and setter for position
    override fun getPosition(): LatLng {
        return position
    }

    fun setPosition(position: LatLng) {
        this.position = position
    }

    // Getter and setter for title
    override fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    // Getter and setter for snippet
    override fun getSnippet(): String {
        return snippet
    }

    fun setSnippet(snippet: String) {
        this.snippet = snippet
    }
}