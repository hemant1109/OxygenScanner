package com.example.oxygenscanner.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.oxygenscanner.R
import com.example.oxygenscanner.databinding.ActivityLoginBinding
import com.example.oxygenscanner.ui.register.RegisterActivity
import com.example.oxygenscanner.ui.startvitalsign.StartVitalSigns
import com.example.oxygenscanner.util.Util
import com.example.oxygenscanner.util.ViewModelFactory
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class LoginActivity : AppCompatActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private lateinit var edtMobileNumber: EditText
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edtMobileNumber = binding.mobileNumber
        val edtOtp = binding.otp
        val btnSendAndVerifyOtp = binding.sendAndVerifyOtp
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, ViewModelFactory())
            .get(LoginViewModel::class.java)

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnSendAndVerifyOtp.setOnClickListener {
            if (Util.mobileValidation(edtMobileNumber, resources)) {
                if (btnSendAndVerifyOtp.text == "Send Otp") {
                    loading.visibility = View.VISIBLE
                    btnSendAndVerifyOtp.visibility = View.INVISIBLE
                    loginViewModel.sendOtp(this, edtMobileNumber.text.toString(), callbacks)
                } else {
                    if (Util.checkBlankValidation(edtOtp, resources.getString(R.string.enter_otp))) {
                        loading.visibility = View.VISIBLE
                        btnSendAndVerifyOtp.visibility = View.INVISIBLE
                        val credential =
                            PhoneAuthProvider.getCredential(
                                storedVerificationId,
                                edtOtp.text.toString()
                            )
                        signInWithPhoneAuthCredential(credential)
                        ///loginViewModel.verifyOtp( this@LoginActivity,edtMobileNumber.text.toString(),edtOtp.text.toString())
                    }

                }
            }

        }

        loginViewModel.otpSend.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult) {
                btnSendAndVerifyOtp.text = "Verify Otp"
                edtOtp.visibility = View.VISIBLE
            } else {
                showLoginFailed("Please try agian later")
            }
            loading.visibility = View.GONE
            btnSendAndVerifyOtp.visibility = View.VISIBLE

        })

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                loading.visibility = View.GONE
                btnSendAndVerifyOtp.visibility = View.VISIBLE

            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                Util.showToast(this@LoginActivity,resources.getString(R.string.opt_send))
                loading.visibility = View.GONE
                btnSendAndVerifyOtp.text = "Verify Otp"
                btnSendAndVerifyOtp.visibility = View.VISIBLE
                edtOtp.visibility = View.VISIBLE
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }


    }

    private fun startVitalSignActivity() {
        val i = Intent(this, StartVitalSigns::class.java)
        i.putExtra("Usr", edtMobileNumber.text.toString())
        startActivity(i)
        finish()
    }

    private fun showLoginFailed(errorString: String) {
        Util.showToast(applicationContext, errorString)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Util.showToast(applicationContext, "Welcome")
                    ///val user = task.result?.user
                    startVitalSignActivity()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                            Util.showToast(applicationContext, "Code is invalid")
                    }
                    // Update UI
                }
            }
    }

}
