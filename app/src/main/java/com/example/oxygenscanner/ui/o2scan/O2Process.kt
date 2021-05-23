package com.example.oxygenscanner.ui.o2scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.android.camera.utils.AutoFitSurfaceView
import com.example.android.camera.utils.getPreviewOutputSize
import com.example.oxygenscanner.databinding.ActivityO2ProcessBinding
import com.example.oxygenscanner.ui.login.LoginActivity
import com.example.oxygenscanner.util.imageprocessing.ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg
import com.example.oxygenscanner.util.math.Fft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.ceil
import kotlin.math.sqrt


class O2Process : AppCompatActivity() {

    private lateinit var timer: CountDownTimer
    private var cameraId: String? = null
    private val cameraManager: CameraManager by lazy {
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private val characteristics: CameraCharacteristics by lazy {
        cameraId = cameraManager.cameraIdList[0]
        cameraManager.getCameraCharacteristics(cameraId!!)
    }

    //Toast
    private var mainToast: Toast? = null

    // DataBase
    var user: String? = null

    //ProgressBar
    private var ProgP = 0
    var inc = 0
    private var SamplingFreq = 0.0
    private var Stdr = 0.0
    private var Stdb = 0.0
    var sumred = 0.0
    var sumblue = 0.0
    var o2 = 0

    //Arraylist
    private var RedAvgList = ArrayList<Double>()
    private var BlueAvgList = ArrayList<Double>()
    var counter = 0
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
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtCountDown.text = (millisUntilFinished).div(1000).toString()
                Log.w("O2Result","O2Result sec ${binding.txtCountDown.text}")
            }

            override fun onFinish() {

            }
        }
        // XML - Java Connecting
        previewHolder = binding.preview
        previewHolder?.holder?.addCallback(surfaceCallback)
        binding.O2PB.progress = 0

        // WakeLock Initialization : Forces the phone to stay On
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen")
    }

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    public override fun onResume() {
        super.onResume()
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
            //openCamera()
        }
    }

    private fun openCamera() {
        lifecycleScope.launch {
            cameraDevice = openCamera(cameraManager, cameraId!!, cameraHandler)
            //camera?.setDisplayOrientation(90)
            startTime = System.currentTimeMillis()
        }
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    public override fun onPause() {
        super.onPause()
        if (wakeLock!!.isHeld)
            wakeLock!!.release()
//        camera.setPreviewCallback(null)
//        camera.stopPreview()
//        camera.release()
        cameraDevice = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start your camera
                // openCamera()
            }
        }
    }

    //getting frames data from the camera and start the heartbeat process
    private val previewCallback = PreviewCallback { data, cam ->

    }
    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            /*try {
                val isTorchAvailable: Boolean =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (isTorchAvailable) {
                    cameraManager.setTorchMode(cameraId!!, true) //Turn ON
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }*/
            // Selects appropriate preview size and configures view finder
            val previewSize = getPreviewOutputSize(
                previewHolder?.display!!, characteristics, SurfaceHolder::class.java
            )
            Log.d(TAG, "View finder size: ${previewHolder?.width} x ${previewHolder?.height}")
            Log.d(TAG, "Selected preview size: $previewSize")
            previewHolder?.setAspectRatio(previewSize.width, previewSize.height)

            // To ensure that size is set, initialize camera in the view's thread
            previewHolder?.post { initializeCamera() }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) =
            Unit

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Ignore
        }
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        var image: Image? = null
        try {
            Log.w("mOnImageAvailableLstnr", "mOnImageAvailableListener called")
            image = reader.acquireLatestImage()
            if (image != null) {
                Log.w("mOnImageAvailableLstnr", "mOnImageAvailableListener image")
                val buffer: ByteBuffer = image.planes[0].buffer
                /*val bitmap: Bitmap = fromByteBuffer(buffer)*/
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                /**
                 * {@inheritDoc}
                 */
                /**
                 * {@inheritDoc}
                 */

                //if data or size == null ****
                previewHolder?.width ?: throw NullPointerException()
                previewHolder?.height ?: throw NullPointerException()

                //Atomically sets the value to the given updated value if the current value == the expected value.
                if (!processing.compareAndSet(false, true)) return@OnImageAvailableListener

                //put width + height of the camera inside the variables
                val width = previewHolder?.width!!
                val height = previewHolder?.height!!
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
                ++counter //countes number of frames in 30 seconds

                //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
                if (RedAvg < 200) {
                    inc = 0
                    ProgP = inc
                    updateProgress(ProgP)
                    processing.set(false)
                }
                val endTime = System.currentTimeMillis()
                val totalTimeInSecs = (endTime - startTime) / 1000.0 //to convert time to seconds
                if (totalTimeInSecs >= 30) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
                    startTime = System.currentTimeMillis()
                    SamplingFreq = counter / totalTimeInSecs
                    val Red = RedAvgList.toTypedArray()
                    val Blue = BlueAvgList.toTypedArray()
                    val HRFreq = Fft.fFT(Red, counter, SamplingFreq)
                    val bpm: Double = ceil(HRFreq * 60)
                    val meanr = sumred / counter
                    val meanb = sumblue / counter
                    for (i in 0 until counter - 1) {
                        val bufferb = Blue[i]
                        Stdb += (bufferb - meanb) * (bufferb - meanb)
                        val bufferr = Red[i]
                        Stdr += (bufferr - meanr) * (bufferr - meanr)
                    }
                    val varr = sqrt(Stdr / (counter - 1))
                    val varb = sqrt(Stdb / (counter - 1))
                    val R = varr / meanr / (varb / meanb)
                    val spo2 = 100 - 5 * R
                    o2 = spo2.toInt()
                    if (o2 < 80 || o2 > 99 || bpm < 45 || bpm > 200) {
                        inc = 0
                        ProgP = inc
                        updateProgress(ProgP)
                        mainToast =
                            Toast.makeText(
                                applicationContext,
                                "Measurement Failed",
                                Toast.LENGTH_SHORT
                            )
                        mainToast?.show()
                        startTime = System.currentTimeMillis()
                        counter = 0
                        processing.set(false)
                        return@OnImageAvailableListener
                    }
                }
                if (o2 != 0) {
                    Log.w("O2Result","O2Result start called")
                    val i = Intent(this@O2Process, O2Result::class.java)
                    i.putExtra("O2R", o2)
                    i.putExtra("Usr", user)
                    startActivity(i)
                    finish()
                    image.close()
                    reader?.close()
                }
                if (RedAvg != 0.0) {
                    ProgP = inc++ / 34
                    updateProgress(ProgP)
                }
                processing.set(false)
                image.close()
            }
        } catch (e: Exception) {
            image?.close()
            reader?.close()
            e.printStackTrace()
            e.message?.let { Log.w("mOnImageAvailableLstnr", it) }
        }
    }

    private fun updateProgress(progress: Int) {
        lifecycleScope.launch {
            binding.O2PB.progress = progress
            //binding.txtCountDown.text = progress.toString()
        }
    }

    /**
     * Begin all camera operations in a coroutine in the main thread. This function:
     * - Opens the camera
     * - Configures the camera session
     * - Starts the preview by dispatching a repeating capture request
     * - Sets up the still image capture listeners
     */
    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        // Open the selected camera
        cameraDevice = openCamera(cameraManager, cameraId!!, cameraHandler)
        timer.start()
        // Initialize an image reader which will be used to capture still photos
        val size = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!.getOutputSizes(pixelFormat).maxByOrNull { it.height * it.width }!!
        imageReader = ImageReader.newInstance(
            size.width, size.height, pixelFormat, IMAGE_BUFFER_SIZE
        )
        imageReader.setOnImageAvailableListener(mOnImageAvailableListener, imageReaderHandler)
        // Creates list of Surfaces where the camera will output frames
        val targets = listOf(previewHolder?.holder?.surface, imageReader.surface)

        // Start a capture session using our open camera and list of Surfaces where frames will go
        session = createCaptureSession(cameraDevice!!, targets, cameraHandler)

        val captureRequest = cameraDevice?.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        )?.apply {
            targets.forEach {
                addTarget(it!!)
            }
            try {
                val isTorchAvailable: Boolean =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (isTorchAvailable) {
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                    set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        // This will keep sending the capture request as frequently as possible until the
        // session is torn down or session.stopRepeating() is called
        captureRequest?.build()?.let { session.setRepeatingRequest(it, null, cameraHandler) }

        // Listen to the capture button
//        capture_button.setOnClickListener {
//
//            // Disable click listener to prevent multiple requests simultaneously in flight
//            it.isEnabled = false
//
//            // Perform I/O heavy operations in a different scope
//            lifecycleScope.launch(Dispatchers.IO) {
//                takePhoto().use { result ->
//                    Log.d(TAG, "Result received: $result")
//
//                    // Save the result to disk
//                    val output = saveResult(result)
//                    Log.d(TAG, "Image saved: ${output.absolutePath}")
//
//                    // If the result is a JPEG file, update EXIF metadata with orientation info
//                    if (output.extension == "jpg") {
//                        val exif = ExifInterface(output.absolutePath)
//                        exif.setAttribute(
//                            ExifInterface.TAG_ORIENTATION, result.orientation.toString()
//                        )
//                        exif.saveAttributes()
//                        Log.d(TAG, "EXIF metadata saved: ${output.absolutePath}")
//                    }
//
//                    // Display the photo taken to user
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        navController.navigate(
//                            CameraFragmentDirections
//                                .actionCameraToJpegViewer(output.absolutePath)
//                                .setOrientation(result.orientation)
//                                .setDepth(
//                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
//                                            result.format == ImageFormat.DEPTH_JPEG
//                                )
//                        )
//                    }
//                }
//
//                // Re-enable click listener after photo is taken
//                it.post { it.isEnabled = true }
//            }
//        }
    }

    override fun onStop() {
        super.onStop()
        try {
            cameraDevice?.close()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }

    /** Opens the camera and returns the opened device (as the result of the suspend coroutine) */
    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
                finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface?>,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->

        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    companion object {
        // Variables Initialization
        private const val TAG = "HeartRateMonitor"
        private val processing = AtomicBoolean(false)
        private const val REQUEST_READ_PHONE_STATE = 1
        private const val REQUEST_CAMERA = 2
        private var previewHolder: AutoFitSurfaceView? = null
        var wakeLock: WakeLock? = null
        private var cameraDevice: CameraDevice? = null

        /** [HandlerThread] where all camera operations run */
        private val cameraThread = HandlerThread("CameraThread").apply { start() }

        /** [Handler] corresponding to [cameraThread] */
        private val cameraHandler = Handler(cameraThread.looper)

        //Freq + timer variable
        private var startTime: Long = 0

        /** Readers used as buffers for camera still shots */
        private lateinit var imageReader: ImageReader

        /** Maximum number of images that will be held in the reader's buffer */
        private const val IMAGE_BUFFER_SIZE: Int = 3

        /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
        private lateinit var session: CameraCaptureSession

        val pixelFormat = ImageFormat.YUV_420_888

        /** [HandlerThread] where all buffer reading operations run */
        private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

        /** [Handler] corresponding to [imageReaderThread] */
        private val imageReaderHandler = Handler(imageReaderThread.looper)

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
}