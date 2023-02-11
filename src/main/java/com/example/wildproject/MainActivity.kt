package com.example.wildproject

import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wildproject.LoginActivity.Companion.useremail
import com.example.wildproject.Utility.animateViewofFloat
import com.example.wildproject.Utility.getFormattedStopWatch
import com.example.wildproject.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
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


    private var chronometer: Runnable = object : Runnable {
        override fun run() {
            try {
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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


    private fun initObjects(): Unit {
        initStopWatch()
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
    }
    private fun resetClicked():Unit{
        resetVariablesRun()
        resetTimeClicked()
        resetInterface()
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

    private fun manageStartStop(): Unit {
        if (timeInSeconds == 0L && !isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.alertActivationGPSTitle)
                .setMessage(R.string.alertActivationGPSDescription)
                .setPositiveButton(R.string.aceptActivationGps,
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

    private fun initNavigationView(): Unit {
        var navigatioView: NavigationView = findViewById(R.id.nav_view)
        navigatioView.setNavigationItemSelectedListener(this)

        var headerView: View =
            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigatioView, false)
        navigatioView.removeHeaderView(headerView)
        navigatioView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            signOut()
        }
    }

    private fun initToolBar(): Unit {
        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
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
        }
       drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun callRecordActictivity(): Unit {
        startActivity(Intent(this, RecordActivity::class.java))
    }
}