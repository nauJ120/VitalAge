package com.example.vitalage


import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.FotoCamaraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

class FotoCamaraActivity : AppCompatActivity() {

    private lateinit var fotoCamaraBinding: FotoCamaraBinding
    private val TAG = FotoCamaraActivity::class.java.simpleName
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var listaMedicamentos: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fotoCamaraBinding = FotoCamaraBinding.inflate(layoutInflater)
        setContentView(fotoCamaraBinding.root)
        enableEdgeToEdge()

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Cargar nombres de medicamentos desde JSON
        listaMedicamentos = cargarNombresDesdeJson(this, "Lista_casi.json")

        // Obtener la URI de la imagen desde el Intent
        val imageUriString = intent.getStringExtra("image_uri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        if (imageUri != null) {
            // Crear un ImageView dinámico
            val imageView = ImageView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageURI(imageUri) // Cargar la imagen
            }

            // Asegurar que `captura` esté vacío antes de agregar la imagen
            fotoCamaraBinding.captura.removeAllViews()

            // Agregar el ImageView al MaterialCardView
            fotoCamaraBinding.captura.postDelayed({
                fotoCamaraBinding.captura.addView(imageView)
            }, 500) // Pequeño delay para asegurar carga correcta

            Log.d(TAG, "Imagen recibida correctamente: $imageUri")
        } else {
            Log.e(TAG, "imageUri es null, no se puede cargar la imagen")
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }

        // Botón para escanear el texto
        fotoCamaraBinding.buttonEscanear.setOnClickListener {
            imageUri?.let { uri -> escanearTexto(uri) }
        }

        // Botón para volver a la cámara
        fotoCamaraBinding.buttonCamera.setOnClickListener {
            val intent = Intent(this, CamaraActivity::class.java)
            startActivity(intent)
        }
    }


    private fun cargarNombresDesdeJson(context: Context, fileName: String): List<String> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            List(jsonArray.length()) { jsonArray.getString(it).uppercase() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el archivo JSON", e)
            emptyList()
        }
    }

    private fun limpiarTexto(texto: String): String {
        return texto.replace("\n", " ")      // Reemplazar saltos de línea con espacios
            .replace(Regex("[^A-Z0-9 ]"), "") // Eliminar caracteres especiales (mantener letras y números)
            .replace(Regex("\\s+"), " ")      // Reemplazar múltiples espacios con uno solo
            .trim()                            // Eliminar espacios al inicio y final
    }

    private fun buscarCoincidencia(nombreDetectado: String, listaOrdenada: List<String>): String {
        val textoLimpio = limpiarTexto(nombreDetectado.uppercase()).trim()
        val palabrasTexto = quitarTildes(textoLimpio).split(" ").map { it.trim() }

        var mejorCoincidencia = "No detectado"
        var maxCoincidencias = 0



        for (jsonStr in listaOrdenada) {
            val jsonObject = JSONObject(jsonStr)
            val principioActivo = jsonObject.getString("PRINCIPIOACTIVO").uppercase()
            val palabrasMedicamento = quitarTildes(principioActivo).split(" ").map { it.trim() }


            val palabrasCoincidentes = palabrasTexto.intersect(palabrasMedicamento.toSet()).size




            if (palabrasCoincidentes > maxCoincidencias) {
                maxCoincidencias = palabrasCoincidentes
                mejorCoincidencia = principioActivo

            }
        }


        return mejorCoincidencia
    }


    private fun escanearTexto(imageUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val image = InputImage.fromBitmap(bitmap, 0)
            Toast.makeText(this, "Reconociendo texto", Toast.LENGTH_SHORT).show()

            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    Toast.makeText(this, "Texto reconocido", Toast.LENGTH_SHORT).show()
                    val resultText = result.text.replace("rg", "mg", true)
                    Log.d(TAG, resultText)

                    // Clasificar el texto reconocido (enviando también el original)
                    val clasificacion = clasificarTexto(resultText, listaMedicamentos)

                    // Pasar los datos a la siguiente actividad con el texto original
                    val intent = Intent(this, EscaneoActivity::class.java).apply {
                        putExtra("nombreMedicamento", clasificacion["nombreOriginal"])
                        putExtra("cantidad", clasificacion["cantidadOriginal"])
                        putExtra("masa", clasificacion["masaOriginal"])
                        putExtra("otrosDatos", clasificacion["otrosOriginal"])
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

    private fun clasificarTexto(texto: String, nombresJsonOrdenados: List<String>): Map<String, String> {
        val resultado = mutableMapOf<String, String>()

        val textoNormalizado = quitarTildes(texto.uppercase())
        val lineas = textoNormalizado.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val nombresDetectados = lineas.mapNotNull {
            val resultado = buscarCoincidencia(it, nombresJsonOrdenados)
            Log.d("DEBUG", "Línea: $it - Resultado: $resultado")
            resultado
        }

        var mejorNombre = "No detectado"

        val palabrasTexto = textoNormalizado.split(" ").toSet()

        for (nombre in nombresDetectados) {
            val nombreLetras = nombre.uppercase().replace(Regex("\\s+"), "").toCharArray()
            val textoLetras = palabrasTexto.joinToString("").uppercase().replace(Regex("\\s+"), "").toCharArray()

            Log.d("DEBUG", "Comparando: Texto -> ${textoLetras.joinToString("")} con Nombre -> ${nombreLetras.joinToString("")}")

            var indexTexto = 0
            var indexNombre = 0

            while (indexTexto < textoLetras.size && indexNombre < nombreLetras.size) {
                if (textoLetras[indexTexto] == nombreLetras[indexNombre]) {
                    indexNombre++
                }
                indexTexto++
            }

            if (indexNombre == nombreLetras.size) {
                mejorNombre = nombre
                Log.d("DEBUG", "Coincidencia exacta encontrada: $nombre")
                break
            }
        }

        // **Si el nombre sigue siendo "No detectado", llamar a busquedaBinariaUltimoElemento**
        if (mejorNombre == "No detectado") {
            mejorNombre = buscarPorSubstring(nombresJsonOrdenados, textoNormalizado)
            Log.d("DEBUG", "Nombre obtenido por búsqueda binaria: $mejorNombre")
        }

        val regexMasa = Regex("\\d+\\s?(mg|ML|ml|g|GR|gr|MG)", RegexOption.IGNORE_CASE)
        val regexCantidad = Regex("(\\d+[xX]?\\d*)\\s?(cápsula|tabletas|capsulas|capsules|tablets|tablet|comprimidos)", RegexOption.IGNORE_CASE)

        val cantidadEncontrada = regexCantidad.find(textoNormalizado)?.value ?: "No detectado"
        val masaEncontrada = regexMasa.find(textoNormalizado)?.value ?: "No detectado"

        resultado["nombre"] = mejorNombre
        resultado["cantidad"] = cantidadEncontrada
        resultado["masa"] = masaEncontrada

        var textoRestante = textoNormalizado
            .replace(mejorNombre, "", true)
            .replace(cantidadEncontrada, "", true)
            .replace(masaEncontrada, "", true)
            .replace(Regex("\\d+(?:\\s?(mg|ML|ml|g|GR|gr|MG))?", RegexOption.IGNORE_CASE), "")
            .trim()

        resultado["otros"] = if (textoRestante.isEmpty()) "No detectado" else textoRestante

        resultado["nombreOriginal"] = mejorNombre
        resultado["cantidadOriginal"] = cantidadEncontrada
        resultado["masaOriginal"] = masaEncontrada
        resultado["otrosOriginal"] = textoRestante

        return resultado
    }


    private fun buscarPorSubstring(lista: List<String>, objetivo: String): String {
        val primeraPalabraObjetivo = extraerPrincipioActivo(objetivo).split("\\s+".toRegex())[0] // Primera palabra limpia

        Log.d("DEBUG", "Buscando coincidencias para: \"$primeraPalabraObjetivo\" en una lista de ${lista.size} elementos.")

        var inicio = 0
        var fin = lista.size - 1
        var mejorCoincidencia = "No detectado"
        var maxCoincidencia = 0

        // Lista para almacenar posibles coincidencias ordenadas por prioridad
        val posiblesCoincidencias = mutableListOf<Pair<String, Int>>()

        while (inicio <= fin) {
            val medio = (inicio + fin) / 2
            var primeraPalabraElemento = lista[medio].split("\\s+".toRegex())[0]
            primeraPalabraElemento = extraerPrincipioActivo(primeraPalabraElemento)

            val coincidencias = contarLetrasInicialesIguales(primeraPalabraElemento, primeraPalabraObjetivo)
            Log.d("DEBUG", "Comparando: \"$primeraPalabraElemento\" con \"$primeraPalabraObjetivo\" -> Letras iguales: $coincidencias")

            if (coincidencias > 0) {
                posiblesCoincidencias.add(primeraPalabraElemento to coincidencias)
            }

            // Ajustar búsqueda binaria
            if (primeraPalabraElemento < primeraPalabraObjetivo) {
                inicio = medio + 1
            } else {
                fin = medio - 1
            }
        }

        // Ordenar coincidencias por cantidad de letras iguales en orden descendente
        if (posiblesCoincidencias.isNotEmpty()) {
            val mejor = posiblesCoincidencias.maxByOrNull { it.second }
            mejorCoincidencia = mejor?.first ?: "No detectado"
            maxCoincidencia = mejor?.second ?: 0
        }

        Log.d("DEBUG", "✅ Mejor coincidencia encontrada: \"$mejorCoincidencia\" con $maxCoincidencia letras iguales")
        return mejorCoincidencia
    }


    private fun contarLetrasInicialesIguales(a: String, b: String): Int {
        val minLength = minOf(a.length, b.length)
        var count = 0

        for (i in 0 until minLength) {
            if (a[i].equals(b[i], ignoreCase = true)) {
                count++
            }
        }

        return count
    }


    private fun extraerPrincipioActivo(objetivo: String): String {
        val regex = """"PRINCIPIOACTIVO":"([^"]+)"""".toRegex()
        val match = regex.find(objetivo)
        return match?.groupValues?.get(1) ?: objetivo // Si no hay coincidencia, usar el string original
    }




    private fun quitarTildes(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}