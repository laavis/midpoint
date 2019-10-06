package com.nopoint.midpoint

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_FRIEND_REQUEST -> {
                Toast.makeText(context, "Accepted friend request", Toast.LENGTH_SHORT).show()
            }
            DECLINE_FRIEND_REQUEST -> {
                Toast.makeText(context, "Declined friend request", Toast.LENGTH_SHORT).show()
            }
            ACCEPT_MEETING_REQUEST -> {
                //Toast.makeText(context, "Accepted meeting request", Toast.LENGTH_SHORT).show()
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
            DECLINE_MEETING_REQUEST -> {
                //Toast.makeText(context, "Declined meeting request", Toast.LENGTH_SHORT).show()
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(1)
        //This is used to close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
    }

    fun acceptMeetingRequest(id: String) {
        //Fetch all meeting requests
        //Find request with id
        //Send response with status 1
    }

    fun declineMeetingRequest(id: String) {
        //Send response with status 2
    }

    fun acceptFriendRequest(id: String) {
    }

    fun declineFriendRequest(id: String) {
    }
}