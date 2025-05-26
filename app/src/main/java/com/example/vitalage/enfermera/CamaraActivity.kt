package com.example.vitalage.enfermera

import android.Manifest
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
import android.widget.TextView
import android.widget.Toast


import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.vitalage.BuildConfig
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.CamaraBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CamaraActivity : AppCompatActivity() {


    private lateinit var camaraBinding: CamaraBinding
    val PERM_CAMERA_CODE = 101

    val REQUEST_IMAGE_CAPTURE = 1
    val PERM_GALERY_GROUP_CODE = 1001
    val REQUEST_PICK = 1002
    private lateinit var photoUri: Uri
    private lateinit var imageFile: File

    private lateinit var patientId: String
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camaraBinding = CamaraBinding.inflate(layoutInflater)
        setContentView(camaraBinding.root)


        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.enfermera)
            tvUser.text = "Enfermera: $usuarioActual"
        }

        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        val cachePath = File(cacheDir, "images")
        if (!cachePath.exists()) cachePath.mkdirs()

        imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_20250423_225515_8072563812882029060.jpg")
        photoUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )




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
        camaraBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        camaraBinding.iconScan.setOnClickListener{
            finish()
        }


        camaraBinding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
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




    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Verifica que hay una app para manejar el intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            try {
                // Asegúrate de tener un archivo nuevo cada vez
                imageFile = createImageFile()
                photoUri = FileProvider.getUriForFile(this, "com.example.vitalage.fileprovider", imageFile)

                // Pasa el URI como salida
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // Da permisos de lectura
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: IOException) {
                Toast.makeText(this, "Error creando archivo de imagen", Toast.LENGTH_SHORT).show()
                Log.e("CameraError", "IOException: ${e.message}", e)
            }
        }
    }
    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        Log.d("Firebase", "UID del usuario autenticado: $uid")

        // 🔥 Corregimos la referencia según la estructura: user -> users -> {UID}
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Intentamos obtener el nombre desde ambas posibles claves
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    Log.d("Firebase", "Nombre obtenido de la base de datos: $nombreUsuario")
                    callback(nombreUsuario)
                } else {
                    Log.e("Firebase", "No se encontró el usuario en la base de datos.")
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener el nombre: ${error.message}")
                callback("Desconocido")
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    try {
                        // Validamos que el archivo exista
                        if (imageFile.exists()) {
                            val imageUri = FileProvider.getUriForFile(this, "com.example.vitalage.fileprovider", imageFile)

                            // Enviamos a la siguiente actividad
                            val intent = Intent(this, FotoCamaraActivity::class.java)
                            intent.putExtra("image_uri", imageUri.toString())
                            intent.putExtra("patient_id", patientId)
                            Log.d("DEBUG", "📌 patientId recibido: $patientId")
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "La imagen no se pudo guardar correctamente", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: OutOfMemoryError) {
                        Toast.makeText(this, "Imagen demasiado grande para procesar", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
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

                                Log.d("DEBUG", "📌 patientId recibidomandando: $patientId")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Importante para compartir la Uri
                            }
                            intent.putExtra("patient_id",patientId)
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

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // Usuario no logeado
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        val userId = user.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("user/users/$userId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val rol = snapshot.child("rol").value.toString()

            // Aquí comparas según el rol esperado
            if (rol != "Enfermera") {
                Toast.makeText(this, "Acceso solo para enfermeras", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
        }
    }

}