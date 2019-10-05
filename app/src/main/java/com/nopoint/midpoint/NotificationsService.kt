package com.nopoint.midpoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.nopoint.midpoint.models.MeetingRequest
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import org.json.JSONObject

const val ACCEPT_FRIEND_REQUEST = "1005"
const val DECLINE_FRIEND_REQUEST = "1006"
const val ACCEPT_MEETING_REQUEST = "1007"
const val DECLINE_MEETING_REQUEST = "1008"
const val EXTRA_NOTIFICATION_ID = "12345"

class NotificationsService : FirebaseMessagingService() {
    private val service = ServiceVolley()
    private val apiController = APIController(service)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        // This is run when app is in the foreground
        remoteMessage.notification?.let {
            val body = it.body
            val title = it.title
            val meetingRequest = remoteMessage.data["meetingRequest"]
            val friendRequest = remoteMessage.data["friendRequest"]
            if (meetingRequest != null) {
                try {
                    val result = Gson().fromJson(meetingRequest, MeetingRequest::class.java)
                    sendNotification(body!!, title!!, result)
                } catch (exception: Throwable) {
                    exception.printStackTrace()
                }
            } else if (friendRequest != null) {
                try {
                    //val result = Gson().fromJson(friendRequest, FriendRequest::class.java) TODO: create friend request model

                } catch (exception: Throwable) {
                    exception.printStackTrace()
                }
            }
        }
    }


    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance().beginWith(work).enqueue()
        // [END dispatch_job]
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(
        messageBody: String,
        messageTitle: String,
        meetingRequest: MeetingRequest
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val NOTIFICATION_ID = 1
        //This is optional if you have more than one buttons and want to differentiate between two

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_logo, "Accept", NotificationController.pendingIntent(this, ACCEPT_MEETING_REQUEST, meetingRequest.id))
            .addAction(R.drawable.ic_logo, "Reject", NotificationController.pendingIntent(this, DECLINE_MEETING_REQUEST, meetingRequest.id))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Meeting requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            NOTIFICATION_ID /* ID of notification */,
            notificationBuilder.build()
        )
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        val path = "users/updateToken"
        val body = JSONObject()
        body.put("firebaseToken", token)
        apiController.post(API.LOCAL_API, path, body) { response ->
            val msg = response?.optString("msg") ?: response?.optString("errors")
            Log.d("FIRBASE TOKEN", msg!!)
        }
    }

    object NotificationController {
        fun pendingIntent(context: Context, name: String, extras: String): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = name
                putExtra(EXTRA_NOTIFICATION_ID, extras)
            }

            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }
}