package com.example.bws.ui.dashboard





import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.bws.MyClusterManagerRenderer
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.BirdSighting
import com.example.bws.ui.models.ClusterMarker
import com.example.bws.ui.models.UserLocation
import com.example.bws.ui.models.UserSettings
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class DashboardFragment : Fragment(), OnMapReadyCallback {

    private val MAPVIEW_BUNDLE_KEY: String="AIzaSyDvAMW0zSS3HZmyX0aXSca7pEZOjIgdJ50"
    private var _binding: FragmentDashboardBinding? = null
    private lateinit var fireStore: FirebaseFirestore
    private var googleMap: GoogleMap? = null
    private lateinit var userPosition: UserLocation
    private var mapBoundary: LatLngBounds? = null
    private var clusterManager: ClusterManager<ClusterMarker>? = null
    private var clusterManagerRenderer: MyClusterManagerRenderer? = null
    private lateinit var birdSightingArrayList : ArrayList<BirdSighting>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        val userClient = UserClient.getInstance(requireActivity().application)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
       // initUserListRecyclerView();
        initGoogleMap(savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        userPosition= UserLocation()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        binding.mapView.onCreate(mapViewBundle)
        binding.mapView.getMapAsync(this)
    }

 /*   private fun initUserListRecyclerView() {
        mUserRecyclerAdapter = UserRecyclerAdapter(mUserList)
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter)
        mUserListRecyclerView.setLayoutManager(LinearLayoutManager(activity))
    }*/

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        binding.mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        map.isMyLocationEnabled = true
        googleMap = map
        addMapMarkers()

            callAPI()


    }
    private fun addMapMarkers() {
        googleMap?.let { map ->

            getUserLocation { userLocation ->
                // Set a boundary to start
                userLocation?.let { location ->
                    Log.d(TAG, "addMapMarkers: location: ${location.geo_point}")
                    try {
                        val userPosition = LatLng(location.geo_point.latitude, location.geo_point.longitude)
                         map.addMarker(
                            MarkerOptions()
                                .position(userPosition)
                                .title("User")
                                .snippet(location.user.username)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )


                    } catch (e: NullPointerException) {
                        Log.e(TAG, "addMapMarkers: NullPointerException: ${e.message}")
                    }
                } ?: run {
                    Log.e(TAG, "addMapMarkers: userLocation is null")
                }
            }


            setCameraView()
        } ?: run {
            Log.e(TAG, "addMapMarkers: googleMap is null")
        }
    }
    private fun callAPI() {
        val mainActivity = activity as MainActivity

        // Check for network availability
        if (!mainActivity.isNetworkAvailable()) {
            Toast.makeText(activity, "Network not available", Toast.LENGTH_SHORT).show()
            return
        }
        var distance = 20
        googleMap?.let { map ->
            getUserLocation { userLocation ->
                // Set a boundary to start
                userLocation?.let { location ->
                    getUserSettings { userSettings -> userSettings?.let{settings ->
                       distance = settings.getDistance()?.toInt() ?: 20
                    } }

                    val urlString = "https://api.ebird.org/v2/data/obs/geo/recent?lat=${location.geo_point.latitude}&lng=${location.geo_point.longitude}&dist=$distance"

                    // Launch coroutine for network call
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = fetchDataFromApi(urlString)
                        if (response != null) {
                            // Process the response on the main thread
                            withContext(Dispatchers.Main) {
                                handleApiResponse(response)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(activity, "Failed to fetch data from eBird API", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: run {
                    Log.e(TAG, "callAPI: userLocation is null")
                }
            }
        } ?: run {
            Log.e(TAG, "callAPI: googleMap is null")
        }
    }

    private suspend fun fetchDataFromApi(urlString: String): String? {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("X-eBirdApiToken", "b4nnc72a23l7")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response data
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e(TAG, "API response code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching data: ${e.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun handleApiResponse(responseData: String) {
        val jsonArray = JSONArray(responseData)
        for (i in 0 until jsonArray.length()) {
            val hotspot = jsonArray.getJSONObject(i)
            val latitude = hotspot.getDouble("lat")
            val longitude = hotspot.getDouble("lng")
            val comName = hotspot.getString("comName")
            val locName = hotspot.getString("locName")
            val obsDt = hotspot.getString("obsDt")

            val position = LatLng(latitude, longitude)

            googleMap?.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(comName)
                    .snippet("Sighted at $locName on the $obsDt,")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }
    }

    // Extension function to check network availability
    private fun MainActivity.isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun setCameraView() {
        getUserLocation() { userLocation ->
            // Set a boundary to start
            if (userLocation != null) {
                if (userLocation.geo_point != null) {
                    // Set a boundary to start
                    val bottomBoundary: Double = userLocation.geo_point!!.latitude - .1
                    val leftBoundary: Double = userLocation.geo_point!!.longitude - .1
                    val topBoundary: Double = userLocation.geo_point!!.latitude + .1
                    val rightBoundary: Double = userLocation.geo_point!!.longitude + .1
                    mapBoundary = LatLngBounds(
                        LatLng(bottomBoundary, leftBoundary),
                        LatLng(topBoundary, rightBoundary)
                    )
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary!!, 0))
                } else {
                    Log.e(ContentValues.TAG, "GeoPoint is null")
                }
            }
        }



    }

    private fun getUserLocation(onLocationReceived: (UserLocation?) -> Unit) {
        val locationsRef = fireStore.collection(getString(R.string.collection_user_locations)).document(FirebaseAuth.getInstance().uid!!)

        locationsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // Convert Firestore document to UserLocation object
                    val userPosition = document.toObject(UserLocation::class.java)
                    Log.d(ContentValues.TAG, "User location retrieved: $userPosition")
                    onLocationReceived(userPosition) // Pass the userPosition back
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                    onLocationReceived(null)
                }
            } else {
                Log.e(ContentValues.TAG, "Error getting document: ${task.exception}")
                onLocationReceived(null)
            }
        }
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


    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }


}