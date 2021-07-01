package com.app.oxygenscanner.ui.login

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.oxygenscanner.data.LoginRepository
import com.app.oxygenscanner.util.FireStoreDB
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {


    private val _otpSend = MutableLiveData<Boolean>()
    val otpSend: LiveData<Boolean> = _otpSend
    fun sendOtp(
        activity: AppCompatActivity,
        mobileNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber("+91$mobileNumber")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    fun verifyOtp(activity: AppCompatActivity, mobileNumber: String, otp: String) {
        val options = PhoneAuthOptions.newBuilder()
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val _mobileExist = MutableLiveData<Boolean>()
    val mobileExist: LiveData<Boolean> = _mobileExist

    fun checkMobileExist(mobileNumber: String) {
        viewModelScope.launch {
            try {
                _mobileExist.postValue(FireStoreDB.checkMobileExist(mobileNumber))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}