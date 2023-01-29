package com.example.wildproject

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.example.wildproject.LoginActivity.Companion.useremail

import com.example.wildproject.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.zip.Inflater

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvSession.text = "El usuario registrado es: $useremail"

        binding.tvSession.setOnClickListener {
            showMsg("Se ha cerrado sessión para $useremail")
            signOut()
        }
    }
    private fun signOut():Unit{
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }
    private fun showMsg(msg: String):Unit{
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}