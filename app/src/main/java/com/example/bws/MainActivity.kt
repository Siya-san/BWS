package com.example.bws

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.User
import com.example.bws.ui.models.UserLocation
import com.example.myapplication2.R
import com.example.myapplication2.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ERROR_DIALOG_REQUEST = 9001
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002
    private val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
    private var locationPermissionGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var userLocation: UserLocation? = null
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var userClient: UserClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_dashboard,
            R.id.navigation_my_sighting_view,
            R.id.navigation_notifications,
            R.id.navigation_settings
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        fireStore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    private fun getUserDetails() {

        if (userLocation == null) {
            userLocation =  UserLocation()
            userClient= UserClient()
            val userRef: DocumentReference = fireStore.collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().uid!!)
            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onComplete: successfully set the user client.")
                    val user = task.result.toObject(User::class.java)
                    userLocation!!.user = user
                    Log.d(TAG, "UserId "+ userLocation!!.user.userId)
                    (applicationContext as UserClient).user = user
                    getLastKnownLocation()
                }
            }
        } else {
            getLastKnownLocation()
        }
    }


    private fun getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request permission or handle the case accordingly
            return
        }

        // Get the last known location from the FusedLocationProviderClient
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result

                // Check if location is null before accessing its properties
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    userLocation?.geo_point = geoPoint
                    userLocation?.timestamp = null // or assign a proper timestamp if needed
                    saveUserLocation()

                } else {
                    Log.e(TAG, "Location is null. Ensure location services are enabled and try again.")
                }
            } else {
                Log.e(TAG, "Task unsuccessful. Unable to get last known location.")
            }
        }
    }

    private fun saveUserLocation() {
        val locationRef = fireStore
            .collection(getString(R.string.collection_user_locations))
            .document(FirebaseAuth.getInstance().uid!!)
        locationRef.set(userLocation!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(
                    TAG, "saveUserLocation: \ninserted user location into database." +
                            "\n latitude: " + userLocation!!.geo_point
                        .latitude +
                            "\n longitude: " + userLocation!!.geo_point.longitude
                )
            }
        }
    }
    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            getUserDetails()

        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun isServicesOK(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it")
            val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog!!.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (locationPermissionGranted) {
                    getUserDetails()

                } else {
                    getLocationPermission()
                }
            }
        }
    }
    fun goToSightingCapture() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navController.navigate(R.id.navigation_capture, Bundle().apply {

        })
    }
    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            if (locationPermissionGranted) {
                getUserDetails()

            } else {
                getLocationPermission()
            }
        }
    }

}