package com.nopoint.midpoint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.nopoint.midpoint.fragments.QRFailFragment
import com.nopoint.midpoint.fragments.QRSuccessFragment
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.QR_REDEEM
import com.nopoint.midpoint.networking.ServiceVolley
import org.json.JSONObject
import java.lang.Exception

class QRScannedActivity : AppCompatActivity() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var authToken: String
    private lateinit var friendToken: String

    private lateinit var fTransaction: FragmentTransaction
    private lateinit var fManager: FragmentManager
    private lateinit var successFragment: QRSuccessFragment
    private lateinit var failFragment: QRFailFragment

    private var errorMsg: String = ""
    private var reqUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = SharedPref(this)

        if (sharedPref.loadNightModeState() == true) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanned)

        fManager = supportFragmentManager
        fTransaction = fManager.beginTransaction()

        successFragment = QRSuccessFragment()
        failFragment = QRFailFragment()

        authToken = CurrentUser.getLocalUser(this)!!.token

        if (intent.getSerializableExtra(SCANNED_STRING) == null) {
            throw RuntimeException("No encrypted String found in intent")
        }

        friendToken = intent.getStringExtra(SCANNED_STRING)


        redeemQrToken()
    }

    data class RedeemToken(
        @SerializedName("error")val error: String?,
        @SerializedName("requester_username")val requester_username: String?
    )

    private fun redeemQrToken() {
        val body = JSONObject()
        body.put("token", friendToken)

        apiController.post(QR_REDEEM, body, authToken) { res ->
            try {
                if (res == null) {
                    throw Exception("Failed to connect")
                }

                val redeemRes = Gson().fromJson(res.toString(), RedeemToken::class.java)


                if (redeemRes.error != null) {
                    errorMsg = redeemRes.run { error.toString() }
                    val fragment = QRFailFragment.newInstance(errorMsg)
                    getFailFragment(fragment)
                    throw Exception("${redeemRes.error}")
                } else {
                    reqUsername = redeemRes.requester_username.toString()

                    val fragment = QRSuccessFragment.newInstance(reqUsername)
                    geSuccessFragment(fragment)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun geSuccessFragment(f: QRSuccessFragment) {
        fTransaction = fManager
            .beginTransaction()
            .addToBackStack(null)
        fTransaction.replace(R.id.qr_outcome_container, f)
        fTransaction.commit()
    }

    private fun getFailFragment(f: QRFailFragment) {
        fTransaction = fManager
            .beginTransaction()
            .addToBackStack(null)
        fTransaction.replace(R.id.qr_outcome_container, f)
        fTransaction.commit()
    }

    companion object {
        private const val SCANNED_STRING = "scanned_string"
        fun getScannedActivity(callerClassContext: Context, encryptedString: String): Intent {
            return Intent(callerClassContext, QRScannedActivity::class.java).putExtra(SCANNED_STRING, encryptedString)
        }
    }
}