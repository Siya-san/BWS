package com.example.bws.ui.dashboard





import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bws.MainActivity
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.BirdSighting
import com.example.bws.ui.models.UserLocation
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class DashboardFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private val MAPVIEW_BUNDLE_KEY: String="AIzaSyDvAMW0zSS3HZmyX0aXSca7pEZOjIgdJ50"
    private var _binding: FragmentDashboardBinding? = null
    private lateinit var fireStore: FirebaseFirestore
    private var googleMap: GoogleMap? = null
    private lateinit var userPosition: UserLocation
    private var mapBoundary: LatLngBounds? = null
    private var geoApiContext: GeoApiContext? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sightingsArrayList : ArrayList<BirdSighting>


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {



        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
       // initUserListRecyclerView();
        initGoogleMap(savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        userPosition= UserLocation()

        return root
    }

    override fun onDestroyView() {
        // Perform MapView cleanup here before nullifying binding
        binding.mapView.onDestroy()
        _binding = null
        super.onDestroyView()
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
        if (geoApiContext == null) {
            geoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_api_key))
                .build()
        }
    }



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

    @SuppressLint("PotentialBehaviorOverride")
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
        useSightings()
        googleMap!!.setOnInfoWindowClickListener(this)



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

        getUserLocation { userLocation ->
                // Set a boundary to start
                userLocation?.let { location ->

                    val userClient = UserClient.getInstance(requireActivity().application)
                    val settings = userClient.userSettings
                    if (settings != null) {
                        distance = settings.getDistance()?.toInt() ?: 20
                    }
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

    }

    private  fun fetchDataFromApi(urlString: String): String? {
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
private fun useSightings(){

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
            for(item in sightingsArrayList){
                val position = LatLng(item.geo_point!!.latitude, item.geo_point!!.longitude)
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("User Sighting")
                        .snippet("Sighted ${item.name} on the ${item.timestamp},")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                )
            }

}}}
    // Extension function to check network availability
    @Deprecated("Deprecated in Java")
    private fun MainActivity.isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun setCameraView() {
        getUserLocation { userLocation ->
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
                    Log.e(TAG, "GeoPoint is null")
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
                    Log.d(TAG, "User location retrieved: $userPosition")
                    onLocationReceived(userPosition) // Pass the userPosition back
                } else {
                    Log.d(TAG, "No such document")
                    onLocationReceived(null)
                }
            } else {
                Log.e(TAG, "Error getting document: ${task.exception}")
                onLocationReceived(null)
            }
        }
    }




    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onInfoWindowClick(marker: Marker) {
        val userClient = UserClient.getInstance(requireActivity().application)
        val user = userClient.user
        if (user != null) {
            if (marker.snippet.equals(user.username)) {
                marker.hideInfoWindow()
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                builder.setMessage("Get directions to the "+marker.title)
                    .setCancelable(true)
                    .setPositiveButton("Yes"
                    ) { dialog, _ ->
                        calculateDirections(marker)
                        dialog.dismiss() }
                    .setNegativeButton("No"
                    ) { dialog, _ -> dialog.cancel() }
                val alert: AlertDialog = builder.create()
                alert.show()
            }
        }
    }
    private fun calculateDirections(marker: Marker) {
        Log.d(TAG, "calculateDirections: calculating directions.")

        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )

        val directions = DirectionsApiRequest(geoApiContext)
        directions.alternatives(false)

        getUserLocation { userLocation ->
            if (userLocation != null) {
                directions.origin(
                    com.google.maps.model.LatLng(
                        userLocation.geo_point.latitude,
                        userLocation.geo_point.longitude
                    )
                )

                Log.d(TAG, "calculateDirections: destination: $destination")

                // Set the destination and the callback only after origin is set
                directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult> {
                    override fun onResult(result: DirectionsResult) {
                        Log.d(TAG, "calculateDirections: routes: ${result.routes[0]}")
                        Log.d(TAG, "calculateDirections: duration: ${result.routes[0].legs[0].duration}")
                        Log.d(TAG, "calculateDirections: distance: ${result.routes[0].legs[0].distance}")
                        Log.d(TAG, "calculateDirections: geocodedWayPoints: ${result.geocodedWaypoints[0]}")
                        addPolylines(result)
                    }

                    override fun onFailure(e: Throwable) {
                        Log.e(TAG, "calculateDirections: Failed to get directions: ${e.message}")
                    }
                })
            } else {
                Log.e(TAG, "calculateDirections: User location is null, cannot calculate directions.")
            }
        }
    }

    private fun addPolylines(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "run: result routes: ${result.routes.size}")

            for (route in result.routes) {
                Log.d(TAG, "run: leg: ${route.legs[0]}")

                // Decode the polyline path
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)

                val newDecodedPath = ArrayList<LatLng>()

                // Loop through all the LatLng coordinates of one polyline
                for (latLng in decodedPath) {
                    newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                }

                // Add the polyline to the GoogleMap
                val polyline = googleMap?.addPolyline(PolylineOptions().addAll(newDecodedPath))

                // Set color and make the polyline clickable
                if (polyline != null) {
                    polyline.color = ContextCompat.getColor(requireContext(), R.color.orange_700)

                    polyline.isClickable = true
                    zoomRoute(polyline.points)
                }
            }
        }
    }
    private fun zoomRoute(lstLatLngRoute: List<LatLng?>?) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(
            latLngPoint!!
        )
        val routePadding = 120
        val latLngBounds = boundsBuilder.build()
        googleMap!!.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600,
            null
        )
    }


}