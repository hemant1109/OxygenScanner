package com.app.oxygenscanner.ui.startvitalsign

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.oxygenscanner.R
import com.app.oxygenscanner.ui.login.LoginActivity
import com.app.oxygenscanner.ui.o2scan.O2Process
import com.google.android.gms.ads.*


class StartVitalSigns : AppCompatActivity() {
    private var adView: AdView? = null
    private var user: String? = null
    private val REQUEST_CAMERA = 2
    private val REQUEST_READ_PHONE_STATE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_vital_signs)
        val extras = intent.extras
        if (extras != null) {
            user = extras.getString("Usr")
        }

        //set permission first
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        }
        val VS = findViewById<AppCompatButton>(R.id.StartVS)
        VS.setOnClickListener { v: View ->
            startO2ScanActivity()
        }
        adView = findViewById(R.id.adView)
        val adRequest: AdRequest = AdRequest.Builder().build()
//        adView?.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                Log.d("adView","adView onAdLoaded")
//            }
//
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                // Code to be executed when an ad request fails.
//                Log.d("adView","adView onAdFailedToLoad ${adError.message}")
//                Log.d("adView","adView onAdFailedToLoad ${adError.cause?.message}")
//                Log.d("adView","adView onAdFailedToLoad ${adError.cause?.domain}")
//            }
//
//            override fun onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//                Log.d("adView","adView onAdOpened")
//
//            }
//
//            override fun onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//                Log.d("adView","adView onAdClicked")
//            }
//
//            override fun onAdClosed() {
//                Log.d("adView","adView onAdClosed")
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//            }
//        }
        adView?.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_logout -> {
                getSharedPreferences(packageName, MODE_PRIVATE).apply {
                    edit().clear().commit()
                }
                startLoginActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startO2ScanActivity() {
        val i = Intent(this, O2Process::class.java)
        i.putExtra("Usr", user)
        startActivity(i)
        finish()
    }

    private fun startLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            //activity
            //startO2ScanActivity()
            //If authorisation not granted for camera
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) //ask for authorisation
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            //end

        } else if (requestCode == REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //activity
                startO2ScanActivity()
            }
        }
    }
    // Called when leaving the activity
    public override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    // Called when returning to the activity
    public override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

}