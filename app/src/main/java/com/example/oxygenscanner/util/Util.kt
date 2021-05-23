package com.example.oxygenscanner.util

import android.util.Log
import com.example.oxygenscanner.BuildConfig

object Util {
    fun logD(tag: String = BuildConfig.APPLICATION_ID, msg: String, tr: Throwable? = null) {
        if (tr != null)
            Log.d(tag.substring(0, 22), msg, tr)
        else
            Log.d(tag.substring(0, 22), msg)
    }
}