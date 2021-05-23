package com.example.oxygenscanner.ui.startvitalsign

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.oxygenscanner.R
import android.widget.ImageButton
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oxygenscanner.ui.login.LoginActivity
import com.example.oxygenscanner.ui.o2scan.O2Process

class StartVitalSigns : AppCompatActivity() {
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
        val VS = findViewById<ImageButton>(R.id.StartVS)
        VS.setOnClickListener { v: View ->
            startO2ScanActivity()
        }
    }

    private fun startO2ScanActivity() {
        val i = Intent(this, O2Process::class.java)
        i.putExtra("Usr", user)
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

}