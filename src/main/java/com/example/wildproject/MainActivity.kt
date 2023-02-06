package com.example.wildproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wildproject.LoginActivity.Companion.useremail
import com.example.wildproject.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private var challengeDistance: Float = 0f
    private var challengeDuration: Int = 0

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

            Utility.animateViewofInt(binding.tvRounds, "textColor",
                ContextCompat.getColor(this, R.color.white), 500)

            Utility.setHeightLinearLayout(binding.lySoftTrack, 120)
            Utility.setHeightLinearLayout(binding.lySoftVolume, 120)

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

        binding.lyFragmentMap.translationY = -300f
        binding.lyIntervalMode.translationY = -300f
        binding.lyChallenges.translationY = -300f
        binding.lySettingsVolumes.translationY = -300f
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