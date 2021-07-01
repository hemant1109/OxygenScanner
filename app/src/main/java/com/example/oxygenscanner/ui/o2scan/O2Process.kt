package com.example.oxygenscanner.ui.o2scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oxygenscanner.databinding.ActivityO2ProcessBinding
import com.example.oxygenscanner.util.Util
import com.example.oxygenscanner.util.imageprocessing.ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg
import com.example.oxygenscanner.util.math.Fft
import com.google.android.gms.ads.AdRequest
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil
import kotlin.math.sqrt


class O2Process : Activity() {

    private var totalTimeInSecs: Double = 0.0

    //Toast
    private var mainToast: Toast? = null

    // DataBase
    var user: String? = null

    //ProgressBar
    var ProgP = 0
    var inc = 0
    private var SamplingFreq = 0.0
    var Stdr = 0.0
    var Stdb = 0.0
    var sumred = 0.0
    var sumblue = 0.0
    var o2 = 0
    private val secondsToScanning = 30

    //Arraylist
    var RedAvgList = ArrayList<Double>()
    var BlueAvgList = ArrayList<Double>()
    var frameCounter = 0
    lateinit var binding: ActivityO2ProcessBinding

    @SuppressLint("InvalidWakeLockTag")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityO2ProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        if (extras != null) {
            user = extras.getString("Usr")
            //The key argument here must match that used in the other activity
        }
        // XML - Java Connecting
        Util.logD(msg = "O2Process previewHolder addCallback")
        previewHolder = binding.preview.holder
        previewHolder?.setKeepScreenOn(true)
        previewHolder?.addCallback(surfaceCallback)
        previewHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        binding.O2PB.max = secondsToScanning

        binding.O2PB.progress = 0
        //binding.txtCountDown.text = ""
        // WakeLock Initialization : Forces the phone to stay On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen")
        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start your camera
                Util.logD(msg = "O2Process onRequestPermissionsResult camera start")
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                camera?.setDisplayOrientation(90)
                startTime = System.currentTimeMillis()
            }
        }
    }

    //getting frames data from the camera and start the heartbeat process
    private val previewCallback = PreviewCallback { data, cam ->
        /**
         * {@inheritDoc}
         */
        /**
         * {@inheritDoc}
         */

        //if data or size == null ****
        if (data == null) throw NullPointerException()
        val size = cam.parameters.previewSize ?: throw NullPointerException()

        //Atomically sets the value to the given updated value if the current value == the expected value.
        if (!processing.compareAndSet(false, true)) return@PreviewCallback

        //put width + height of the camera inside the variables
        val width = size.width
        val height = size.height
        val RedAvg: Double = decodeYUV420SPtoRedBlueGreenAvg(
            data.clone(),
            height,
            width,
            1
        ) //1 stands for red intensity, 2 for blue, 3 for green
        sumred += RedAvg
        val BlueAvg: Double = decodeYUV420SPtoRedBlueGreenAvg(
            data.clone(),
            height,
            width,
            2
        ) //1 stands for red intensity, 2 for blue, 3 for green
        sumblue += BlueAvg
        RedAvgList.add(RedAvg)
        BlueAvgList.add(BlueAvg)
        ++frameCounter //counts number of frames in 30 seconds

        //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
        if (RedAvg < 200) {
            inc = 0
            ProgP = inc
            updateProgress(totalTimeInSecs)
            processing.set(false)
        }
        val endTime = System.currentTimeMillis()
        totalTimeInSecs = (endTime - startTime) / 1000.0 //to convert time to seconds
        updateProgress(totalTimeInSecs)
        if (totalTimeInSecs >= secondsToScanning) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
            startTime = System.currentTimeMillis()
            SamplingFreq = frameCounter / totalTimeInSecs
            val Red = RedAvgList.toTypedArray()
            val Blue = BlueAvgList.toTypedArray()
            val HRFreq = Fft.fFT(Red, frameCounter, SamplingFreq)
            val bpm: Double = ceil(HRFreq * 60)
            val meanr = sumred / frameCounter
            val meanb = sumblue / frameCounter
            for (i in 0 until frameCounter - 1) {
                val bufferb = Blue[i]
                Stdb += (bufferb - meanb) * (bufferb - meanb)
                val bufferr = Red[i]
                Stdr += (bufferr - meanr) * (bufferr - meanr)
            }
            val varr = sqrt(Stdr / (frameCounter - 1))
            val varb = sqrt(Stdb / (frameCounter - 1))
            val R = varr / meanr / (varb / meanb)
            val spo2 = 100 - 5 * R
            o2 = spo2.toInt()
            Util.logD(msg = "O2Process bpm ${bpm.toInt()}")
            Util.logD(msg = "O2Process o2 $o2")
            if (o2 < 80 || o2 > 99 /*|| bpm < 45 || bpm > 200*/) {
                inc = 0
                ProgP = inc
                updateProgress(totalTimeInSecs)
                Util.logD(msg = "O2Process Measurement Failed")
                mainToast =
                    Toast.makeText(applicationContext, "Measurement Failed", Toast.LENGTH_SHORT)
                mainToast?.show()
                startTime = System.currentTimeMillis()
                frameCounter = 0
                processing.set(false)
                return@PreviewCallback
            }
        }
        if (o2 != 0) {
            val i = Intent(this@O2Process, O2Result::class.java)
            i.putExtra("O2R", o2)
            i.putExtra("Usr", user)
            startActivity(i)
            finish()
        }
        if (RedAvg != 0.0) {
            ProgP = inc++ / 34
            updateProgress(totalTimeInSecs)
        }
        processing.set(false)
    }

    private fun updateProgress(totalTimeInSecs: Double) {
        binding.O2PB.progress = ProgP/*(secondsToScanning - totalTimeInSecs.toInt())*/
        //binding.txtCountDown.text = (secondsToScanning - totalTimeInSecs.toInt()).toString()
    }

    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                Util.logD(msg = "O2Process surfaceCallback surfaceCreated")
                camera?.setPreviewDisplay(previewHolder)
                camera?.setPreviewCallback(previewCallback)
                Util.logD(msg = "O2Process setPreviewDisplay")
            } catch (t: Throwable) {
                Log.e("PreviewDemoSurfcCallbck", "O2Process Exception in setPreviewDisplay()", t)
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Util.logD(msg = "O2Process surfaceCallback surfaceChanged")

            camera?.parameters?.let { parameters ->
                parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                /*val size = getSmallestPreviewSize(width, height, parameters)
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height)
                    Log.d(TAG, "Using width=" + size.width + " height=" + size.height)
                }*/
                camera?.parameters = parameters
                val allSizes: MutableList<Camera.Size>? = parameters.getSupportedPictureSizes()
                var size: Camera.Size? = allSizes?.get(0) // get top size

                for (i in allSizes?.indices!!) {
                    if (allSizes[i].width > size?.width!!) size = allSizes[i]
                }
                //set max Picture Size
                size?.width?.let { parameters.setPictureSize(it, size.height) }
                camera?.startPreview()
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Ignore
            Util.logD(msg = "O2Process surfaceDestroyed called")
        }
    }

    companion object {
        // Variables Initialization
        private const val TAG = "HeartRateMonitor"
        private val processing = AtomicBoolean(false)
        private const val REQUEST_READ_PHONE_STATE = 1
        private const val REQUEST_CAMERA = 2
        private var previewHolder: SurfaceHolder? = null
        private var camera: Camera? = null
        var wakeLock: WakeLock? = null

        //Freq + timer variable
        private var startTime: Long = 0

        // SPO2 variables
        private const val RedBlueRatio = 0.0
        private fun getSmallestPreviewSize(
            width: Int,
            height: Int,
            parameters: Camera.Parameters
        ): Camera.Size? {
            var result: Camera.Size? = null
            for (size in parameters.supportedPreviewSizes) {
                if (size.width <= width && size.height <= height) {
                    if (result == null) {
                        result = size
                    } else {
                        val resultArea = result.width * result.height
                        val newArea = size.width * size.height
                        if (newArea < resultArea) result = size
                    }
                }
            }
            return result
        }
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    public override fun onResume() {
        super.onResume()
        binding.adView.resume()
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        } else {
            wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
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
            ) else {
            //start your camera
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            camera?.setDisplayOrientation(90)
            startTime = System.currentTimeMillis()
        }
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    public override fun onPause() {
        binding.adView.pause()
        super.onPause()
        Util.logD(msg = "O2Process onPause called")
        if (wakeLock!!.isHeld)
            wakeLock!!.release()
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
        Util.logD(msg = "O2Process camera null")
    }
}