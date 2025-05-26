package com.example.vitalage.enfermera

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vitalage.R


class AlarmaDeToma : BroadcastReceiver() {
    companion object{
        const val MY_CHANNEL_ID = "Channel1"
        const val NOTIFICATION_ID = 1
        const val TITLEEXTRA = "TITLEEXTRA"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Aquí puedes reprogramar tus alarmas
                Log.d("AlarmaDeToma", "Dispositivo reiniciado. Reprogramando alarmas.")
                // Código para reprogramar las alarmas aquí
            }

            else -> {
                // Aquí va tu lógica actual para mostrar la notificación
                val title = intent?.getStringExtra(TITLEEXTRA) ?: "Recordatorio de Medicamento"
                val nombrePaciente = intent?.getStringExtra("nombrePaciente") ?: "Paciente"
                val medicamento = intent?.getStringExtra("medicamento") ?: "Medicamento"

                val hora = intent?.getStringExtra("horario") ?: "Hora no definida"

                val mensajePersonalizado = """
                $nombrePaciente debe tomar:
                - $medicamento            
                - Hora: $hora
            """.trimIndent()

                val notificationBuilder = NotificationCompat.Builder(context!!, MY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_add_alert_24)
                    .setContentTitle(title)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(mensajePersonalizado))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            }
        }
    }


}