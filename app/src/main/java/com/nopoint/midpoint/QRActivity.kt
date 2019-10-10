package com.nopoint.midpoint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.nopoint.midpoint.helpers.QRCodeHelper
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.models.FriendToken
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.QR_CREATE
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.activity_qr.*
import java.lang.Exception

class QRActivity : AppCompatActivity() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var authToken: String
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        authToken = CurrentUser.getLocalUser(this)!!.token
        createQRToken()

        qr_button_scan.setOnClickListener {
            val intent = Intent(this, QRScanActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        qr_btn_close_activity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragmentToLoad", "friend")
            startActivity(intent)
            this.finish()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("FIX", "back")
    }

    // Get unique friend token and generate QR code from it
    private fun createQRToken() {
        apiController.get(QR_CREATE, authToken) { res ->
            try {
                if (res == null) throw Exception("No server response")

                val tokenRes = Gson().fromJson(res.toString(), FriendToken::class.java)

                Log.d("QR", "asd ${tokenRes.token}")

                generateQRCode(tokenRes.token)

            } catch (e: Exception) {
                Log.e("QR", "$e")
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun generateQRCode(friendToken: String) {
        val bitmap = QRCodeHelper
            .newInstance(this)
            .setContent(friendToken)
            .setErrorCorrenctionLevel(ErrorCorrectionLevel.Q)
            .setMargin(2)
            .getQRCOde()

        qr_image_view_code.setImageBitmap(bitmap)
    }
}