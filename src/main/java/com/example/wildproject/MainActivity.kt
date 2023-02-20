package com.example.wildproject

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wildproject.Constants.INTERVAL_LOCATION
import com.example.wildproject.Constants.LIMIT_DISTANCE_ACCEPTED_BIKE
import com.example.wildproject.Constants.LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE
import com.example.wildproject.Constants.LIMIT_DISTANCE_ACCEPTED_RUNNING
import com.example.wildproject.Constants.key_challengeAutofinish
import com.example.wildproject.Constants.key_challengeDistance
import com.example.wildproject.Constants.key_challengeDurationHH
import com.example.wildproject.Constants.key_challengeDurationMM
import com.example.wildproject.Constants.key_challengeDurationSS
import com.example.wildproject.Constants.key_challengeNofify
import com.example.wildproject.Constants.key_intervalDuration
import com.example.wildproject.Constants.key_maxCircularSeekBar
import com.example.wildproject.Constants.key_modeChallenge
import com.example.wildproject.Constants.key_modeChallengeDistance
import com.example.wildproject.Constants.key_modeChallengeDuration
import com.example.wildproject.Constants.key_modeInterval
import com.example.wildproject.Constants.key_progressCircularSeekBar
import com.example.wildproject.Constants.key_provider
import com.example.wildproject.Constants.key_runningTime
import com.example.wildproject.Constants.key_selectedSport
import com.example.wildproject.Constants.key_userApp
import com.example.wildproject.Constants.key_walkingTime
import com.example.wildproject.LoginActivity.Companion.providerSession
import com.example.wildproject.LoginActivity.Companion.useremail
import com.example.wildproject.Utility.animateViewofFloat
import com.example.wildproject.Utility.getFormattedStopWatch
import com.example.wildproject.Utility.getFormattedTotalTime
import com.example.wildproject.Utility.roundNumber
import com.example.wildproject.Utility.setHeightLinearLayout
import com.example.wildproject.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.navigation.NavigationView
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{
    companion object {
        lateinit var mainContext: Context
        
        lateinit var totalsSelectedSport: Totals
        lateinit var totalsBike: Totals
        lateinit var totalsRollerSkate: Totals
        lateinit var totalsRunning: Totals
        val REQUIRED_PERMISSION_GPS = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )


    }

    private lateinit var listPoints: Iterable<LatLng>

    private lateinit var mMap: GoogleMap

    private var PERMISSION_ID: Int = 62
    private var LOCATIN_PERMISSION_REQ_CODE = 1000
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private var challengeDistance: Float = 0f
    private var challengeDuration: Int = 0

    private var ROUND_INTERVAL: Int = 300
    private var TIME_RUNNING: Int = 0

    private var mHandler: Handler? = null
    private var timeInSeconds = 0L
    private var startButtonClicked: Boolean = false

    private var mInterval: Long = 0
    private var widthAnimations = 0
    private var rounds: Int = 1

    private var activatedGPS: Boolean = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var minAltitude: Double? = null
    private var maxAltitude: Double? = null
    private var minLatitude: Double? = null
    private var maxLatitude: Double? = null
    private var minLongitude: Double? = null
    private var maxLongitude: Double? = null

    private lateinit var sportSelected: String
    private var LIMIT_DISTANCE_ACCEPTED: Double = 0.0

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    private var
            flagSavedLocation: Boolean = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var init_lt: Double = 0.0
    private var init_ln: Double = 0.0

    private var distance: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var speed: Double = 0.0

    private lateinit var levelBike: Level
    private lateinit var levelRollerSkate: Level
    private lateinit var levelRunning: Level
    private lateinit var levelSelectedSport: Level

    private lateinit var levelsListBike: ArrayList<Level>
    private lateinit var levelsListRollerSkate: ArrayList<Level>
    private lateinit var levelsListRunning: ArrayList<Level>

    private fun createPolylines(listPoints: Iterable<LatLng>):Unit{
            val polyLinesOptions = PolylineOptions()
                .width(25f)
                .color(ContextCompat.getColor(this, R.color.salmon_dark))
                .addAll(listPoints)
            var polyline = mMap.addPolyline(polyLinesOptions)
                polyline.startCap = RoundCap()

    }
    private fun rellenarFireBase():Unit{
        val dbTotal: FirebaseFirestore= FirebaseFirestore.getInstance()
        var lists: Array<String> = arrayOf("levelsRunning", "levelsBike", "levelsRollerSkate")
        for(list in lists){
            for(t in 1..10){
                dbTotal.collection(list).document("level_$t").set(
                        hashMapOf(
                            "name" to "level_$t",
                            "image" to "level",
                            "RunsTarget" to t*10,
                            "DistanceTarget" to t*3
                        )
                ).addOnSuccessListener {
                    println("se realizo correctamente")
                }
            }
        }

    }
    private fun loadfromDB(): Unit{
          loadTotalsUser()
    }
    private fun loadTotalsUser():Unit{
         loadTotalSport("Bike")
         loadTotalSport("RollerSkate")
         loadTotalSport("Running")
    }
    private fun loadTotalSport(sport: String){
        println("asiendo la soliciutd")
        Log.d("errorFirebase", "asiendo petición firebase")
        var collection = "totals$sport"
        var dbTotalUser = FirebaseFirestore.getInstance()
        dbTotalUser.collection(collection).document(useremail)
            .get()
            .addOnSuccessListener { document ->
                if(document.data?.size != null){
                    var totals = document.toObject(Totals::class.java)
                    when(sport){
                     "Bike"-> totalsBike = totals!!
                        "RollerSkate"-> totalsRollerSkate = totals!!
                        "Running"-> totalsRunning = totals!!
                    }
                }else{
                    val dbTotal: FirebaseFirestore= FirebaseFirestore.getInstance()
                    dbTotal.collection(collection).document(useremail).set(
                        hashMapOf(
                            "recordAvgSpeed" to 0.0,
                            "recordDistance" to 0.0,
                            "recordSpeed" to 0.0,
                            "totalDistance" to 0.0,
                            "totalRuns" to 0,
                            "totalTime" to 0
                        )
                    )
                }
                setLevelSport(sport)
            }
            .addOnFailureListener { exception ->
                Log.d("errorFirebase", "Error al intentar conseguir el dato")
            }
    }
    private fun setLevelSport(sport:String):Unit{
           var dbLevels: FirebaseFirestore = FirebaseFirestore.getInstance()
           dbLevels.collection("levels$sport")
               .get()
               .addOnSuccessListener { documents->
                   var levels: ArrayList<Level> = ArrayList<Level>(0)
                   for(document in documents){
                       levels.add(document.toObject(Level::class.java))
                   }
                   when(sport){
                      "Bike"-> {
                          levelsListBike = levels
                          setLevelBike()
                      }
                      "RollerSkate"->{
                          levelsListRollerSkate = levels
                          setLevelRollerSkate()
                      }
                      "Running"->{
                          levelsListRunning = levels
                          setLevelRunning()
                      }
                   }

               }
               .addOnFailureListener {exception->
                    Log.e("errorFirebase", "Ha ocurrido un error al solicitar el documento")
               }
    }
    private fun setLevelRollerSkate(){

        var lyNavLevelRollerSkate = findViewById<LinearLayout>(R.id.lyNavLevelSkate)
        if (totalsRollerSkate.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRollerSkate, 0)
        else{

            setHeightLinearLayout(lyNavLevelRollerSkate, 300)
            for (level in levelsListRollerSkate){
                if (totalsRollerSkate.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRollerSkate.totalDistance!! < level.DistanceTarget!!.toDouble()){

                    levelRollerSkate.name = level.name!!
                    levelRollerSkate.image = level.image!!
                    levelRollerSkate.RunsTarget = level.RunsTarget!!
                    levelRollerSkate.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRollerSkate = findViewById<ImageView>(R.id.ivRollerSkate)
            var tvTotalTimeRollerSkate = findViewById<TextView>(R.id.tvTotalTimeSkate)
            var tvTotalRunsRollerSkate = findViewById<TextView>(R.id.tvTotalRunsSkate)
            var tvTotalDistanceRollerSkate = findViewById<TextView>(R.id.tvTotalRunsSkate)

            var tvNumberLevelRollerSkate = findViewById<TextView>(R.id.tvNumberLevelSkate)
            var levelText = "${getString(R.string.level)} ${levelRollerSkate.image!!.subSequence(6,7).toString()}"
            tvNumberLevelRollerSkate.text = levelText

            var tt = getFormattedTotalTime(totalsRollerSkate.totalTime!!.toLong())
            tvTotalTimeRollerSkate.text = tt

            when (levelRollerSkate.image){
                "level_1" -> ivLevelRollerSkate.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelRollerSkate.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelRollerSkate.setImageResource(R.drawable.level_3)
                "level_4" -> ivLevelRollerSkate.setImageResource(R.drawable.level_4)
                "level_5" -> ivLevelRollerSkate.setImageResource(R.drawable.level_5)
                "level_6" -> ivLevelRollerSkate.setImageResource(R.drawable.level_6)
                "level_7" -> ivLevelRollerSkate.setImageResource(R.drawable.level_7)
            }


            tvTotalRunsRollerSkate.text = "${totalsRollerSkate.totalRuns}/${levelRollerSkate.RunsTarget}"

            var porcent = totalsRollerSkate.totalDistance!!.toInt() * 100 / levelRollerSkate.DistanceTarget!!.toInt()
            tvTotalDistanceRollerSkate.text = "${porcent.toInt()}%"

            var csbDistanceRollerSkate = findViewById<CircularSeekBar>(R.id.csbDistanceSkate)
            csbDistanceRollerSkate.max = levelRollerSkate.DistanceTarget!!.toFloat()
            if (totalsRollerSkate.totalDistance!! >= levelRollerSkate.DistanceTarget!!.toDouble())
                csbDistanceRollerSkate.progress = csbDistanceRollerSkate.max
            else
                csbDistanceRollerSkate.progress = totalsRollerSkate.totalDistance!!.toFloat()

            var csbRunsRollerSkate = findViewById<CircularSeekBar>(R.id.csbRunsSkate)
            csbRunsRollerSkate.max = levelRollerSkate.RunsTarget!!.toFloat()
            if (totalsRollerSkate.totalRuns!! >= levelRollerSkate.RunsTarget!!.toInt())
                csbRunsRollerSkate.progress = csbRunsRollerSkate.max
            else
                csbRunsRollerSkate.progress = totalsRollerSkate.totalRuns!!.toFloat()
        }
    }
    private fun setLevelRunning(){
        var lyNavLevelRunning = findViewById<LinearLayout>(R.id.lyNavLevelRun)
        if (totalsRunning.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRunning, 0)
        else{

            setHeightLinearLayout(lyNavLevelRunning, 300)
            for (level in levelsListRunning){
                if (totalsRunning.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRunning.totalDistance!! < level.DistanceTarget!!.toDouble()){

                    levelRunning.name = level.name!!
                    levelRunning.image = level.image!!
                    levelRunning.RunsTarget = level.RunsTarget!!
                    levelRunning.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRunning = findViewById<ImageView>(R.id.ivLebelRun)
            var tvTotalTimeRunning = findViewById<TextView>(R.id.tvTotalTimeRun)
            var tvTotalRunsRunning = findViewById<TextView>(R.id.tvTotalRunsRun)
            var tvTotalDistanceRunning = findViewById<TextView>(R.id.tvTotalDistanceRun)


            var tvNumberLevelRunning = findViewById<TextView>(R.id.tvNumberLevelRun)
            var levelText = "${getString(R.string.level)} ${levelRunning.image!!.subSequence(6,7).toString()}"
            tvNumberLevelRunning.text = levelText

            var tt = getFormattedTotalTime(totalsRunning.totalTime!!.toLong())
            tvTotalTimeRunning.text = tt

            when (levelRunning.image){
                "level_1" -> ivLevelRunning.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelRunning.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelRunning.setImageResource(R.drawable.level_3)
                "level_4" -> ivLevelRunning.setImageResource(R.drawable.level_4)
                "level_5" -> ivLevelRunning.setImageResource(R.drawable.level_5)
                "level_6" -> ivLevelRunning.setImageResource(R.drawable.level_6)
                "level_7" -> ivLevelRunning.setImageResource(R.drawable.level_7)
            }

            tvTotalRunsRunning.text = "${totalsRunning.totalRuns}/${levelRunning.RunsTarget}"
            var porcent = totalsRunning.totalDistance!!.toInt() * 100 / levelRunning.DistanceTarget!!.toInt()
            tvTotalDistanceRunning.text = "${porcent.toInt()}%"

            var csbDistanceRunning = findViewById<CircularSeekBar>(R.id.csbDistanceRun)
            csbDistanceRunning.max = levelRunning.DistanceTarget!!.toFloat()
            if (totalsRunning.totalDistance!! >= levelRunning.DistanceTarget!!.toDouble())
                csbDistanceRunning.progress = csbDistanceRunning.max
            else
                csbDistanceRunning.progress = totalsRunning.totalDistance!!.toFloat()

            var csbRunsRunning = findViewById<CircularSeekBar>(R.id.csbRunsRun)
            csbRunsRunning.max = levelRunning.RunsTarget!!.toFloat()
            if (totalsRunning.totalRuns!! >= levelRunning.RunsTarget!!.toInt())
                csbRunsRunning.progress = csbRunsRunning.max
            else
                csbRunsRunning.progress = totalsRunning.totalRuns!!.toFloat()

        }
    }
    private fun setLevelBike(){
        var lyNavLevelBike = findViewById<LinearLayout>(R.id.lyNavLevelBike)
        if (totalsBike.totalTime!! == 0) setHeightLinearLayout(lyNavLevelBike, 0)
        else{
            setHeightLinearLayout(lyNavLevelBike, 300)
            for (level in levelsListBike){
                if (totalsBike.totalRuns!! < level.RunsTarget!!
                    || totalsBike.totalDistance!! < level.DistanceTarget!!){

                    levelBike.name = level.name!!
                    levelBike.image = level.image!!
                    levelBike.RunsTarget = level.RunsTarget!!
                    levelBike.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelBike = findViewById<ImageView>(R.id.ivBike)
            var tvTotalTimeBike = findViewById<TextView>(R.id.tvTotalTimeBike)
            var tvTotalRunsBike = findViewById<TextView>(R.id.tvTotalRunsBike)
            var tvTotalDistanceBike = findViewById<TextView>(R.id.tvTotalDistanceBike)
            var tvNumberLevelBike = findViewById<TextView>(R.id.tvNumberLevelBike)

            var levelText = "${getString(R.string.level)} ${levelBike.image!!.subSequence(6,7).toString()}"

            tvNumberLevelBike.text = levelText

            var tt = getFormattedTotalTime(totalsBike.totalTime!!.toLong())
            tvTotalTimeBike.text = tt

            when (levelBike.image){
                "level_1" -> ivLevelBike.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelBike.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelBike.setImageResource(R.drawable.level_3)
                "level_4" -> ivLevelBike.setImageResource(R.drawable.level_4)
                "level_5" -> ivLevelBike.setImageResource(R.drawable.level_5)
                "level_6" -> ivLevelBike.setImageResource(R.drawable.level_6)
                "level_7" -> ivLevelBike.setImageResource(R.drawable.level_7)
            }
            tvTotalRunsBike.text = "${totalsBike.totalRuns}/${levelBike.RunsTarget}"
            var porcent = totalsBike.totalDistance!!.toInt() * 100 / levelBike.DistanceTarget!!.toInt()
            tvTotalDistanceBike.text = "${porcent.toInt()}%"

            var csbDistanceBike = findViewById<CircularSeekBar>(R.id.csbDistanceBike)
            csbDistanceBike.max = levelBike.DistanceTarget!!.toFloat()
            if (totalsBike.totalDistance!! >= levelBike.DistanceTarget!!.toDouble())
                csbDistanceBike.progress = csbDistanceBike.max
            else
                csbDistanceBike.progress = totalsBike.totalDistance!!.toFloat()

            var csbRunsBike = findViewById<CircularSeekBar>(R.id.csbRunsBike)
            csbRunsBike.max = levelBike.RunsTarget!!.toFloat()
            if (totalsBike.totalRuns!! >= levelBike.RunsTarget!!.toInt())
                csbRunsBike.progress = csbRunsBike.max
            else
                csbRunsBike.progress = totalsBike.totalRuns!!.toFloat()

        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        /* val sydney = LatLng(-34.0, 151.0)
         mMap.addMarker(
             MarkerOptions()
             .position(sydney)
             .title("Marker in Sydney"))
         mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMapLongClickListener { mapCentered = false }
        mMap.setOnMyLocationClickListener(this)
        mMap.setOnMapClickListener { mapCentered = false }

        managedLocation()
        mLocationCallBack
        centerMap(init_lt, init_ln)
    }

    private var mapCentered: Boolean = true

    private fun centerMap(lt: Double, ln: Double): Unit {

        val posMap = LatLng(lt, ln)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posMap, 16f), 1000, null)

    }

    @SuppressLint("MissingPermission")
    private fun enabledMyLocation(): Unit {
        if (!::mMap.isInitialized) return
        if (allPermissionsGrantedGPS()) {
            mMap.isMyLocationEnabled = true
        } else requestPermissionLocation()
    }

    fun selectBike(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("Bike")
    }

    fun selectRollerSkate(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("RollerSkate")
    }

    fun selectRunning(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("Running")
    }

    private fun selectSport(sport: String){

        sportSelected = sport

        var lySportBike = findViewById<LinearLayout>(R.id.lySportBike)
        var lySportRollerSkate = findViewById<LinearLayout>(R.id.lySportRollerSkate)
        var lySportRunning = findViewById<LinearLayout>(R.id.lySportRunning)

        when (sport){
            "Bike"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_BIKE

                lySportBike.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
            }
            "RollerSkate"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE

                lySportBike.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
            }
            "Running"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_RUNNING

                lySportBike.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATIN_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.lyOpenerButton.isEnabled = true
                } else {
                    if (binding.lyMap.height > 0) {
                        binding.lyFragmentMap.translationY = -300f
                        binding.ivOpenClose.rotation = 0f
                    }
                    binding.lyOpenerButton.isEnabled = false
                }

            }
        }
    }

    private var chronometer: Runnable = object : Runnable {
        override fun run() {
            try {
                if (activatedGPS && timeInSeconds.toInt() % INTERVAL_LOCATION == 0) {
                    managedLocation()
                }
                if (binding.swIntervalMode.isChecked) {
                    checkStopRun(timeInSeconds)
                    checkNewRound(timeInSeconds)
                }
                timeInSeconds++
                updateStopWatchView()
            } finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED


    }
    @SuppressLint("MissingPermission")
    private fun managedLocation(): Unit {
        if (checkPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                requestNewLocationData()
            }
        } else requestPermissionLocation()
    }
    @SuppressLint("MissingPermission")
    fun requestNewLocationData(): Unit {
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()
        )

    }



    private val mLocationCallBack: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationresult: LocationResult) {
            var mLastLocation: Location? = locationresult.lastLocation
            init_lt = mLastLocation!!.latitude
            init_ln = mLastLocation!!.longitude
            if(timeInSeconds >0L){
                registerNewLocation(mLastLocation)
            }
        }
    }
    private fun registerNewLocation(location: Location){
        var new_latitude: Double = location.latitude
        var new_longitude: Double = location.longitude

        if (flagSavedLocation) {
            if (timeInSeconds >= INTERVAL_LOCATION) {
                var distanceInterval = calculateDistance(new_latitude, new_longitude)

                var newPos = LatLng(new_latitude,new_longitude)
                (listPoints as ArrayList<LatLng>).add(newPos)

                updateSpeeds(distanceInterval)
                refreshInterfaceData()
            }
        }
        latitude = new_latitude
        longitude = new_longitude

        if (mapCentered) centerMap(latitude, longitude)

        if (minLatitude == null) {
            minLatitude = latitude
            maxLatitude = latitude
            minLongitude = longitude
            maxLongitude = longitude
        }
        if (latitude < minLatitude!!) minLatitude = latitude
        if (latitude > maxLatitude!!) maxLatitude = latitude
        if (longitude < minLongitude!!) minLongitude = longitude
        if (longitude > maxLongitude!!) maxLongitude = longitude

        if (location.hasAltitude()) {
            if (maxAltitude == null) {
                maxAltitude = location.altitude
                minAltitude = location.altitude
            }
            if (location.latitude > maxAltitude!!) maxAltitude = location.altitude
            if (location.latitude < minAltitude!!) minAltitude = location.altitude
        }
    }
    private fun updateSpeeds(d: Double) {
        //la distancia se calcula en km, asi que la pasamos a metros para el calculo de velocidadr
        //convertirmos m/s a km/h multiplicando por 3.6
        speed = ((d * 1000) / INTERVAL_LOCATION) * 3.6
        if (speed > maxSpeed) maxSpeed = speed
        avgSpeed = ((distance * 1000) / timeInSeconds) * 3.6
    }
    private fun refreshInterfaceData(){
        var tvCurrentDistance = findViewById<TextView>(R.id.tvCurrentDistance)
        var tvCurrentAvgSpeed = findViewById<TextView>(R.id.tvCurrentAvgSpeed)
        var tvCurrentSpeed = findViewById<TextView>(R.id.tvCurrentSpeed)
        tvCurrentDistance.text = roundNumber(distance.toString(), 2)
        tvCurrentAvgSpeed.text = roundNumber(avgSpeed.toString(), 1)
        tvCurrentSpeed.text = roundNumber(speed.toString(), 1)


        binding.csbCurrentDistance.progress = distance.toFloat()

        binding.csbCurrentAvgSpeed.progress = avgSpeed.toFloat()

        binding.csbCurrentSpeed.progress = speed.toFloat()

        if (speed == maxSpeed){
            binding.csbCurrentMaxSpeed.max = binding.csbRecordSpeed.max
            binding.csbCurrentMaxSpeed.progress = speed.toFloat()

            binding.csbCurrentSpeed.max = binding.csbRecordSpeed.max
        }
    }
    private fun calculateDistance(n_lt: Double, n_lg: Double): Double{
        val radioTierra = 6371.0 //en kilómetros

        val dLat = Math.toRadians(n_lt - latitude)
        val dLng = Math.toRadians(n_lg - longitude)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val va1 =
            Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                    * Math.cos(Math.toRadians(latitude)) * Math.cos(
                Math.toRadians( n_lt  )
            ))
        val va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1))
        var n_distance =  radioTierra * va2

        //if (n_distance < LIMIT_DISTANCE_ACCEPTED) distance += n_distance

        distance += n_distance
        return n_distance
    }

    private fun checkStopRun(Secs: Long): Unit {
        var secAux: Long = Secs
        while (secAux.toInt() > ROUND_INTERVAL) secAux -= ROUND_INTERVAL
        if (secAux.toInt() == TIME_RUNNING) {
            binding.tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_walking))
            binding.lyRoundProgressBg.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.chrono_walking
                )
            )
            binding.lyRoundProgressBg.translationX = -widthAnimations.toFloat()
        } else {
            updateProgressBarRound(Secs)
        }

    }

    private fun managedStartStop(): Unit {

    }

    private fun updateProgressBarRound(secs: Long): Unit {
        var s = secs.toInt()
        while (s >= ROUND_INTERVAL) s -= ROUND_INTERVAL
        s++


        if (binding.tvChrono.getCurrentTextColor() == ContextCompat.getColor(
                this,
                R.color.chrono_running
            )
        ) {

            var movement = -1 * (widthAnimations - (s * widthAnimations / TIME_RUNNING)).toFloat()
            animateViewofFloat(binding.lyRoundProgressBg, "translationX", movement, 1000L)
        }
        if (binding.tvChrono.getCurrentTextColor() == ContextCompat.getColor(
                this,
                R.color.chrono_walking
            )
        ) {
            s -= TIME_RUNNING
            var movement =
                -1 * (widthAnimations - (s * widthAnimations / (ROUND_INTERVAL - TIME_RUNNING))).toFloat()
            animateViewofFloat(binding.lyRoundProgressBg, "translationX", movement, 1000L)

        }
    }
    private fun checkNewRound(Secs: Long):Unit{
        if(Secs.toInt()%ROUND_INTERVAL== 0 && Secs.toInt()>0){
            rounds++
            binding.tvRounds.text = "Round: $rounds"

            binding.tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            binding.lyRoundProgressBg.setBackgroundColor(ContextCompat.getColor(this, R.color.chrono_running))
            binding.lyRoundProgressBg.translationX = -widthAnimations.toFloat()
        }else{
            updateProgressBarRound(Secs)
        }
    }

    private fun updateStopWatchView() {
        binding.tvChrono.text = getFormattedStopWatch(timeInSeconds * 1000)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainContext = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        println("iniciando aplicacion")



        loadfromDB()

        initToolBar()
        initObjects()
        initNavigationView()
        eventsViews()
    }


    private fun eventsViews(): Unit {

        binding.swVolumes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Utility.animateViewofInt(
                    binding.swVolumes,
                    "textColor",
                    ContextCompat.getColor(this, R.color.orange),
                    500
                )
                var value = 400
                if (isChecked) value = 600
                Utility.setHeightLinearLayout(binding.lySettingsVolumesSpace, value)

                Utility.animateViewofFloat(binding.lySettingsVolumes, "translationY", 0f, 500)
            } else {
                binding.swVolumes.setTextColor(ContextCompat.getColor(this, R.color.white))
                Utility.setHeightLinearLayout(binding.lySettingsVolumesSpace, 0)
                binding.lySettingsVolumes.translationY = -300f

            }
        }
        binding.swChallenges.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                Utility.animateViewofInt(
                    view,
                    "textColor",
                    ContextCompat.getColor(this, R.color.orange),
                    500
                )
                Utility.setHeightLinearLayout(binding.lyChallengesSpace, 500)
                Utility.animateViewofFloat(binding.lySettingsVolumes, "translationY", 0f, 500)

            } else {
                binding.swChallenges.setTextColor(ContextCompat.getColor(this, R.color.white))
                Utility.setHeightLinearLayout(binding.lyChallengesSpace, 0)
                binding.lyChallenges.translationY = -300f

                challengeDistance = 0f
                challengeDuration = 0
            }
        }
        binding.swIntervalMode.setOnCheckedChangeListener { buttonView, isChecked ->
            inflateIntervalMode(isChecked)
        }

    }

    private fun inflateChallenges(): Unit {
        showChallenge("duration")
    }
    fun showDuration():Unit{
        showChallenge("distance")
    }
    fun showChallenge(option: String):Unit{
        when(option){
            "duration"->{
                binding.lyChallengeDuration.translationZ = 5f
                binding.lyChallengeDuration.translationZ = 0f

                binding.tvChallengeDuration.setTextColor(ContextCompat.getColor(this, R.color.orange))
                binding.tvChallengeDuration.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))

                binding.tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.gray_medium))

                challengeDistance = 0f

              getChallengeDuration(binding.npChallengeDurationHH.value,
                    binding.npChallengeDurationMM.value,
                    binding.npChallengeDurationSS.value)
            }
            "distance"->{
                binding.lyChallengeDuration.translationZ = 0f
                binding.lyChallengeDuration.translationZ = 5f

                binding.tvChallengeDuration.setTextColor(ContextCompat.getColor(this, R.color.gray_medium))
                binding.tvChallengeDuration.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))

                binding.tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.gray_medium))
                binding.tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.white))
                challengeDuration = 0
                challengeDistance = binding.npChallengeDistance.value.toFloat()

            }

        }

    }
    private fun getChallengeDuration(a: Int, b: Int, c: Int): Unit{
         challengeDuration = Utility.getSecondFromWatch(String.format("%02d:%02d:%02d", a,b,c))
    }
    private fun initStopWatch():Unit{
        binding.tvChrono.text = getString(R.string.init_stop_watch_value)
    }
    fun inflateIntervalMode(isChecked: Boolean):Unit{
        if(isChecked){
            Utility.animateViewofInt(binding.swIntervalMode, "textColor",
            ContextCompat.getColor(this, R.color.orange), 500)

            Utility.setHeightLinearLayout(binding.lyIntervalModeSpace, 600)
            Utility.animateViewofFloat(binding.lyIntervalMode, "translationY", 0f, 500)

            Utility.animateViewofFloat(binding.tvChrono, "translationX", -110f, 500)
            binding.tvRounds.text = getString(R.string.rounds)

            Utility.animateViewofInt(
                binding.tvRounds, "textColor",
                ContextCompat.getColor(this, R.color.white), 500
            )

            Utility.setHeightLinearLayout(binding.lySoftTrack, 120)
            Utility.setHeightLinearLayout(binding.lySoftVolume, 120)
            if (binding.swVolumes.isChecked) {
                Utility.setHeightLinearLayout(binding.lySettingsVolumesSpace, 600)
            }
            TIME_RUNNING= Utility.getSecFromWatch(binding.tvRunningTime.text.toString())

        }else{
            Utility.animateViewofInt(binding.swIntervalMode, "textColor",
                ContextCompat.getColor(this, R.color.white), 500)

            Utility.setHeightLinearLayout(binding.lyIntervalModeSpace, 0)
            binding.lyIntervalMode.translationY=-200f
            Utility.animateViewofFloat(binding.tvChrono, "translationX", 0f, 500)
            binding.tvRounds.text=""

            Utility.setHeightLinearLayout(binding.lySoftTrack, 0)
            Utility.setHeightLinearLayout(binding.lySoftVolume, 0)





        }
    }

    private fun initTotals():Unit{
        totalsBike = Totals()
        totalsRollerSkate = Totals()
        totalsRunning = Totals()

        totalsBike.totalRuns = 0
        totalsBike.totalDistance = 0.0
        totalsBike.totalTime = 0
        totalsBike.recordDistance = 0.0
        totalsBike.recordSpeed = 0.0
        totalsBike.recordAvgSpeed = 0.0

        totalsRollerSkate.totalRuns = 0
        totalsRollerSkate.totalDistance = 0.0
        totalsRollerSkate.totalTime = 0
        totalsRollerSkate.recordDistance = 0.0
        totalsRollerSkate.recordSpeed = 0.0
        totalsRollerSkate.recordAvgSpeed = 0.0

        totalsRunning.totalRuns = 0
        totalsRunning.totalDistance = 0.0
        totalsRunning.totalTime = 0
        totalsRunning.recordDistance = 0.0
        totalsRunning.recordSpeed = 0.0
        totalsRunning.recordAvgSpeed = 0.0
    }
    private fun initLevels():Unit{
        levelSelectedSport = Level()
        levelBike = Level()
        levelRollerSkate = Level()
        levelRunning = Level()

        levelsListBike = arrayListOf()
        levelsListBike.clear()

        levelsListRollerSkate = arrayListOf()
        levelsListRollerSkate.clear()

        levelsListRunning = arrayListOf()
        levelsListRunning.clear()

        levelBike.name = "turtle"
        levelBike.image = "level_1"
        levelBike.RunsTarget = 5
        levelBike.DistanceTarget = 40

        levelRollerSkate.name = "turtle"
        levelRollerSkate.image = "level_1"
        levelRollerSkate.RunsTarget = 5
        levelRollerSkate.DistanceTarget = 20

        levelRunning.name = "turtle"
        levelRunning.image = "level_1"
        levelRunning.RunsTarget = 5
        levelRunning.DistanceTarget = 10
    }
    private fun initObjects(): Unit {
        initStopWatch()
        initTotals()
        initLevels()
        Utility.setHeightLinearLayout(binding.lyMap, 0)
        Utility.setHeightLinearLayout(binding.lyIntervalModeSpace, 0)
        Utility.setHeightLinearLayout(binding.lyChallengesSpace, 0)
        Utility.setHeightLinearLayout(binding.lySettingsVolumesSpace, 0)
        Utility.setHeightLinearLayout(binding.lySoftTrack, 0)
        Utility.setHeightLinearLayout(binding.lySoftVolume, 0)

        binding.lyFragmentMap.translationY = -300f
        binding.lyIntervalMode.translationY = -300f
        binding.lyChallenges.translationY = -300f
        binding.lySettingsVolumes.translationY = -300f


        //reiniciar seekbar
        binding.csbCurrentDistance.progress =0f
        binding.csbChallengeDistance.progress =0f
        binding.csbCurrentAvgSpeed.progress =0f
        binding.csbCurrentSpeed.progress =0f
        binding.csbCurrentMaxSpeed.progress =0f

        //marcar ceros los text de tiempos
        binding.tvDistanceRecord.text = ""
        binding.tvAvgSpeedRecord.text = ""
        binding.tvMaxSpeedRecord.text = ""


        var pos_see: Float = 0f
        var esAnterior: Boolean = false
        val formateador = DecimalFormat("#")
        // binding.csbRunWalk.max = 100f
        //  binding.csbRunWalk.progress = 150f
        binding.csbRunWalk.max = 300f
        binding.csbRunWalk.progress = 150f
        binding.csbRunWalk.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    var STEPS_UX: Int = 15
                    if (ROUND_INTERVAL > 600) STEPS_UX = 60
                    if (ROUND_INTERVAL > 1800) STEPS_UX = 300
                    var set: Int = 0
                    var p = progress.toInt()

                    var limit = 60
                    if (ROUND_INTERVAL > 1800) limit = 300

                    if (p % STEPS_UX != 0 && progress != binding.csbRunWalk.max) {
                        while (p >= limit) p -= limit
                        while (p >= STEPS_UX) p -= STEPS_UX
                        if (STEPS_UX - p > STEPS_UX / 2) set = -1 * p
                        else set = STEPS_UX - p

                        if (binding.csbRunWalk.progress + set > binding.csbRunWalk.max)
                            binding.csbRunWalk.progress = binding.csbRunWalk.max
                        else
                            binding.csbRunWalk.progress = binding.csbRunWalk.progress + set
                    }
                }
                if(binding.csbRunWalk.progress== 0f) managedEnableButtonsRun(false, false)
                else managedEnableButtonsRun(false, true)

                binding.tvRunningTime.text =
                    getFormattedStopWatch((binding.csbRunWalk.progress.toInt() * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                binding.tvWalkingTime.text =
                    getFormattedStopWatch(((ROUND_INTERVAL - binding.csbRunWalk.progress.toInt()) * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                TIME_RUNNING = Utility.getSecFromWatch(binding.tvRunningTime.text.toString())

            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {

            }
        })


        //internval
        binding.npDurationInterval.minValue = 1
        binding.npDurationInterval.maxValue = 2
        binding.npDurationInterval.value = 5
        binding.npDurationInterval.wrapSelectorWheel = true
        binding.npDurationInterval.setFormatter(NumberPicker.Formatter { i ->
            String.format("%02d", i)
        })

        binding.npDurationInterval.setOnValueChangedListener { picker, oldVal, newVal ->
            binding.csbRunWalk.max = (newVal * 60).toFloat()
            binding.csbRunWalk.progress = binding.csbRunWalk.max / 2


            binding.tvRunningTime.text =
                Utility.getFormattedStopWatch(((newVal * 60 / 2) * 1000).toLong()).subSequence(3, 8)
            binding.tvWalkingTime.text = binding.tvRunningTime.text

            ROUND_INTERVAL = newVal * 60

            TIME_RUNNING = ROUND_INTERVAL / 2
        }

        binding.npChallengeDistance.minValue = 1
        binding.npChallengeDistance.maxValue = 300
        binding.npChallengeDistance.value = 10
        binding.npChallengeDistance.wrapSelectorWheel = true


        binding.npChallengeDistance.setOnValueChangedListener { picker, oldVal, newVal ->

            challengeDistance = newVal.toFloat()
            binding.csbChallengeDistance.max = newVal.toFloat()
            binding.csbChallengeDistance.progress = newVal.toFloat()

            challengeDuration = 0
            if (binding.csbChallengeDistance.max > binding.csbRecordDistance.max) {
                binding.csbCurrentDistance.max = binding.csbChallengeDistance.max
            }
        }



        binding.npChallengeDurationHH.minValue = 0
        binding.npChallengeDurationHH.maxValue = 23
        binding.npChallengeDurationHH.value = 1
        binding.npChallengeDurationHH.wrapSelectorWheel = true
        binding.npChallengeDurationHH.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        binding.npChallengeDurationMM.minValue = 0
        binding.npChallengeDurationMM.maxValue = 59
        binding.npChallengeDurationMM.value = 0
        binding.npChallengeDurationMM.wrapSelectorWheel = true
        binding.npChallengeDurationMM.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        binding.npChallengeDurationSS.minValue = 0
        binding.npChallengeDurationSS.maxValue = 59
        binding.npChallengeDurationSS.value = 0
        binding.npChallengeDurationSS.wrapSelectorWheel = true
        binding.npChallengeDurationSS.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        binding.npChallengeDurationHH.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                newVal,
                binding.npChallengeDurationMM.value,
                binding.npChallengeDurationSS.value
            )
        }
        binding.npChallengeDurationMM.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                binding.npChallengeDurationHH.value,
                newVal,
                binding.npChallengeDurationSS.value
            )
        }
        binding.npChallengeDurationSS.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                binding.npChallengeDurationHH.value,
                binding.npChallengeDurationMM.value,
                newVal
            )
        }

        binding.btStart.setOnClickListener {
            startOrStopButtonClicked()
        }

        binding.tvReset.setOnClickListener {
            resetClicked()
        }

        binding.fbCamera.isVisible = false

        initMap()

        /*binding.lyOpenerButton.setOnClickListener {
            callShowHideMap()
        }*/
        initPreferences()
        recoveryPreferences()

        binding.cbAutoFinish.setOnClickListener {
            showPopUp()
        }
    }
    private fun initPreferences():Unit{
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }
    private fun initMap():Unit{
        listPoints = arrayListOf()
        (listPoints as ArrayList<LatLng>).clear()

        createMapFragment()
        binding.lyOpenerButton.isEnabled = allPermissionsGrantedGPS()
    }
    fun changeTypeMap(v: View){
        if(mMap.mapType == GoogleMap.MAP_TYPE_HYBRID){
            mMap.mapType == GoogleMap.MAP_TYPE_NORMAL
            binding.ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        } else {
            mMap.mapType == GoogleMap.MAP_TYPE_HYBRID
            binding.ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }

    fun callCenterMap(v: View) {
        mapCentered = true
        if (latitude == 0.0) centerMap(init_lt, init_ln)
        else centerMap(latitude, longitude)
    }

    fun callShowHideMap(v: View) {
        if (allPermissionsGrantedGPS()) {
            var lyMap = findViewById<LinearLayout>(R.id.lyMap)
            var lyFragmentMap = findViewById<LinearLayout>(R.id.lyFragmentMap)
            var ivOpenClose = findViewById<ImageView>(R.id.ivOpenClose)

            if (lyMap.height == 0) {
                setHeightLinearLayout(lyMap, 1157)
                animateViewofFloat(lyFragmentMap, "translationY", 0f, 0)
                ivOpenClose.setRotation(180f)
            } else {
                setHeightLinearLayout(lyMap, 0)
                lyFragmentMap.translationY = -300f
                ivOpenClose.setRotation(0f)
            }

        } else requestPermissionLocation()
    }
    private fun createMapFragment():Unit{
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun resetClicked(): Unit {

        savePreferences()
        showPopUp()
        resetVariablesRun()
        resetTimeClicked()
        resetInterface()
    }

    private fun recoveryPreferences() {
        if (sharedPreferences.getString(key_userApp, "null") == useremail) {
            sportSelected = sharedPreferences.getString(key_selectedSport, "Running").toString()

            binding.swIntervalMode.isChecked = sharedPreferences.getBoolean(key_modeInterval, false)
            if (binding.swIntervalMode.isChecked) {
                binding.npDurationInterval.value = sharedPreferences.getInt(key_intervalDuration, 5)
                ROUND_INTERVAL = binding.npDurationInterval.value * 60
                binding.csbRunWalk.progress =
                    sharedPreferences.getFloat(key_progressCircularSeekBar, 150.0f)
                binding.csbRunWalk.max = sharedPreferences.getFloat(key_maxCircularSeekBar, 300.0f)
                binding.tvRunningTime.text = sharedPreferences.getString(key_runningTime, "2:30")
                binding.tvWalkingTime.text = sharedPreferences.getString(key_walkingTime, "2:30")
                binding.swIntervalMode.callOnClick()
            }

            binding.swChallenges.isChecked = sharedPreferences.getBoolean(key_modeChallenge, false)
            if (binding.swChallenges.isChecked) {
                binding.swChallenges.callOnClick()
                if (sharedPreferences.getBoolean(key_modeChallengeDuration, false)) {
                    binding.npChallengeDurationHH.value =
                        sharedPreferences.getInt(key_challengeDurationHH, 1)
                    binding.npChallengeDurationMM.value =
                        sharedPreferences.getInt(key_challengeDurationMM, 0)
                    binding.npChallengeDurationSS.value =
                        sharedPreferences.getInt(key_challengeDurationSS, 0)
                    getChallengeDuration(
                        binding.npChallengeDurationHH.value,
                        binding.npChallengeDurationMM.value,
                        binding.npChallengeDurationSS.value
                    )
                    challengeDistance = 0f

                    showChallenge("duration")
                }
                if (sharedPreferences.getBoolean(key_modeChallengeDistance, false)) {
                    binding.npChallengeDistance.value =
                        sharedPreferences.getInt(key_challengeDistance, 10)
                    challengeDistance = binding.npChallengeDistance.value.toFloat()
                    challengeDuration = 0

                    showChallenge("distance")
                }
            }
            binding.cbNotify.isChecked = sharedPreferences.getBoolean(key_challengeNofify, true)
            binding.cbAutoFinish.isChecked =
                sharedPreferences.getBoolean(key_challengeAutofinish, false)

            /*   binding.sbHardVolume.progress = sharedPreferences.getInt(key_hardVol, 100)
               binding.sbSoftVolume.progress = sharedPreferences.getInt(key_softVol, 100)
               binding.sbNotifyVolume.progress = sharedPreferences.getInt(key_notifyVol, 100)*/

        }

    }

    private fun savePreferences(): Unit {
        editor.clear()
        editor.apply {
            putString(key_userApp, useremail)
            putString(key_provider, providerSession)

            putString(key_selectedSport, sportSelected)

            putBoolean(key_modeInterval, binding.swIntervalMode.isChecked)
            putInt(key_intervalDuration, binding.npDurationInterval.value)
            putFloat(key_progressCircularSeekBar, binding.csbRunWalk.progress)
            putFloat(key_maxCircularSeekBar, binding.csbRunWalk.max)
            putString(key_runningTime, binding.tvRunningTime.text.toString())
            putString(key_walkingTime, binding.tvWalkingTime.text.toString())

            putBoolean(key_modeChallenge, binding.swChallenges.isChecked)
            putBoolean(key_modeChallengeDuration, !(challengeDuration == 0))
            putInt(key_challengeDurationHH, binding.npChallengeDurationHH.value)
            putInt(key_challengeDurationMM, binding.npChallengeDurationMM.value)
            putInt(key_challengeDurationSS, binding.npChallengeDurationSS.value)
            putBoolean(key_modeChallengeDistance, !(challengeDistance == 0f))
            putInt(key_challengeDistance, binding.npChallengeDistance.value)


            putBoolean(key_challengeNofify, binding.cbNotify.isChecked)
            putBoolean(key_challengeAutofinish, binding.cbAutoFinish.isChecked)


        }.apply()
    }
    private fun resetInterface():Unit{
        binding.fbCamera.isVisible = false

        activatedGPS = true

        binding.tvCurrentDistance.text= "0.0"
        binding.tvCurrentAvgSpeed.text= "0.0"
        binding.tvCurrentSpeed.text= "0.0"

        binding.csbCurrentDistance.progress =0f
        binding.csbCurrentAvgSpeed.progress =0f
        binding.csbCurrentSpeed.progress =0f
        binding.csbCurrentMaxSpeed.progress =0f

        binding.tvDistanceRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        binding.tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        binding.tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))


        binding.lyChronoProgressBg.translationX = -widthAnimations.toFloat()

        binding.lyRoundProgressBg.translationX = - widthAnimations.toFloat()

        binding.swIntervalMode.isClickable = true
        binding.npDurationInterval.isEnabled = true
        binding.csbRunWalk.isEnabled = true

        binding.swChallenges.isClickable = true
        binding.npChallengeDistance.isEnabled = true
        binding.npChallengeDurationHH.isEnabled = true
        binding.npChallengeDurationMM.isEnabled = true
        binding.npChallengeDurationSS.isEnabled = true

    }

    private fun resetVariablesRun(): Unit {
        timeInSeconds = 0
        rounds = 1

        timeInSeconds = 0
        rounds = 1


        distance = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0

        minAltitude = null
        maxAltitude = null
        minLatitude = null
        maxLatitude = null
        minLongitude = null
        maxLongitude = null

        (listPoints as ArrayList<LatLng>).clear()

        challengeDistance = 0f
        challengeDuration = 0

        activatedGPS = true
        flagSavedLocation = false

        initStopWatch()
    }

    private fun resetTimeClicked(): Unit {
        initStopWatch()
        managedEnableButtonsRun(false, true)
        // binding.btStart.background = getDrawable(R.drawable.circle_background_toplay)
        binding.tvChrono.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun startOrStopButtonClicked(): Unit {
        manageStartStop()
    }

    private fun initPermissionGPS(): Unit {
        if (allPermissionsGrantedGPS()) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        } else requestPermissionLocation()

    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSION_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionLocation(): Unit {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun manageStartStop(): Unit {
        if (timeInSeconds == 0L && !isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.alertActivationGPSTitle)
                .setMessage(R.string.alertActivationGPSDescription)
                .setPositiveButton(
                    R.string.aceptActivationGps,
                    DialogInterface.OnClickListener { dialog, which ->
                        activationLocation()
                    }
                )
                .setNegativeButton(R.string.ignoreActivationGPS,
                    DialogInterface.OnClickListener { dialog, which ->
                        activatedGPS = false
                        manageRun()
                    }
                ).show()

        } else manageRun()
    }

    private fun activationLocation(): Unit {
            var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun manageRun(): Unit {
        if (timeInSeconds.toInt() == 0) {

            binding.fbCamera.isVisible = true

            binding.swIntervalMode.isClickable = false
            binding.npDurationInterval.isEnabled = false
            binding.csbRunWalk.isEnabled = false

            binding.swChallenges.isClickable = false
            binding.npChallengeDistance.isEnabled = false
            binding.npChallengeDurationHH.isEnabled = false
            binding.npChallengeDurationMM.isEnabled = false
            binding.npChallengeDurationSS.isEnabled = false

            binding.tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            if(activatedGPS){
                flagSavedLocation = false
                managedLocation()
                flagSavedLocation = true
                managedLocation()
            }

        }
        if (!startButtonClicked) {
            startButtonClicked = true
            startTime()
            managedEnableButtonsRun(false, true)
        } else {
            startButtonClicked = false
            stopTime()
            managedEnableButtonsRun(true, true)
        }
    }

    private fun managedEnableButtonsRun(e_reset: Boolean, e_run: Boolean): Unit {
        binding.tvReset.isEnabled = e_reset
        binding.btStart.isEnabled = e_run
        if (e_reset) {
            binding.tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            ObjectAnimator.ofFloat(binding.tvReset, "translationY", 0f).apply {
                duration = 500
                start()
            }
        }else{
            binding.tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            ObjectAnimator.ofFloat(binding.tvReset, "translationY", 150f).apply {
                duration = 500
                start()
            }
        }
        if(e_run){
            if(startButtonClicked){
                binding.btStart.background = getDrawable(R.drawable.circle_background_topause)
                binding.btStartLabel.text = getString(R.string.stop)
            }else{
                binding.btStart.background = getDrawable(R.drawable.circle_background_toplay)
                binding.btStartLabel.text = getString(R.string.start)
            }
        }
        else binding.btStart.background = getDrawable(R.drawable.circle_background_todisable)
    }

    private fun stopTime(): Unit {
        mHandler?.removeCallbacks(chronometer)
    }

    private fun startTime(): Unit {
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }

    fun inflateVolumes(v: View): Unit {

    }

    private fun initNavigationView() {
        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        var headerView: View =
            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            signOut()

    }

    private fun initToolBar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.bar_title,
            R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toggle)

        toggle.syncState()
    }

    private fun signOut(): Unit {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun showMsg(msg: String): Unit {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_record -> callRecordActictivity()
            R.id.nav_item_signout -> signOut()
            R.id.nav_item_clearpreferences -> alertClearPreferences()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun alertClearPreferences(): Unit {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alertClearPreferencesTitle))
            .setMessage(getString(R.string.alertClearPreferencesDescription))
            .setPositiveButton(
                android.R.string.ok,
                DialogInterface.OnClickListener { dialog, which ->
                    callClearPreferences()
                })
            .setNegativeButton(
                android.R.string.cancel,
                DialogInterface.OnClickListener { dialog, which ->

                })
            .setCancelable(true)
            .show()

    }

    private fun callClearPreferences(): Unit {
        editor.clear().apply()
        Toast.makeText(this,
            "Se realizo el borrado de preferencias correctamente",
            Toast.LENGTH_SHORT).show()
    }
    private fun showPopUp(){
        var rlMain = findViewById<RelativeLayout>(R.id.rlMain)
        rlMain.isEnabled = false

        binding.lyPopupRun.isVisible = true

        var lyWindow = findViewById<LinearLayout>(R.id.lyWindow)
        ObjectAnimator.ofFloat(lyWindow, "translationX", 0f ).apply {
            duration = 200L
            start()
        }

        loadDataPopUp()

    }
    private fun loadDataPopUp():Unit{
        //showHeaderPopUp()
        //showMedals()
        showDataRun()
    }
    private fun showDataRun():Unit{
        binding.tvDurationRun.setText(binding.tvChrono.text)
        if(challengeDuration>0){
            setHeightLinearLayout(binding.lyChallengeDurationRun, 128)
            binding.tvChallengeDurationRun.setText(getFormattedStopWatch((challengeDuration*1000).toLong()))
        }else setHeightLinearLayout(binding.lyIntervalRun, 0)
        binding.tvDistanceRun.setText(roundNumber(distance.toString(), 2))
        if(challengeDistance >0f){
            setHeightLinearLayout(binding.lyChallengeDistancePopUp, 0 )
            binding.tvChallengeDurationRun.setText(challengeDuration.toString())
        }else setHeightLinearLayout(binding.lyChallengeDistancePopUp, 0)

        if(maxAltitude == null){
            setHeightLinearLayout(binding.lyUnevennessRun, 0)
        }else{
            setHeightLinearLayout(binding.lyUnevennessRun, 128)
            binding.tvMaxUnevennessRun.setText(maxAltitude!!.toInt().toString())
            binding.tvMinUnevennessRun.setText(minAltitude!!.toInt().toString())
        }

        binding.tvAvgSpeedRun.setText(roundNumber(avgSpeed.toString(), 1))
        binding.tvMaxSpeedRun.setText(roundNumber(maxSpeed.toString(), 1))


    }

    fun closePopUp(v: View):Unit{
        closePopUpRun()
    }
    private fun closePopUpRun():Unit{
        hidePopUpRun()
        binding.rlMain.isEnabled = true
        resetVariablesRun()
    }
    private fun hidePopUpRun():Unit{
        binding.lyWindow.translationY = 400f
        binding.lyPopupRun.isVisible = false
    }

    private fun callRecordActictivity(): Unit {
        startActivity(Intent(this, RecordActivity::class.java))
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {

    }

}