package com.example.bws.ui.sightings.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bws.ui.models.BirdSighting
import com.example.myapplication2.R


class ViewAdaptor(private val sightingList: ArrayList<BirdSighting>, viewFragment: ViewFragment) : RecyclerView.Adapter<ViewAdaptor.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.sighting_list_items,
            parent,false)
        return MyViewHolder(itemView)

    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = sightingList[position]

        holder.name.text = currentitem.name
        holder.date.text = currentitem.timestamp.toString()
        holder.loc.text = "latitude: ${currentitem.geo_point?.latitude} , longitude:  ${currentitem.geo_point?.longitude}"
        Glide.with(holder.itemView)
            .load(currentitem.imageString)
            .into(holder.image)
    }
    override fun getItemCount(): Int {

        return sightingList.size
    }
    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.imageView3)
        val name : TextView = itemView.findViewById(R.id.sighting_name)
        val date : TextView = itemView.findViewById(R.id.date_seen)
        val loc : TextView = itemView.findViewById(R.id.locName)

    }


}