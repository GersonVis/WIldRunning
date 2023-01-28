package com.example.wildproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wildproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {
    companion object{
        lateinit var useremail: String
        lateinit var providerSession: String
    }
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout

    private lateinit var mAuth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth=FirebaseAuth.getInstance()
        binding.lyTerm.visibility = View.INVISIBLE
    }

    fun login(v: View) {
        loginUser()
    }

    private fun loginUser() {
        email = binding.etEmail.text.toString()
        useremail = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()
        binding.btnLogin.text="Conectando..."

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                task->
                binding.btnLogin.text="Iniciar sesión"
                if(task.isSuccessful)goHome(email, "email")
                else{

                    if(binding.lyTerm.visibility==View.INVISIBLE) binding.lyTerm.visibility=View.VISIBLE
                    else{
                        if(binding.cbAccept.isChecked) register()
                    }
                }
            }
    }
    private fun goHome(email: String, provider: String): Unit{
        useremail = email
        providerSession = provider

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    private fun register(): Unit{
        var email:String= binding.etEmail.text.toString()
        var password:String=binding.etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbInstance = FirebaseFirestore.getInstance()
                    dbInstance.collection("users").document(email)
                        .set(hashMapOf(
                            "user" to useremail,
                            "dateRegister" to dateRegister
                        ))
                  goHome(email, "email")
                } else{
                       Toast.makeText(this, "Error, algo salio mal", Toast.LENGTH_SHORT).show()
                    }
            }

    }
}