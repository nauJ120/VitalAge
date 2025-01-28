package com.example.vitalage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class KardexMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kardex_menu)

        // Referencias a los botones
        val btnInventory = findViewById<Button>(R.id.btnInventory)
        val btnMedCard = findViewById<Button>(R.id.btnMedCard)
        val btnControl = findViewById<Button>(R.id.btnControl)

        btnInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        btnMedCard.setOnClickListener {
            val intent = Intent(this, MedicationCardActivity::class.java)
            startActivity(intent)
        }

        btnControl.setOnClickListener {
            val intent = Intent(this, MedicalControlActivity::class.java)
            startActivity(intent)
        }
    }
}