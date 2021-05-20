package com.example.oxygenscanner.ui.startvitalsign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.oxygenscanner.R
import android.widget.ImageButton
import android.content.Intent
import android.view.View
import com.example.oxygenscanner.ui.login.LoginActivity
import com.example.oxygenscanner.ui.o2scan.O2Process

class StartVitalSigns : AppCompatActivity() {
    private var user: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_vital_signs)
        val extras = intent.extras
        if (extras != null) {
            user = extras.getString("Usr")
        }
        val VS = findViewById<ImageButton>(R.id.StartVS)
        VS.setOnClickListener { v: View ->
            startO2ScanActivity()
        }
    }
    private fun startO2ScanActivity() {
        val i = Intent(this, O2Process::class.java)
        i.putExtra("Usr",user)
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        val i = Intent(this@StartVitalSigns, LoginActivity::class.java)
        startActivity(i)
        finish()
        super.onBackPressed()
    }
}