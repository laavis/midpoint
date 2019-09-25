package com.nopoint.midpoint

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.Endpoint
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private val service = ServiceVolley()
    private val apiController = APIController(service)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hasPermissions()
        start_map_btn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            //Sets the map activity as the new root
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        network_btn.setOnClickListener {sendPostRequest()}
    }

    /**
     * Example function for sending post request & logging the output of the request
     */
    private fun sendPostRequest() {
        val path = "post"
        val params = JSONObject()
        params.put("email", "foo@email.com")
        params.put("password", "test")
        apiController.post(Endpoint.TEST, path, params) { response ->
            Log.d("server response", response?.get("data").toString())
        }
    }

    private fun hasPermissions() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
}
