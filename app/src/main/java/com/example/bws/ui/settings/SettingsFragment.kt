package com.example.bws.ui.settings

import android.content.ContentValues
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.User
import com.example.bws.ui.models.UserLocation
import com.example.bws.ui.models.UserSettings
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentDashboardBinding
import com.example.myapplication2.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var fireStore: FirebaseFirestore
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var viewModel: SettingsViewModel
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

        binding.radioKm.setOnCheckedChangeListener { buttonView, isChecked -> units="Kilometers" }
        binding.radioMiles.setOnCheckedChangeListener { buttonView, isChecked -> units="Miles" }
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        getUserSettings { userSettings -> userSettings?.let{settings ->
            var defaultUnitsM= binding.radioMiles
            var defaultUnitsK= binding.radioKm
            var defaultDistance =binding.textRadius


            if(userSettings.getUnit().equals("Miles")){
                defaultUnitsM.isChecked=true
                val mile = settings.getDistance()?.toInt()?.div(0.621371)
                defaultDistance.setText(mile.toString())
            }else{
                defaultUnitsK.isChecked=true
                defaultDistance.setText(settings.getDistance())
            }
        } }
    }
    private fun getUserSettings(onSettingsReceived: (UserSettings?) -> Unit) {
        val locationsRef = fireStore.collection(getString(R.string.collection_user_settings)).document(FirebaseAuth.getInstance().uid!!)

        locationsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // Convert Firestore document to UserLocation object
                    val userSettings = document.toObject(UserSettings::class.java)
                    Log.d(ContentValues.TAG, "User Settings retrieved: $userSettings")
                    onSettingsReceived(userSettings) // Pass the userPosition back
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                    onSettingsReceived(null)
                }
            } else {
                Log.e(ContentValues.TAG, "Error getting document: ${task.exception}")
                onSettingsReceived(null)
            }
        }
    }

}