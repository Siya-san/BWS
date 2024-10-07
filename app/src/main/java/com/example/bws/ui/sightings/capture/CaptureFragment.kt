package com.example.bws.ui.sightings.capture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.bws.MainActivity
import com.example.bws.ui.models.MyBirdSighting
import com.example.myapplication2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class CaptureFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var uploadButton: Button
    private lateinit var save: Button
    private lateinit var imageView: ImageView
    private lateinit var name: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val TAG = "BirdSightingFragment"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_capture, container, false)

        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference



        imageView = view.findViewById(R.id.imageView)
        uploadButton = view.findViewById(R.id.uploadButton)
        name=view.findViewById(R.id.editName)
        save=view.findViewById(R.id.buttonSaveSighting)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        uploadButton.setOnClickListener {
            takePicture()
        }
        save.setOnClickListener {
            saveImageUriToFirestore()
        }

        return view
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap
            imageUri = getImageUri(imageBitmap)
            imageView.setImageBitmap(imageBitmap)


        }
    }

    private fun getImageUri(imageBitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver, imageBitmap, "BirdImage", null)
        return Uri.parse(path)
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            val imageRef = storageReference.child("bird_sightings/$uid/${UUID.randomUUID()}.jpg")

            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { url ->
                    //saveImageUriToFirestore(url.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveImageUriToFirestore() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            // Fetch the user's location before saving the sighting
            getLastKnownLocation { location ->
                if (location != null) {
                    // Create a GeoPoint from the user's location
                    val geoPoint = GeoPoint(location.latitude, location.longitude)

                    // Prepare the bird sighting object
                    val birdSighting = MyBirdSighting().apply {
                        setUser(uid)
                        setName(name.text.toString())

                        setGeo_point(geoPoint) // Include the user's location

                    }

                    // Use add() to create a new document in Firestore
                    firestore.collection(getString(R.string.collection_user_sightings)).add(birdSighting)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Bird sighting saved successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save bird sighting", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Failed to get location. Ensure location is enabled.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLastKnownLocation(onLocationReceived: (Location?) -> Unit) {
        Log.d(TAG, "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request permission or handle the case accordingly
            onLocationReceived(null) // Return null if location access is not permitted
            return
        }

        // Get the last known location from the FusedLocationProviderClient
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result

                // Check if location is null before accessing its properties
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    Log.e(TAG, "Location is null. Ensure location services are enabled and try again.")
                    onLocationReceived(null)
                }
            } else {
                Log.e(TAG, "Task unsuccessful. Unable to get last known location.")
                onLocationReceived(null)
            }
        }
    }



}
