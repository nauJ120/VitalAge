package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_admin)

        findViewById<CardView>(R.id.cardDashBoard).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        findViewById<CardView>(R.id.cardManageUsers).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        findViewById<CardView>(R.id.cardManageResidents).setOnClickListener {
            startActivity(Intent(this, ResidentManagementActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this) {
        }
    }
