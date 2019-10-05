package com.nopoint.midpoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.NotificationManager


class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val accept = intent.getStringExtra("actionAccept")
        val reject = intent.getStringExtra("actionReject")
        val notificationId = intent.getIntExtra("notificationId", 1)
        if (accept != null) {
            acceptRequest(accept)
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        } else if (reject != null) {
            rejectRequest(reject)
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
        //This is used to close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
    }

    fun acceptRequest(id: String) {
        Log.d("ACCEPT", "ACCEPTED")
        //Fetch all meeting requests
        //Find request with id
        //Send response with status 1
    }

    fun rejectRequest(id: String) {
        Log.d("REJECT", "REJECTED")
        //Fetch all meeting requests
        //Find request with id
        //Send response with status 2
    }

}