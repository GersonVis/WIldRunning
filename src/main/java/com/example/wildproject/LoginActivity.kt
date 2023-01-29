package com.example.wildproject

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.wildproject.ValidateEmail.Companion.isEmail
import com.example.wildproject.databinding.ActivityLoginBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
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
        mAuth = FirebaseAuth.getInstance()
        binding.lyTerm.visibility = View.INVISIBLE
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            goHome(currentUser.email.toString(), currentUser.providerId.toString())
        }
        managedButtonLogin()
        binding.etEmail.doOnTextChanged { text, start, before, count ->
            managedButtonLogin()
        }
        binding.etPassword.doOnTextChanged { text, start, before, count ->
            managedButtonLogin()
        }
    }
    private fun managedButtonLogin():Unit{
        if(TextUtils.isEmpty(binding.etPassword.text) || !ValidateEmail.isEmail(binding.etEmail.text.toString())){
            binding.btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            binding.btnLogin.isEnabled = false
        }else{
            binding.btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            binding.btnLogin.isEnabled = true
        }
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun login(v: View) {
        loginUser()
    }

    private fun loginUser() {
        email = binding.etEmail.text.toString()
        useremail = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()
        binding.btnLogin.text = "Conectando..."

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
                        )
                        )
                    goHome(email, "email")
                } else {
                    Toast.makeText(this, "Error, algo salio mal", Toast.LENGTH_SHORT).show()
                }
            }

    }

    fun goTerms(v: View): Unit {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }
    fun forgotPassword(v:View):Unit{
       // val intent = Intent(this, ForgotPasswordActivity::class.java)
      //  startActivity(intent)
        var e = binding.etEmail.text.toString()
        if(!TextUtils.isEmpty(e)){
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener{
                    if(it.isSuccessful) Snackbar.make(binding.root, "Se ha mandando un correó a $e", Snackbar.LENGTH_SHORT).show()
                    else{
                        Snackbar.make(binding.root, "No se encontro al usuario con este correó", Snackbar.LENGTH_SHORT)
                    }
                }
        }else{
            Snackbar.make(binding.root, "El campo email esta vacío", Snackbar.LENGTH_SHORT).show()
        }
    }

}