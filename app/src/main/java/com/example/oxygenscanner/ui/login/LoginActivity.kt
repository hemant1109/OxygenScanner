package com.example.oxygenscanner.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.oxygenscanner.R
import com.example.oxygenscanner.databinding.ActivityLoginBinding
import com.example.oxygenscanner.ui.register.RegisterActivity
import com.example.oxygenscanner.ui.startvitalsign.StartVitalSigns
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit
import com.example.oxygenscanner.util.Util
import com.example.oxygenscanner.util.ViewModelFactory
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class LoginActivity : AppCompatActivity() {

    private val MOBILE_NUMBER: String = "mobileNumber"
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var edtOtp: AppCompatEditText
    private lateinit var loading: ProgressBar
    private lateinit var btnSendAndVerifyOtp: AppCompatButton
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private lateinit var edtMobileNumber: EditText
    private lateinit var loginViewModel: LoginViewModel
    // variable for FirebaseAuth class
    private var mAuth: FirebaseAuth? = null

    // variable for our text input
    // field for phone and OTP.
    private lateinit var edtPhone: EditText
    private lateinit var edtOTP: EditText

    // buttons for generating OTP and verifying OTP
    private lateinit var verifyOTPBtn: Button
    private lateinit var generateOTPBtn: Button

    // string for storing our verification ID
    private var verificationId: String? = null
    private lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth
    var IS_LOGGED_IN = "is_logged_in"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        if (sharedPreferences?.getBoolean(IS_LOGGED_IN, false) == true) {
            startVitalSignActivity()
        }
        auth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance()

        MobileAds.initialize(
            this
        ) {
///haji baki chee
        }
        edtMobileNumber = binding.mobileNumber
        edtOtp = binding.otp
        btnSendAndVerifyOtp = binding.sendAndVerifyOtp
        loading = binding.loading

        loginViewModel = ViewModelProvider(this, ViewModelFactory())
            .get(LoginViewModel::class.java)

        binding.tvRegister.setOnClickListener {
            edtMobileNumber.text.clear()
            edtOtp.text?.clear()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        loginViewModel.mobileExist.observe(this, {
            if (it) {
                loginViewModel.sendOtp(this, edtMobileNumber.text.toString(), callbacks)
            } else {
                loading.visibility = View.INVISIBLE
                btnSendAndVerifyOtp.visibility = View.VISIBLE
                Util.showToast(applicationContext, "Please register this mobile number.")
            }
        })
        btnSendAndVerifyOtp.setOnClickListener {
            if (Util.mobileValidation(edtMobileNumber, resources)) {
                if (btnSendAndVerifyOtp.text == "Send Otp") {
                    loading.visibility = View.VISIBLE
                    btnSendAndVerifyOtp.visibility = View.INVISIBLE
                    loginViewModel.checkMobileExist(edtMobileNumber.text.toString())
                } else {
                    if (Util.checkBlankValidation(
                            edtOtp,
                            resources.getString(R.string.enter_otp)
                        )
                    ) {
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
                btnSendAndVerifyOtp.text = getString(R.string.verify_otp)
                edtOtp.visibility = View.VISIBLE
            } else {
                showLoginFailed("Please try again later")
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
                loading.visibility = View.GONE
                btnSendAndVerifyOtp.visibility = View.VISIBLE
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
                Util.showToast(this@LoginActivity, resources.getString(R.string.opt_send))
                loading.visibility = View.GONE
                btnSendAndVerifyOtp.text = getString(R.string.verify_otp)
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
        i.putExtra("Usr", sharedPreferences?.getString(MOBILE_NUMBER, ""))
        startActivity(i)
        finish()
    }

    private fun showLoginFailed(errorString: String) {
        Util.showToast(applicationContext, errorString)
    }
        // initializing variables for button and Edittext.
        edtPhone = binding.idEdtPhoneNumber
        edtOTP = binding.idEdtOtp
        verifyOTPBtn = binding.idBtnVerify
        generateOTPBtn = binding.btnGetOtp

        // setting onclick listner for generate OTP button.
        generateOTPBtn.setOnClickListener(View.OnClickListener {
            // below line is for checking weather the user
            // has entered his mobile number or not.
            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                // when mobile number text field is empty
                // displaying a toast message.
                Toast.makeText(
                    this@LoginActivity,
                    "Please enter a valid phone number.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // if the text field is not empty we are calling our
                // send OTP method for getting OTP from Firebase.
                val phone = "+91" + edtPhone.getText().toString()
                sendVerificationCode(phone)
            }
        })

        // initializing on click listener
        // for verify otp button
        verifyOTPBtn.setOnClickListener(View.OnClickListener {
            // validating if the OTP text field is empty or not.
            if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                // if the OTP text field is empty display
                // a message to user to enter OTP
                Toast.makeText(this@LoginActivity, "Please enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                // if OTP field is not empty calling
                // method to verify the OTP.
                verifyCode(edtOTP.getText().toString())
            }
        })
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Util.showToast(applicationContext, "Welcome")
                    ///val user = task.result?.user
                    sharedPreferences?.edit()?.apply {
                        putBoolean(IS_LOGGED_IN, binding.cbRememberMe.isChecked)
                        putString(MOBILE_NUMBER, edtMobileNumber.text.toString())
                        commit()
                    }
                    edtMobileNumber.text.clear()
                    edtOtp.text?.clear()
                    startVitalSignActivity()
                } else {
                    loading.visibility = View.GONE
                    btnSendAndVerifyOtp.visibility = View.VISIBLE
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

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // if the code is correct and the task is successful
                    // we are sending our user to new activity.
                    val i = Intent(this@LoginActivity, StartVitalSigns::class.java)
                    i.putExtra("Usr", binding.idEdtPhoneNumber.text.toString())
                    startActivity(i)
                    finish()
                } else {
                    // if the code is not correct then we are
                    // displaying an error message to the user.
                    Toast.makeText(this@LoginActivity, task.exception!!.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}

    private fun sendVerificationCode(number: String) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,  // first parameter is user's mobile number
            60,  // second parameter is time limit for OTP
            // verification which is 60 seconds in our case.
            TimeUnit.SECONDS,  // third parameter is for initializing units
            // for time period which is in seconds in our case.
            TaskExecutors.MAIN_THREAD,  // this task will be excuted on Main thread.
            mCallBack // we are calling callback method when we recieve OTP for
            // auto verification of user.
        )
    }

    // callback method is called on Phone auth provider.
    private val   // initializing our callbacks for on
    // verification callback method.
            mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            // below method is used when
            // OTP is sent from Firebase
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                // when we receive the OTP it
                // contains a unique id which
                // we are storing in our string
                // which we have already created.
                verificationId = s
            }

            // this method is called when user
            // receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                // below line is used for getting OTP code
                // which is sent in phone auth credentials.
                val code = phoneAuthCredential.smsCode

                // checking if the code
                // is null or not.
                if (code != null) {
                    // if the code is not null then
                    // we are setting that code to
                    // our OTP edittext field.
                    edtOTP!!.setText(code)

                    // after setting this code
                    // to OTP edittext field we
                    // are calling our verifycode method.
                    verifyCode(code)
                }
            }

            // this method is called when firebase doesn't
            // sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                // displaying error message with firebase exception.
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

    // below method is use to verify code from Firebase.
    private fun verifyCode(code: String) {
        // below line is used for getting getting
        // credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential)
    }
}