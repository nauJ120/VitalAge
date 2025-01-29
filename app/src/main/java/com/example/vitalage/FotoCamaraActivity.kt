package com.example.vitalage

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.vitalage.databinding.FotoCamaraBinding
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.DateFormat.getDateInstance
import java.util.Date
import java.util.Objects

class FotoCamaraActivity : AppCompatActivity(){

    private lateinit var fotoCamaraBinding: FotoCamaraBinding

    val PERM_CAMERA_CODE = 101
    private val TAG = CamaraActivity::class.java.simpleName
    val REQUEST_IMAGE_CAPTURE = 1
    var outputPath: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fotoCamaraBinding = FotoCamaraBinding.inflate(layoutInflater)
        setContentView(fotoCamaraBinding.root)
        enableEdgeToEdge()

        val byteArray = intent.getByteArrayExtra("image")
        val imagen =
            byteArray?.let { android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

        Log.d(TAG, "onActivityResult: Foto recibida correctamente")
        fotoCamaraBinding.captura.removeAllViews()
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        imageView.setImageBitmap(imagen)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.adjustViewBounds = true
        fotoCamaraBinding.captura.addView(imageView)


        fotoCamaraBinding.buttonEscanear.setOnClickListener{
            val intent = Intent(this, EscaneoActivity::class.java)
            startActivity(intent)

        }
        fotoCamaraBinding.buttonCamera.setOnClickListener {
            val intent = Intent(this, CamaraActivity::class.java)
            startActivity(intent)
        }
    }





}