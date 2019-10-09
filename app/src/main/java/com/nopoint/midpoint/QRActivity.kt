package com.nopoint.midpoint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.nopoint.midpoint.helpers.EncryptionHelper
import com.nopoint.midpoint.helpers.QRCodeHelper
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.models.QRScanObject
import com.nopoint.midpoint.models.User
import kotlinx.android.synthetic.main.activity_qr.*

class QRActivity : AppCompatActivity() {

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        user = CurrentUser.getCurrentUser(this)
        Log.d("QR", "current user: $user")

        val qrToken = generateQRToken()
        Log.d("QR", "qr token: $qrToken")



        generateQRCode()

        qr_button_scan.setOnClickListener {
            val intent = Intent(this, QRScanActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    private fun generateQRCode() {
        val qrToken = generateQRToken()
        val username = user!!.username

        val asd = QRScanObject(qrToken, username)

        val serializedString = Gson().toJson(asd)
        val encryptedString = EncryptionHelper.instance.encryptionString(serializedString)!!.encryptMsg()

        val bitmap = QRCodeHelper
            .newInstance(this)
            .setContent(encryptedString)
            .setErrorCorrenctionLevel(ErrorCorrectionLevel.Q)
            .setMargin(2)
            .getQRCOde()

        qr_image_view_code.setImageBitmap(bitmap)

    }

    private fun generateQRToken(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz123456789?!&/()}{?"
        return (1..64)
            .map { allowedChars.random() }
            .joinToString("")
    }


}