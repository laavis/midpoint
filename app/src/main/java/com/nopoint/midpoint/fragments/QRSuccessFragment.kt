package com.nopoint.midpoint.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.QRScannedActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.models.Friend
import kotlinx.android.synthetic.main.fragment_qr_success.*

class QRSuccessFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qr_success, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reqUsername = arguments?.getString(REQUESTER_USERNAME)


        qr_requester_username.text = reqUsername.toString()

        qr_scanned_btn_continue.setOnClickListener {
            val intent = Intent(context!!.applicationContext, MainActivity::class.java)
            intent.putExtra("fragmentToLoad", "friend")
            startActivity(intent)
            (activity as QRScannedActivity).finish()
        }

    }

    companion object {
        const val REQUESTER_USERNAME = ""

        fun newInstance(reqUsername: String): QRSuccessFragment {
            val fragment = QRSuccessFragment()

            val bundle = Bundle().apply {
                putString(REQUESTER_USERNAME, reqUsername)
            }

            fragment.arguments = bundle

            return fragment
        }
    }
}