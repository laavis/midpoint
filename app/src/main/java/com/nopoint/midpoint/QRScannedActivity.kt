package com.nopoint.midpoint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nopoint.midpoint.helpers.EncryptionHelper
import com.nopoint.midpoint.models.QRScanObject
import kotlinx.android.synthetic.main.activity_qr_scanned.*

class QRScannedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanned)

        if (intent.getSerializableExtra(SCANNED_STRING) == null) {
            throw RuntimeException("No encrypted String found in intent")
        }

        val decryptedString = EncryptionHelper.instance.getDecryptionString(intent.getStringExtra(SCANNED_STRING))
        Log.d("QR", "decrypted: $decryptedString")

        val qrScanObject = Gson().fromJson(decryptedString, QRScanObject::class.java)

        qr_scanned_username.text = qrScanObject.username
    }

    companion object {
        private const val SCANNED_STRING = "scanned_string"
        fun getScannedActivity(callerClassContext: Context, encryptedString: String): Intent {
            return Intent(callerClassContext, QRScannedActivity::class.java).putExtra(SCANNED_STRING, encryptedString)
        }
    }
}