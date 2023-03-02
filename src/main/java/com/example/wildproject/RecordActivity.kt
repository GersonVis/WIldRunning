package com.example.wildproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wildproject.LoginActivity.Companion.useremail
import com.example.wildproject.databinding.ActivityRecordBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.Math.random

class RecordActivity : AppCompatActivity() {
    private var sportSelected: String = "Running"

    private lateinit var binding: ActivityRecordBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var runsArrayList: ArrayList<Runs>
    private lateinit var myAdapter: RunsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

       //  rellenarFireBase()

        var toolbar: Toolbar = findViewById(R.id.toolbar_record)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title_record)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        recyclerView = binding.rvRecords
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        runsArrayList = arrayListOf()

        //conectaos el adapter
        myAdapter = RunsAdapter(runsArrayList)
        recyclerView.adapter = myAdapter




    }

    private fun rellenarFireBase(): Unit {
        val dbTotal: FirebaseFirestore = FirebaseFirestore.getInstance()
        var r = (0..10).random()
        random()
        var lists: Array<String> = arrayOf("runsBike", "runsRunning", "runsRollerSkate")
        for (sport in lists) {
            for (pos in 0..1) {
                dbTotal.collection("$sport")
                    .document("$useremail${(0..100).random()}${(0..100).random()}").set(
                    hashMapOf(
                        "user" to useremail,
                        "date" to "00:00:00",
                        "startTime" to "00:00:00",
                        "sport" to sportSelected,
                        "activatedGPS" to true,
                        "duration" to "00:00:00",
                        "distance" to 343f,
                        "avgSpeed" to 343f,
                        "maxSpeed" to 343f,
                        "minAltitude" to 343,
                        "maxAltitude" to 343,
                        "minLatitude" to 343,
                        "maxLatitude" to 343,
                        "minLongitude" to 343,
                        "maxLongitude" to 343,
                        "centerLatitude" to 323f,
                        "centerLongitude" to 33f,
                        "medalDistance" to "343",
                        "medalAvgSpeed" to "343",
                        "medalMaxSpeed" to "343",
                        "challengeDistance" to 0.0,
                        "challengeDuration" to ""
                    )

                ).addOnSuccessListener {
                    println("se realizo correctamente")
                }.addOnFailureListener {
                    throw(Exception("fallo al crear"))
                }
            }
        }

        /*  for(list in lists){
              for(t in 1..10){
                  dbTotal.collection(list+"/visoso126@gmail.com/323423").document("level_$t").set(
                      hashMapOf(
                          "name" to "level_$t",
                          "image" to "level",
                          "RunsTarget" to t*10,
                          "DistanceTarget" to t*3
                      )
                  ).addOnSuccessListener {
                      println("se realizo correctamente")
                  }.addOnFailureListener {
                      throw(Exception("fallo al crear"))
                  }
              }
          }*/

    }

    override fun onResume() {
        super.onResume()
        loadRecyclerView("date", Query.Direction.DESCENDING)
    }

    override fun onPause() {
        super.onPause()
        runsArrayList.clear()
    }

    private fun loadRecyclerView(field: String, order: Query.Direction) {
        runsArrayList.clear()
        var dbRuns = FirebaseFirestore.getInstance()

        dbRuns.collection("runsBike").orderBy(field, order)
            .whereEqualTo("user", useremail)
            .get()
            .addOnSuccessListener { document ->
                Log.i("firebase", "recivido")
                Log.i("firebase", "recivido")
                for (run in document) {

                    var runOb = run.toObject(Runs::class.java)
                    Log.i("firebase", "date: ${runOb.date}")
                    println("date: ${runOb.date}")
                    runsArrayList.add(run.toObject(Runs::class.java))
                }
                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {

            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.order_records_by, menu)
        return true//super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var order: Query.Direction = Query.Direction.DESCENDING

        when (item.itemId) {
            R.id.orderby_date -> {
                if (item.title == getString(R.string.orderby_dateZA)) {
                    item.title = getString(R.string.orderby_dateAZ)
                    order = Query.Direction.DESCENDING
                } else {
                    item.title = getString(R.string.orderby_dateZA)
                    order = Query.Direction.ASCENDING
                }
                loadRecyclerView("date", order)
                return true
            }
            R.id.orderby_duration -> {
                var option = getString(R.string.orderby_durationZA)
                if (item.title == getString(R.string.orderby_durationZA)) {
                    item.title = getString(R.string.orderby_durationAZ)
                    order = Query.Direction.DESCENDING
                } else {
                    item.title = getString(R.string.orderby_durationZA)
                    order = Query.Direction.ASCENDING
                }
                loadRecyclerView("duration", order)
                return true
            }

            R.id.orderby_distance -> {
                var option = getString(R.string.orderby_distanceZA)
                if (item.title == option) {
                    item.title = getString(R.string.orderby_distanceAZ)
                    order = Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_distanceZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("distance", order)
                return true
            }

            R.id.orderby_avgspeed -> {
                var option = getString(R.string.orderby_avgspeedZA)
                if (item.title == getString(R.string.orderby_avgspeedZA)) {
                    item.title = getString(R.string.orderby_avgspeedAZ)
                    order = Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_avgspeedZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("avgSpeed", order)
                return true
            }

            R.id.orderby_maxspeed -> {
                var option = getString(R.string.orderby_maxspeedZA)
                if (item.title == getString(R.string.orderby_maxspeedZA)) {
                    item.title = getString(R.string.orderby_maxspeedAZ)
                    order = Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_maxspeedZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("maxSpeed", order)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun callHome(v: View): Unit {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun loadRunsBike(v: View) {
        sportSelected = "Bike"
        binding.ivBike.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        binding.ivRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
        binding.ivRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))

        loadRecyclerView("date", Query.Direction.DESCENDING)
    }

    fun loadRuns(v: View) {
        sportSelected = "Running"
        binding.ivBike.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
        binding.ivRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
        binding.ivRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        loadRecyclerView("date", Query.Direction.DESCENDING)
    }

    fun loadRunsRollerSkate(v: View) {
        sportSelected = "Skate"
        binding.ivBike.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
        binding.ivRollerSkate.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        binding.ivRunning.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_medium))
        loadRecyclerView("date", Query.Direction.DESCENDING)
    }


}