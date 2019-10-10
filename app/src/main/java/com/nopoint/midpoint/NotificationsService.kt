package com.nopoint.midpoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import org.json.JSONObject
import java.util.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.networking.UPDATE_TOKEN


const val ACCEPT_FRIEND_REQUEST = "1005"
const val DECLINE_FRIEND_REQUEST = "1006"
const val ACCEPT_MEETING_REQUEST = "1007"
const val DECLINE_MEETING_REQUEST = "1008"
const val EXTRA_NOTIFICATION_ID = "12345"
const val LAUNCHED_FROM_NOTIFICATION_ID = "09876"
const val UPDATE_MEETING_REQUESTS = "1009"

class NotificationsService : FirebaseMessagingService() {
    private val service = ServiceVolley()
    private val apiController = APIController(service)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Check if message contains a notification payload.
        // This is run when app is in the foreground
        remoteMessage.notification?.let {
            val body = it.body
            val title = it.title
            val meetingRequest = remoteMessage.data["meetingRequest"]
            if (meetingRequest != null) {
                val update = Intent(UPDATE_MEETING_REQUESTS)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(update)
            }
            val friendRequest = remoteMessage.data["friendRequest"]
            sendNotification(body!!, title!!, meetingRequest, friendRequest)
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

    private fun sendNotification(
        messageBody: String,
        messageTitle: String,
        meetingRequest: String?,
        friendRequest: String?
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(LAUNCHED_FROM_NOTIFICATION_ID, true)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val NOTIFICATION_ID = 1

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setColor(getColor(R.color.color_primary))
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if (meetingRequest != null) {
            Log.d("ID", meetingRequest)
            notificationBuilder
                .addAction(
                    R.drawable.ic_logo,
                    "Accept",
                    NotificationController.pendingIntent(
                        this,
                        ACCEPT_MEETING_REQUEST,
                        meetingRequest
                    )
                )
                .addAction(
                    R.drawable.ic_logo,
                    "Reject",
                    NotificationController.pendingIntent(
                        this,
                        DECLINE_MEETING_REQUEST,
                        meetingRequest
                    )
                )
        } else if (friendRequest != null) {
            notificationBuilder
                .addAction(
                    R.drawable.ic_logo,
                    "Accept",
                    NotificationController.pendingIntent(
                        this,
                        ACCEPT_FRIEND_REQUEST,
                        friendRequest
                    )
                )
                .addAction(
                    R.drawable.ic_logo,
                    "Reject",
                    NotificationController.pendingIntent(
                        this,
                        DECLINE_FRIEND_REQUEST,
                        friendRequest
                    )
                )
        }


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Meeting requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(true)
            channel.lightColor = R.color.color_white_accent
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            NOTIFICATION_ID /* ID of notification */,
            notificationBuilder.build()
        )
    }

    override fun onNewToken(token: String) {
        if (CurrentUser.getCurrentUser(this) != null) {
            val body = JSONObject()
            body.put("firebaseToken", token)

            apiController.post(UPDATE_TOKEN, body) { response ->
                val msg = response?.optString("msg") ?: response?.optString("errors")
            }
        }
    }

    object NotificationController {
        fun pendingIntent(context: Context, name: String, extras: String): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = name
                putExtra(EXTRA_NOTIFICATION_ID, extras)
            }
            return PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, 0)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

}