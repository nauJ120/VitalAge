package com.example.vitalage
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView


import androidx.appcompat.widget.AppCompatButton


import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.vitalage.databinding.CamaraBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CamaraActivity : AppCompatActivity() {


    private lateinit var camaraBinding: CamaraBinding
    val PERM_CAMERA_CODE = 101
    private val TAG = CamaraActivity::class.java.simpleName
    val REQUEST_IMAGE_CAPTURE = 1
    val PERM_GALERY_GROUP_CODE = 202
    val REQUEST_PICK = 3

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

        camaraBinding.buttonGalery.setOnClickListener(){
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    Toast.makeText(applicationContext, "El permiso de Galeria es necesario para usar esta actividad  😭 ", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.plus(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                    requestPermissions(permissions, PERM_GALERY_GROUP_CODE)
                }
            }
        }
    }


    private fun startGallery() {
        val intentPick = Intent(Intent.ACTION_PICK)
        intentPick.type =  "image/*"
        startActivityForResult(intentPick, REQUEST_PICK)
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
            REQUEST_PICK -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Se seleccionó un archivo de galería", Toast.LENGTH_SHORT).show()
                    data?.data?.let { uri ->
                        try {
                            val inputStream = contentResolver.openInputStream(uri)

                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeStream(inputStream, null, options)

                            // Calcular escala para evitar imágenes demasiado grandes
                            val scaleFactor = Math.max(1, options.outWidth / 1000) // Ajusta según necesidad
                            options.inJustDecodeBounds = false
                            options.inSampleSize = scaleFactor

                            inputStream?.close() // Cerrar y volver a abrir para evitar errores
                            val newInputStream = contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)

                            if (bitmap == null) {
                                throw Exception("No se pudo decodificar la imagen")
                            }

                            val displayMetrics = resources.displayMetrics
                            val targetHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500f, displayMetrics).toInt()
                            val targetWidth = displayMetrics.widthPixels
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

                            // Guardar la imagen en caché
                            val cacheDir = File(cacheDir, "images")
                            if (!cacheDir.exists()) cacheDir.mkdirs()
                            val imageFile = File(cacheDir, "selected_image.png")

                            FileOutputStream(imageFile).use { fos ->
                                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            }

                            // Obtener URI con FileProvider
                            val imageUri = FileProvider.getUriForFile(this, "com.example.vitalage.fileprovider", imageFile)

                            val intent = Intent(this, FotoCamaraActivity::class.java)
                            intent.putExtra("image_uri", imageUri.toString())
                            startActivity(intent)

                        } catch (e: OutOfMemoryError) {
                            Toast.makeText(this, "Imagen demasiado grande para procesar", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "No se seleccionó ningún archivo de galería", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



}