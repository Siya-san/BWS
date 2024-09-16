package com.example.bws.ui.dashboard



import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.bws.ui.UserClient
import com.example.bws.ui.models.UserLocation
import com.example.myapplication2.R
import com.example.myapplication2.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore


class DashboardFragment : Fragment(), OnMapReadyCallback {

    private val MAPVIEW_BUNDLE_KEY: String="AIzaSyDvAMW0zSS3HZmyX0aXSca7pEZOjIgdJ50"
    private var _binding: FragmentDashboardBinding? = null
    private var fireStore: FirebaseFirestore? = null
    private var googleMap: GoogleMap? = null
    private lateinit var userPosition: UserLocation
    private var mapBoundary: LatLngBounds? = null

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
        getUserLocation()
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
        googleMap = map;
        setCameraView();
    }

    private fun setCameraView() {

        // Set a boundary to start
        val bottomBoundary: Double = userPosition.getGeo_point().getLatitude() - .1
        val leftBoundary: Double = userPosition.getGeo_point().getLongitude() - .1
        val topBoundary: Double = userPosition.getGeo_point().getLatitude() + .1
        val rightBoundary: Double = userPosition.getGeo_point().getLongitude() + .1
        mapBoundary = LatLngBounds(
            LatLng(bottomBoundary, leftBoundary),
            LatLng(topBoundary, rightBoundary)
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary!!, 0))
    }

    private fun getUserLocation() {
        val locationsRef = fireStore?.collection(getString(R.string.collection_user_locations))
            ?.document(UserClient.user?.userId!!)

        // Initialize userLocation to null, since we're going to fetch it asynchronously


        if (locationsRef != null) {
            locationsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check if the result is not null and convert it to UserLocation object
                    val location = task.result.toObject(UserLocation::class.java)
                    if (location != null) {
                        userPosition = location
                    }
                }
                // Pass the result (userLocation) back via the callback

            }
        } else {
            // If locationsRef is null, immediately invoke the callback with null

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