package com.example.vitalage

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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


import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class CamaraActivity : AppCompatActivity() {


    private lateinit var camaraBinding: CamaraBinding
    val PERM_CAMERA_CODE = 101
    private val TAG = CamaraActivity::class.java.simpleName
    val REQUEST_IMAGE_CAPTURE = 1
    val PERM_GALERY_GROUP_CODE = 1001
    val REQUEST_PICK = 1002
    private var imageFile: File? = null
    var outputPath: Uri? = null
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camaraBinding = CamaraBinding.inflate(layoutInflater)
        setContentView(camaraBinding.root)
        enableEdgeToEdge()
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        camaraBinding.buttonCamera.setOnClickListener{
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "Permiso de camara condecido", Toast.LENGTH_SHORT).show()

                    dispatchTakePictureIntent()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    Toast.makeText(this, "El permiso de Camara es necesario para usar esta actividad 😭", Toast.LENGTH_SHORT).show()
                }


                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA_CODE)
                }
            }
        }

        camaraBinding.buttonGalery.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) -> {
                    startGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    Toast.makeText(this, "El permiso de Galería es necesario para usar esta actividad 😭", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    val permissions = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    }

                    requestPermissions(permissions.toTypedArray(), PERM_GALERY_GROUP_CODE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
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


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Foto capturada correctamente", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    data?.extras?.get("data")?.let { imageBitmap ->
                        try {
                            // Convertimos la imagen a Bitmap
                            val bitmap = imageBitmap as Bitmap

                            // Guardamos la imagen en caché para compartirla entre actividades
                            val cacheDir = File(cacheDir, "images")
                            if (!cacheDir.exists()) cacheDir.mkdirs()
                            val imageFile = File(cacheDir, "captured_image.png")

                            FileOutputStream(imageFile).use { fos ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            }

                            // Obtenemos URI usando FileProvider
                            val imageUri = FileProvider.getUriForFile(this, "com.example.vitalage.fileprovider", imageFile)

                            // Creamos el intent para ir a la otra actividad
                            val intent = Intent(this, FotoCamaraActivity::class.java)
                            intent.putExtra("image_uri", imageUri.toString())  // Pasamos el URI como String
                            intent.putExtra("patient_id",patientId)
                            Log.d("DEBUG", "📌 patientId recibido: $patientId")
                            startActivity(intent)

                        } catch (e: OutOfMemoryError) {
                            Toast.makeText(this, "Imagen demasiado grande para procesar", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
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
                            // Abrimos un InputStream para leer la imagen
                            val inputStream = contentResolver.openInputStream(uri)
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(inputStream, null, options)
                            inputStream?.close() // Cerramos el InputStream después de obtener las dimensiones

                            // Calcular escala para evitar imágenes demasiado grandes
                            val scaleFactor = Math.max(1, options.outWidth / 1000) // Ajusta según necesidad
                            options.inJustDecodeBounds = false
                            options.inSampleSize = scaleFactor

                            // Volvemos a abrir el InputStream para decodificar la imagen
                            val newInputStream = contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
                            newInputStream?.close()

                            if (bitmap == null) {
                                throw Exception("No se pudo decodificar la imagen")
                            }

                            // Redimensionamos la imagen
                            val displayMetrics = resources.displayMetrics
                            val targetHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500f, displayMetrics).toInt()
                            val targetWidth = displayMetrics.widthPixels
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

                            // Guardamos la imagen en caché
                            val cacheDir = File(cacheDir, "images").apply { if (!exists()) mkdirs() }
                            val imageFile = File(cacheDir, "selected_image.png")

                            FileOutputStream(imageFile).use { fos ->
                                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            }

                            // Obtener URI con FileProvider
                            val imageUri = FileProvider.getUriForFile(this, "com.example.vitalage.fileprovider", imageFile)

                            // Enviamos la imagen a la otra actividad
                            val intent = Intent(this, FotoCamaraActivity::class.java).apply {
                                putExtra("image_uri", imageUri.toString())
                                intent.putExtra("patient_id",patientId)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Importante para compartir la Uri
                            }
                            startActivity(intent)

                        } catch (e: OutOfMemoryError) {
                            Toast.makeText(this, "Imagen demasiado grande para procesar", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("ImageProcessing", "Error al procesar la imagen", e)
                        }
                    }
                } else {
                    Toast.makeText(this, "No se seleccionó ningún archivo de galería", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}