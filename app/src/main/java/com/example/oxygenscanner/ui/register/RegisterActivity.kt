package com.example.oxygenscanner.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.oxygenscanner.data.Result
import com.example.oxygenscanner.data.model.User
import com.example.oxygenscanner.databinding.ActivityRegisterBinding
import com.example.oxygenscanner.ui.login.LoginActivity
import com.example.oxygenscanner.util.ViewModelFactory
import com.google.firebase.FirebaseApp


class RegisterActivity : AppCompatActivity() {

    private val gender: String? = null
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val edtMobileNumber = binding.mobileNumber
        val edtFullName = binding.fullName
        val btnRegister = binding.register
        val edtEmail = binding.email
        val rbMale = binding.rbMale
        val rbFemale = binding.rbFemale
        val loading = binding.loading

        registerViewModel = ViewModelProvider(this, ViewModelFactory())
            .get(RegisterViewModel::class.java)


        btnRegister.setOnClickListener {
            registerViewModel.register(
                User(
                    fullName = edtFullName.text.toString(),
                    mobileNumber = edtMobileNumber.text.toString(),
                    email = edtEmail.text.toString(),
                    gender = gender
                )
            )
        }

        registerViewModel.registerLiveData.observe(this@RegisterActivity, {
            if (it is Result.Success) {
                startLoginActivity()
            } else if (it is Result.Error) {
                it.exception.message?.let { msg -> showRegisterFailed(msg) }
            }
        })
    }

    private fun startLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finishAffinity()
    }

    private fun showRegisterFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}
