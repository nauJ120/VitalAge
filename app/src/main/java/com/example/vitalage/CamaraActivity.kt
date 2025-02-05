package com.example.vitalage
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast

import androidx.appcompat.widget.AppCompatButton


import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vitalage.databinding.CamaraBinding

class CamaraActivity : AppCompatActivity() {


    private lateinit var camaraBinding: CamaraBinding
    val PERM_CAMERA_CODE = 101
    private val TAG = CamaraActivity::class.java.simpleName
    val REQUEST_IMAGE_CAPTURE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camaraBinding = CamaraBinding.inflate(layoutInflater)
        setContentView(camaraBinding.root)
        enableEdgeToEdge()

        camaraBinding.buttonCamera.setOnClickListener{
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "Permiso de camara condecido", Toast.LENGTH_SHORT).show()
                    lanzar()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    Toast.makeText(this, "El permiso de Camara es necesario para usar esta actividad 😭", Toast.LENGTH_SHORT).show()
                }


                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA_CODE)
                }
            }
        }
    }


    private fun lanzar() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    val imagen = data?.extras?.get("data") as? android.graphics.Bitmap
                    if (imagen != null) {
                        val stream = java.io.ByteArrayOutputStream()
                        imagen.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                        val byteArray = stream.toByteArray()
                        val intent = Intent(this, FotoCamaraActivity::class.java)
                        intent.putExtra("image", byteArray)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }



}