package com.jujare.maps.ubercaranimation.utils

import android.content.Context
import android.graphics.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.jujare.maps.ubercaranimation.R
import java.lang.Math.abs
import java.lang.Math.atan

object MapUtils {

    //As I don't have server added a list of list of locations from source to destination.
    fun getListOfLocations(): ArrayList<LatLng> {
        val locationList = ArrayList<LatLng>()
        locationList.add(LatLng(28.436970000000002, 77.11272000000001))
        locationList.add(LatLng(28.43635, 77.11289000000001))
        locationList.add(LatLng(28.4353, 77.11317000000001))
        locationList.add(LatLng(28.435280000000002, 77.11332))
        locationList.add(LatLng(28.435350000000003, 77.11368))
        locationList.add(LatLng(28.4356, 77.11498))
        locationList.add(LatLng(28.435660000000002, 77.11519000000001))
        locationList.add(LatLng(28.43568, 77.11521))
        locationList.add(LatLng(28.436580000000003, 77.11499))
        locationList.add(LatLng(28.436590000000002, 77.11507))
        return locationList
    }

    //Returns a bitmap which looks like Uber's black origin, destination marker
    fun getOriginDestinationMarkerBitmap(): Bitmap{
        val height = 20
        val width = 20
        val bitmap: Bitmap = Bitmap.createBitmap(height,width,Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint)
        return bitmap
    }

    //Get car icon as Bitmap
    fun getCarBitmap(context: Context):Bitmap{
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources,R.drawable.ic_car),
            50,100,false)
    }

    //Get the rotation of car
    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }


}