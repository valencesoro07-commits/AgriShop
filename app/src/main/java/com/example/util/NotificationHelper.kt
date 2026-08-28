package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.NotificationType

object NotificationHelper {
    const val CHANNEL_RENTALS = "channel_rentals"
    const val CHANNEL_PRODUCE = "channel_produce"
    const val CHANNEL_FORUM = "channel_forum"
    const val CHANNEL_PAYMENTS = "channel_payments"
    const val CHANNEL_ECO = "channel_eco"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val rentalsChannel = NotificationChannel(
                CHANNEL_RENTALS,
                "Contrats & Rappels de Matériel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes de restitution et de validation de location d'engins"
            }

            val produceChannel = NotificationChannel(
                CHANNEL_PRODUCE,
                "Nouvelles Récoltes & Offres Proches",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Annonces de vivriers et récoltes disponibles près de chez vous"
            }

            val forumChannel = NotificationChannel(
                CHANNEL_FORUM,
                "Forum & Conseils Agricoles",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Réponses aux questions communautaires et entraide"
            }

            val paymentsChannel = NotificationChannel(
                CHANNEL_PAYMENTS,
                "Paiements & Mobile Money",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Confirmations de transactions Wave, Orange, MTN et CinetPay"
            }

            val ecoChannel = NotificationChannel(
                CHANNEL_ECO,
                "Éco-Points & Compost",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Crédits d'éco-points et collectes de résidus"
            }

            manager.createNotificationChannels(listOf(rentalsChannel, produceChannel, forumChannel, paymentsChannel, ecoChannel))
        }
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        type: NotificationType,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val channelId = when (type) {
            NotificationType.RENTAL_REMINDER -> CHANNEL_RENTALS
            NotificationType.NEW_LISTING -> CHANNEL_PRODUCE
            NotificationType.FORUM_REPLY -> CHANNEL_FORUM
            NotificationType.PAYMENT_SUCCESS -> CHANNEL_PAYMENTS
            NotificationType.ECO_POINTS, NotificationType.WASTE_PICKUP -> CHANNEL_ECO
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            // Graceful catch for test container or permission denial
        }
    }
}
