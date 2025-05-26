package com.example.vitalage.generales

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.R


//Kotlin create a data class with name and age and a function to print out the name and age





class MainActivity : AppCompatActivity() {
    companion object{
        const val MY_CHANNEL_ID = "Channel1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        crearCanalDeNotificacion()
        setContentView(R.layout.iniciar_sesion)

        val intent = Intent(this, IniciarSesionActivity::class.java)
        startActivity(intent)
    }
    private fun crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL_ID,
                "Vitalage",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
    }
}

