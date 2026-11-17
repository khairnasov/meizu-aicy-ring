package com.dinar.aicyring

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class AicyNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val pkg = sbn?.packageName ?: return
        if (pkg == packageName) return

        val prefs = getSharedPreferences("aicy_prefs", Context.MODE_PRIVATE)
        val defaultMap = mapOf(
            "org.telegram.messenger" to "#2AABEE",
            "com.google.android.youtube" to "#FF0033",
            "com.whatsapp" to "#25D366",
            "com.instagram.android" to "#E1306C",
            "com.vkontakte.android" to "#0077FF",
            "com.discord" to "#5865F2",
            "com.google.android.apps.bard" to "#4285F4"
        )

        val hexColor = prefs.getString("color_$pkg", defaultMap[pkg]) ?: return
        val parsedColor = try { Color.parseColor(hexColor) } catch (e: Exception) { Color.CYAN }

        triggerAicyRingPulse(this, pkg, parsedColor, 700, 300)
    }

    companion object {
        fun triggerAicyRingPulse(
            context: Context,
            tag: String,
            argbColor: Int,
            onMs: Int,
            offMs: Int
        ) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "aicy_led_channel_${Integer.toHexString(argbColor)}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Aicy Ring Color $channelId",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    lightColor = argbColor
                    setSound(null, null)
                    enableVibration(false)
                }
                nm.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Aicy Ring Active")
                .setContentText("Световой сигнал для $tag")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLights(argbColor, onMs, offMs)
                .setAutoCancel(true)
                .build()

            nm.notify(tag.hashCode(), notification)
        }
    }
}
