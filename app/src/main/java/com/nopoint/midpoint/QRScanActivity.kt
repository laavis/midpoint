package com.nopoint.midpoint

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var qrScanner: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = SharedPref(this)

        if (sharedPref.loadNightModeState() == true) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_qr_scan)
        qrScanner = findViewById(R.id.qr_code_scanner)
        setScannerProperties()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                return
            }
        }

        qrScanner.startCamera()
        qrScanner.setResultHandler(this)
    }

    private fun setScannerProperties() {
        qrScanner.setFormats(listOf(BarcodeFormat.QR_CODE))
        qrScanner.setAutoFocus(true)
        qrScanner.setBorderStrokeWidth(10)
        qrScanner.setBorderCornerRadius(30)
        qrScanner.setBorderLineLength(150)
        qrScanner.setSquareViewFinder(true)
        if (Build.MANUFACTURER.equals(HUAWEI, ignoreCase = true))
            qrScanner.setAspectTolerance(0.5f)

    }


    private fun openCamera() {
        qrScanner.startCamera()
        qrScanner.setResultHandler(this)
    }

    private fun resumeCamera() {

    }

    private fun showCameraSnackbar() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "TWAT GIVE PERMISSION TO CAMERA", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openCamera()
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                showCameraSnackbar()
        }
    }

    override fun handleResult(p0: Result?) {
        if (p0 != null) {
            Log.d("QR", "scan success")
            startActivity(QRScannedActivity.getScannedActivity(this, p0.text))
            resumeCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        qrScanner.stopCamera()
    }

    companion object {
        private const val HUAWEI = "huawei"
        private const val CAMERA_REQUEST_CODE = 666
        fun getScannedActivity(callerClassContext: Context) = Intent(callerClassContext, QRScanActivity::class.java)
    }
}