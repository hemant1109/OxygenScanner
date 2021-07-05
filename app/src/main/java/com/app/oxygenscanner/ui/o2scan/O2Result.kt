package com.app.oxygenscanner.ui.o2scan

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.app.oxygenscanner.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

const val AD_UNIT_ID = "ca-app-pub-1491257039649212/3450129307"
const val ADS_LENGTH_MILLISECONDS = 3000L
/*
* service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if
          request.time < timestamp.date(2021, 7, 9);
    }
  }
}
* */

class O2Result : AppCompatActivity() {
    private var user: String? = null
    private var Date: String? = null
    var df: DateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
    var today = Calendar.getInstance().time
    var O2 = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_o2_result)
        Date = df.format(today)
        val RO2 = findViewById<TextView>(R.id.O2R)
        val bundle = intent.extras
        if (bundle != null) {
            O2 = bundle.getInt("O2R")
            user = bundle.getString("Usr")
            RO2.text = O2.toString()
        }
        startAds()
    }

    private var mAdIsLoading: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private var mAdsIsInProgress = false
    private var mTimerMilliseconds = 0L
    private var mCountDownTimer: CountDownTimer? = null

    private fun startAds() {
        if (!mAdIsLoading && mInterstitialAd == null) {
            mAdIsLoading = true
            loadAd()
        }
        resumeAds(ADS_LENGTH_MILLISECONDS)
    }

    private fun resumeAds(milliseconds: Long) {
        // Create a new timer for the correct length and start it.
        mAdsIsInProgress = true
        mTimerMilliseconds = milliseconds
        createTimer(milliseconds)
        mCountDownTimer?.start()
    }

    private fun createTimer(milliseconds: Long) {
        mCountDownTimer?.cancel()

        mCountDownTimer = object : CountDownTimer(milliseconds, 50) {
            override fun onTick(millisUntilFinished: Long) {
                mTimerMilliseconds = millisUntilFinished
                //timer.text = "seconds remaining: ${ millisUntilFinished / 1000 + 1 }"
            }

            override fun onFinish() {
                mAdsIsInProgress = false
                //timer.text = "done!"
                //retry_button.visibility = View.VISIBLE
            }
        }
    }

    // Resume the ads if it's in progress.
    public override fun onResume() {
        super.onResume()
        if (mAdsIsInProgress) {
            resumeAds(mTimerMilliseconds)
        }
    }

    // Cancel the timer if the ads is paused.
    public override fun onPause() {
        mCountDownTimer?.cancel()
        super.onPause()
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("onAdFailedToLoad", adError.message)
                mInterstitialAd = null
                mAdIsLoading = false
                val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                        "message: ${adError.message}"
                Toast.makeText(
                    this@O2Result,
                    "onAdFailedToLoad() with error $error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("onAdLoaded", "Ad was loaded.")
                mInterstitialAd = interstitialAd
                mAdIsLoading = false
                showInterstitial()
            }
        }
        )
    }

    // Show the ad if it's ready. Otherwise toast and restart the ads.
    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("onAdDismissedFullScreen", "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    //loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d("onAdFaildToShowFullScrn", "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("onAdShowedFullScreen", "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(this)
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
            startAds()
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        val i = Intent(this@O2Result, O2Process::class.java)
        i.putExtra("O2R", O2)
        i.putExtra("Usr", user)
        startActivity(i)
        finish()
    }
}