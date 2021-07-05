package com.app.oxygenscanner.util

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatRadioButton
import com.app.oxygenscanner.BuildConfig
import com.app.oxygenscanner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Util {
    fun logD(tag: String = BuildConfig.APPLICATION_ID, msg: String, tr: Throwable? = null) {
        if (tr != null)
            Log.d(tag, msg, tr)
        else
            Log.d(tag, msg)
    }

    fun checkBlankValidation(editText: EditText, str: String): Boolean {
        if (editText.text?.trim().isNullOrEmpty()) {
            showToast(editText.context, str)
            return false
        }
        return true
    }

    fun checkBlankValidationStr(string: String,radioButton: AppCompatRadioButton, str: String): Boolean {
        if (string.trim().isEmpty()) {
            showToast(radioButton.context, str)
            return false
        }
        return true
    }

    fun mobileValidation(
        editPhoneNumber: EditText,
        resources: Resources,
    ): Boolean {
        if (checkBlankValidation(
                editPhoneNumber, resources.getString(R.string.enter_mobile_number)            )
        ) {
                return checkIndiaMobileNumberValidation(
                    editPhoneNumber,
                    resources.getString(R.string.enter_valid_phone_number))
        }
        return false
    }

    private fun checkIndiaMobileNumberValidation(editText: EditText, messageStr: String): Boolean {
        editText.text?.trim()?.length?.let {
            return if (it < 10) {
                showToast(editText.context, messageStr)
                false
            } else {
                editText.error = null
                true
            }

        }

        return false
    }

    fun checkRadioGroupValidation(
        radioGroup: RadioGroup,
        messageStr: String,
    ): Boolean {
        if (radioGroup.checkedRadioButtonId == -1) {
            showToast(radioGroup.context, messageStr)
            ///Snackbar.make(radioGroup, messageStr, Snackbar.LENGTH_SHORT).setAction("Action", null).show()

            return false
        }
        return true
    }
    fun checkEmailValidation(editText: EditText, error: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(editText.text.trim().toString()).matches()) {
            showToast(editText.context, error)
            return false
        }
        editText.error = null
        return true
    }


    fun showToast(context: Context, text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}