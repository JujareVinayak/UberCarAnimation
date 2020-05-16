package com.jujare.maps.ubercaranimation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.jujare.maps.ubercaranimation.utils.AnimationUtils
import com.jujare.maps.ubercaranimation.utils.MapUtils

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var defaultLocation:LatLng
    private lateinit var grayPolyline: Polyline
    private lateinit var blackPolyline: Polyline
    private lateinit var originMarker: Marker
    private lateinit var destinationMarker: Marker
    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    //Since we are reading the locations from the array of size 10 and not from the server, the cab position will be updated
    // so fastly that you will never see any animation in between.we will use Handler to delay the location feeding by 5 sec.
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Making sure to enable location permission
        if(!isBlockingPermissionsEnabled())
            checkPermissionsGranted()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun checkPermissionsGranted() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),5)
    }

    private fun isBlockingPermissionsEnabled(): Boolean {
        var permList: MutableList<String> = arrayListOf()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            permList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d("MainAcitivity", "ACCESS_FINE_LOCATION PERMISSION not GRANTED")
        }
        return permList.size == 0
    }

    //Overridden method of OnMapReadyCallback interface. Called when the map is loaded.
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.isMyLocationEnabled = true
        defaultLocation = MapUtils.getListOfLocations().get(0)
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLocation)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())) //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        googleMap.addMarker(markerOptions)
        showDefaultLocation(defaultLocation)
        Handler().postDelayed(Runnable {
            showPath(MapUtils.getListOfLocations())
            showMovingCab(MapUtils.getListOfLocations())
        }, 3000)
    }

    /**
     * Location of origin
     */
    private fun showDefaultLocation(latLng: LatLng) {
        moveCamera(latLng)
        animateCamera(latLng)
    }

    private fun moveCamera(latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition:CameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //Draw polyline from origin to destination
    private fun showPath(latLngList: ArrayList<LatLng>){
        val builder = LatLngBounds.builder()
        for(latLng in latLngList){
            builder.include(latLng)
        }
        //An area that is created by using the LatLng we have.
        val bounds:LatLngBounds = builder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2) //transforms the camera such that the specified latitude/longitude bounds are centered on screen at the greatest possible zoom level.
        googleMap.animateCamera(cameraUpdate)

        val grayPolylineOptions = PolylineOptions()
        grayPolylineOptions.color(Color.GRAY)
        grayPolylineOptions.width(5f)
        grayPolylineOptions.addAll(latLngList)//Adding all latLng list ensures first we animate gray line along the path.
        grayPolyline = googleMap.addPolyline(grayPolylineOptions)

        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.color(Color.BLACK)
        blackPolylineOptions.width(5f)
        blackPolyline = googleMap.addPolyline(blackPolylineOptions)


        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker.setAnchor(0.5f,0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        destinationMarker.setAnchor(0.5f,0.5f)

        val polylineAnimator = AnimationUtils.polylineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (grayPolyline.points.size) * (percentValue / 100.0f).toInt()
            blackPolyline.points = grayPolyline.points.subList(0, index)// Will animate from gray to black
        }
        polylineAnimator.start()

    }

    /**
     * Api to add the marker
     */
    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor =
                BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())
        return googleMap.addMarker(
                MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    /**
     *Animate the Cab
     */
    private fun showMovingCab(cabLatLngList: ArrayList<LatLng>) {
        handler = Handler()
        var index = 0
        runnable = Runnable {
            run {
                if (index < 10) {
                    updateCarLocation(cabLatLngList[index])
                    handler.postDelayed(runnable, 3000)
                    ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(this@MainActivity, "Trip Ended", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    /**
     * Update car Location with given parameter LatLng
     * @param latLng: location where car icon tobe updated.
     */
    private fun updateCarLocation(latLng: LatLng){
        if (movingCabMarker == null) {
            movingCabMarker = addCarMarkerAndGet(latLng)
        }
        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLng!!)
        }else{
            previousLatLng = currentLatLng
            currentLatLng = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLng != null && previousLatLng != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                            multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                            multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    val rotation = MapUtils.getRotation(previousLatLng!!, nextLocation)
                    if (!rotation.isNaN()) {
                        movingCabMarker?.rotation = rotation
                    }
                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    /**
     * Adds car icon at given LatLng
     */
    private fun addCarMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(
                MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }
}
