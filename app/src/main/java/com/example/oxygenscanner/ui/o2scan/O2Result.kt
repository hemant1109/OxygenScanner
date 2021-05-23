package com.example.oxygenscanner.ui.o2scan

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oxygenscanner.R
import com.example.oxygenscanner.ui.login.LoginActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
        val SO2 = findViewById<ImageButton>(R.id.SendO2)
        val bundle = intent.extras
        if (bundle != null) {
            O2 = bundle.getInt("O2R")
            user = bundle.getString("Usr")
            RO2.text = O2.toString()
        }
        SO2.setOnClickListener { v: View? ->
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "Health Watcher")
            i.putExtra(Intent.EXTRA_TEXT, "$user's Oxygen Saturation Level \n at $Date is :   $O2")
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@O2Result,
                    "There are no email clients installed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}