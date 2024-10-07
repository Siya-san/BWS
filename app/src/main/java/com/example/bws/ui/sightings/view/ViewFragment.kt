package com.example.bws.ui.sightings.view

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bws.ui.models.BirdSighting
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

@Suppress("NAME_SHADOWING")
class ViewFragment : Fragment() {

    private lateinit var _binding: FragmentViewBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sightingsRecyclerview : RecyclerView
    private lateinit var sightingsArrayList : ArrayList<BirdSighting>
    private lateinit var auth: FirebaseAuth

    private val binding get() = _binding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViewBinding.inflate( inflater,
            container,
            false)
        val root: View = binding.root
        val rView: RecyclerView =binding.listViewObservations
        sightingsRecyclerview = rView
        sightingsRecyclerview.layoutManager = LinearLayoutManager(context)
        sightingsRecyclerview.setHasFixedSize(true)
        sightingsArrayList = arrayListOf()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val collectionRef = firestore.collection(getString(R.string.collection_user_sightings))
        collectionRef.addSnapshotListener { snapshot: QuerySnapshot?, error: Exception? ->
            if (error != null) {
                Log.e("Firestore", "Error fetching data", error)
                return@addSnapshotListener
            }

            if (auth.currentUser == null) {
                Log.e("Firestore", "User is not authenticated, cannot fetch data.")
                return@addSnapshotListener
            }

            snapshot?.let {
                Log.d("Firestore", "Snapshot retrieved, size: ${it.size()}")
                sightingsArrayList.clear()

                for (document in it.documents) {
                    val sighting = document.toObject(BirdSighting::class.java)
                    Log.d("Firestore", "Sighting data: ${document.data}")

                    sighting?.let { sightingObj ->
                        if (sightingObj.user == auth.currentUser?.uid) {
                            sightingsArrayList.add(sightingObj)
                            Log.d("Firestore", "Sighting added to list: $sightingObj")
                        }
                    }
                }

                sightingsRecyclerview.adapter?.notifyDataSetChanged()
            } ?: Log.d("Firestore", "No data found in snapshot.")
        }


        sightingsRecyclerview.adapter = ViewAdaptor(sightingsArrayList, this)




return root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }


}