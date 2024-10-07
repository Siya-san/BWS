package com.example.bws.ui.settings

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.UserSettings
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {



    private lateinit var fireStore: FirebaseFirestore
    private var _binding: FragmentSettingsBinding? = null

    private var distances: String? = null
    private var units: String? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fireStore = FirebaseFirestore.getInstance()
        val userClient = UserClient.getInstance(requireActivity().application)

        binding.radioKm.setOnCheckedChangeListener { _, _ -> units="Kilometers" }
        binding.radioMiles.setOnCheckedChangeListener { _, _ -> units="Miles" }
        binding.saveChanges.setOnClickListener {
            try {
                distances = binding.textRadius.text.toString()
                val userSettings = UserSettings()

                // Check for null values and log accordingly
                if (units == null || distances!!.isEmpty() || userClient.user == null) {
                    throw NullPointerException("Some required values are null: units=$units, distances=$distances, user=${userClient.user}")
                }

                userSettings.setUnit(units)
                userSettings.setUser(userClient.user)
                userSettings.setDistance(distances)
                if(userSettings.getUnit().equals("Miles")){

                    val mile = userSettings.getDistance()?.toInt()?.times(0.621371)
                    userSettings.setDistance(mile.toString())
                }else{

                    userSettings.setDistance(distances)
                }
                val settingRef = fireStore
                    .collection(getString(R.string.collection_user_settings))
                    .document(FirebaseAuth.getInstance().uid!!)

                settingRef.set(userSettings).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "userSettings: $userSettings")
                    } else {
                        Log.e(ContentValues.TAG, "Failed to update user settings")
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error saving user settings: ${e.message}")
            }
        }



        return root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val userClient = UserClient.getInstance(requireActivity().application)

        val settings = userClient.userSettings


        val defaultUnitsM= binding.radioMiles
        val defaultUnitsK= binding.radioKm
        val defaultDistance =binding.textRadius


        if (settings != null) {
            if(settings.getUnit().equals("Miles")){
                defaultUnitsM.isChecked=true
                val mile = settings.getDistance()?.toInt()?.div(0.621371)
                defaultDistance.setText(mile.toString())
            }else{
                defaultUnitsK.isChecked=true
                defaultDistance.setText(settings.getDistance())
            }
        }
        }



}