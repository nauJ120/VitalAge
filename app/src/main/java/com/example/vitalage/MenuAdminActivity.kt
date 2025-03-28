package com.example.vitalage

import com.example.vitalage.AdminDashboardActivity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MenuAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_admin)

        // Acceso al Dashboard
        findViewById<CardView>(R.id.cardDashBoard).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        // Gestión de Usuarios
        findViewById<CardView>(R.id.cardManageUsers).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        // Gestión de Residentes
        findViewById<CardView>(R.id.cardManageResidents).setOnClickListener {
            startActivity(Intent(this, ResidentManagementActivity::class.java))
        }

        // Footer - Home
        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
        }

        // Footer - Perfil
        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
