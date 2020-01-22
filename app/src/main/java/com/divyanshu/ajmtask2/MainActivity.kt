package com.divyanshu.ajmtask2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private var mMap: GoogleMap? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var lat = ""
    private var lng = ""
    private var mTimer1: Timer? = null
    private var mTt1: TimerTask? = null
    private val mTimerHandler = Handler()
    private var mDatabase: FirebaseDatabase? = null
    private var REQUEST_LOCATION_PERMS = 11111
    private var userType = ""
    private var firebaseData: DatabaseReference? = null
    var userLatLng: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        userType = intent.getStringExtra("USER_TYPE")!!

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.googleMaps) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)



        if (mDatabase == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            mDatabase = FirebaseDatabase.getInstance()
        }
        SetPermission()
        startTimer()
        startTimerForLoad()
    }


    private fun SetPermission() {
        Log.e("checkPermission", "permission")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), REQUEST_LOCATION_PERMS
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), REQUEST_LOCATION_PERMS
                    )
                }
            }
        } else {
            if (checkGPSStatus(this)) {
                getCurrentlocation()
            } else {
                buildAlertMessageNoGps()
            }
        }
    }

    fun checkGPSStatus(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun getCurrentlocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mFusedLocationClient!!.lastLocation.addOnSuccessListener(
            this@MainActivity
        ) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lat = location.latitude.toString() + ""
                lng = location.longitude.toString() + ""
                Log.e("latlongs", "$lat <==> $lng")
                //                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                //                    DatabaseReference myRef = database.getReference("LatLngs");
                //
                //                    myRef.setValue(lat+" "+lng);


                if (userType == "1") {
                    val mRef = mDatabase!!.getReference("User1LatLng")
                    mRef.setValue("$lat $lng")
                } else {
                    val mRef2 = mDatabase!!.getReference("User2LatLng")
                    mRef2.setValue("$lat $lng")
                }

                mMap!!.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    ).title("currentLocation")
                )

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude)).zoom(16.0f).build()

                mMap!!.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(cameraPosition)
                )

            }
        }
    }


    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    1010
                )
            }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }


    private fun startTimer() {
        mTimer1 = Timer()
        mTt1 = object : TimerTask() {
            override fun run() {
                mTimerHandler.post { SetPermission() }
            }
        }

        mTimer1!!.schedule(mTt1, 1, 10000)
    }

    private fun startTimerForLoad() {
        mTimer1 = Timer()
        mTt1 = object : TimerTask() {
            override fun run() {
                mTimerHandler.post { getLatLng() }
            }
        }

        mTimer1!!.schedule(mTt1, 1, 12000)
    }

    private fun getLatLng() {
        firebaseData = FirebaseDatabase.getInstance().getReference("ajm-task2");
        Log.d("Dash", " db ref :" + firebaseData);

        if (userType.equals("1")) {
            firebaseData!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("MainActivity", "user1LatLng: $p0")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user2LatLng = dataSnapshot.child("User2LatLng").getValue().toString()
                    Log.d("MainActivity", "user2LatLng: $user2LatLng")

                }

            })
        }
        else
        {
            firebaseData!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("MainActivity", "user1LatLng: $p0")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user1LatLng = dataSnapshot.child("User1LatLng").getValue().toString()
                    Log.d("MainActivity", "user1LatLng: $user1LatLng")

                }

            })


        }


    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}
