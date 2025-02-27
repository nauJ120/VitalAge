package com.example.vitalage


import android.R.attr.bitmap
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.FotoCamaraBinding
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.net.Uri


class FotoCamaraActivity : AppCompatActivity() {

    private lateinit var fotoCamaraBinding: FotoCamaraBinding
    private val TAG = FotoCamaraActivity::class.java.simpleName
    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fotoCamaraBinding = FotoCamaraBinding.inflate(layoutInflater)
        setContentView(fotoCamaraBinding.root)
        enableEdgeToEdge()

        // Obtener la URI de la imagen desde el Intent
        val imageUriString = intent.getStringExtra("image_uri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        // Mostrar la imagen en un ImageView
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }

        imageUri?.let {
            imageView.setImageURI(it)
            fotoCamaraBinding.captura.addView(imageView)
            Log.d(TAG, "Imagen recibida correctamente: $imageUri")
        } ?: run {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }

        // Botón para escanear el texto
        fotoCamaraBinding.buttonEscanear.setOnClickListener {
            imageUri?.let { uri ->
                escanearTexto(uri)
            }
        }

        // Botón para volver a la cámara
        fotoCamaraBinding.buttonCamera.setOnClickListener {
            val intent = Intent(this, CamaraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun escanearTexto(imageUri: Uri) {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val image = InputImage.fromBitmap(bitmap, 0)
            Toast.makeText(this, "Reconociendo texto", Toast.LENGTH_SHORT).show()

            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    Toast.makeText(this, "Se reconoció el texto", Toast.LENGTH_SHORT).show()
                    val resultText = result.text.replace("rg", "mg", true)
                    Log.d(TAG, resultText)

                    // Clasificar el texto reconocido
                    val clasificacion = clasificarTexto(resultText)

                    // Pasar los datos a la siguiente actividad
                    val intent = Intent(this, EscaneoActivity::class.java).apply {
                        putExtra("nombreMedicamento", clasificacion["nombre"])
                        putExtra("cantidad", clasificacion["cantidad"])
                        putExtra("masa", clasificacion["masa"])
                        putExtra("otrosDatos", clasificacion["otros"])
                    }
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se reconoció el texto", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error al escanear la imagen", e)
        }
    }

    private fun clasificarTexto(texto: String): Map<String, String> {
        val resultado = mutableMapOf<String, String>()

        val regexMasa = Regex("\\d+\\s?(mg|ML|ml|g|GR|gr|MG)", RegexOption.IGNORE_CASE)
        val regexNombre = Regex("^[\\p{L}]+(\\s[\\p{L}]+)?$", RegexOption.IGNORE_CASE)
        val regexCantidad = Regex("([\\d+x]+)\\s?(capsula|tabletas|capsulas|capsules|tablets|tablet)", RegexOption.IGNORE_CASE)


        val cantidadEncontrada = regexCantidad.find(texto)?.value ?: "No detectado"
        val masaEncontrado = regexMasa.find(texto)?.value ?: "No detectado"

        val nombreEncontrado = texto.split("\n")
            .mapNotNull { regexNombre.find(it)?.value }
            .firstOrNull() ?: "No detectado"

        resultado["nombre"] = nombreEncontrado
        resultado["cantidad"] = cantidadEncontrada
        resultado["masa"] = masaEncontrado

        var textoRestante = texto
            .replace(nombreEncontrado, "", true)
            .replace(cantidadEncontrada, "", true)
            .replace(masaEncontrado, "", true)
            .replace(Regex("\\d+"), "")
            .trim()

        resultado["otros"] = if (textoRestante.isEmpty()) "No detectado" else textoRestante

        return resultado
    }
}