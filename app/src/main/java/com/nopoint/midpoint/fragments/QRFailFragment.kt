package com.nopoint.midpoint.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nopoint.midpoint.*
import kotlinx.android.synthetic.main.fragment_qr_fail.*

class QRFailFragment: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qr_fail, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val errorMsg = arguments?.getString(ERROR_MSG)

        if (errorMsg!!.isNotEmpty()) {
            qr_fail_message.text =  errorMsg.toString()
        }

        qr_fail_btn_return.setOnClickListener {
            val intent = Intent(context!!.applicationContext, QRActivity::class.java)
            startActivity(intent)
            (activity as QRScannedActivity).finish()
        }
    }

    companion object {
        const val ERROR_MSG = ""

        fun newInstance(errorMsg: String): QRFailFragment {
            val fragment = QRFailFragment()

            val bundle = Bundle().apply {
                putString(ERROR_MSG, errorMsg)
            }

            fragment.arguments = bundle

            return fragment
        }
    }
}