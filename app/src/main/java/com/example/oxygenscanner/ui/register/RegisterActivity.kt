package com.example.oxygenscanner.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.oxygenscanner.R
import com.example.oxygenscanner.data.Result
import com.example.oxygenscanner.data.model.User
import com.example.oxygenscanner.databinding.ActivityRegisterBinding
import com.example.oxygenscanner.ui.login.LoginActivity
import com.example.oxygenscanner.util.Util
import com.example.oxygenscanner.util.ViewModelFactory
import com.google.firebase.FirebaseApp


class RegisterActivity : AppCompatActivity() {

    private var gender: String? = null
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var edtFullName : EditText
    private lateinit var edtMobileNumber : EditText
    private lateinit var edtEmail : EditText
    private lateinit var rbMale : AppCompatRadioButton
    private lateinit var rbFemale : AppCompatRadioButton
    private lateinit var radioGrp : RadioGroup
    private lateinit var btnRegister : Button
    private lateinit var loading : ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        edtFullName = binding.fullName
        edtMobileNumber = binding.mobileNumber
        edtEmail = binding.email
        rbMale = binding.rbMale
        rbFemale = binding.rbFemale
        radioGrp=binding.radioGrp
        btnRegister = binding.register
        loading = binding.loading

        rbMale.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                gender = "Male"
            }
        }
        rbFemale.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                gender = "Female"
            }
        }
        registerViewModel = ViewModelProvider(this, ViewModelFactory())
            .get(RegisterViewModel::class.java)

        btnRegister.setOnClickListener {
            if(validateUser()) {
                btnRegister.isVisible = false
                loading.isVisible = true
                registerViewModel.register(
                    User(
                        fullName = edtFullName.text.toString(),
                        mobileNumber = edtMobileNumber.text.toString(),
                        email = edtEmail.text.toString(),
                        gender = gender
                    )
                )
            }

        }

        registerViewModel.registerLiveData.observe(this@RegisterActivity, {
            btnRegister.isVisible = true
            loading.isVisible = false
            if (it is Result.Success) {
                Util.showToast(this,resources.getString(R.string.register_sucesffuly))
                startLoginActivity()
            } else if (it is Result.Error) {
                it.exception.message?.let { msg -> showRegisterFailed(msg) }
            }
        })
    }

    private fun validateUser(): Boolean {
        return Util.checkBlankValidation(binding.fullName, resources.getString(R.string.enter_full_name))
                && Util.mobileValidation(binding.mobileNumber, resources)
                && Util.checkEmailValidation(binding.email, resources.getString(R.string.enter_email_address))
                && Util.checkRadioGroupValidation(radioGroup = radioGrp, resources.getString(R.string.select_gender))
    }

    private fun startLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finishAffinity()
    }

    private fun showRegisterFailed(errorString: String) {
        Util.showToast(applicationContext, errorString)
    }
}
