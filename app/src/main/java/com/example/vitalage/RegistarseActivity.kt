package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.CheckBox
import android.widget.Toast
import com.example.vitalage.databinding.RegistrarseBinding
import com.example.vitalage.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.database




class RegistarseActivityActivity : AppCompatActivity() {

    var message = "Cedula de Ciudadania"
    private lateinit var binding: RegistrarseBinding

    private lateinit var auth: FirebaseAuth
    private val database = Firebase.database
    private val messageRef = database.getReference("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = RegistrarseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        val atras = findViewById<ImageView>(R.id.flechitaatras)

        val checkb = findViewById<CheckBox>(R.id.checkbox)
        val checkbo = findViewById<CheckBox>(R.id.checkbox2)
        val checkbox3 = findViewById<CheckBox>(R.id.checkbox3)

        val editable = findViewById<TextInputEditText>(R.id.tipo_de_doc)
        editable.isFocusable = false
        editable.isFocusableInTouchMode = false
        editable.inputType = InputType.TYPE_NULL

        message = intent.getStringExtra("tipo_documento") ?: message
        editable.setText(message)


        editable.setOnClickListener(){
            val intent = Intent(this, TipoDocumentoActivity::class.java)
            startActivity(intent)
        }


        atras.setOnClickListener(){
            val intent = Intent(this, IniciarSesionActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener(){
            registrarse()
        }

        checkb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkbo.isChecked = false
                checkbox3.isChecked = false
            }
        }

        checkbo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkb.isChecked = false
                checkbox3.isChecked = false
            }
        }

        checkbox3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkb.isChecked = false
                checkbo.isChecked = false
            }
        }

    }

    private fun registrarse() {
        auth.createUserWithEmailAndPassword(
            binding.correo.text.toString(),
            binding.contrasenia.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = binding.correo.text.toString()
                }
                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { profileUpdateTask ->
                        if (profileUpdateTask.isSuccessful) {

                            val userid = Firebase.auth.currentUser?.uid


                            var rol : String = ""


                            if(binding.checkbox.isChecked){
                                rol = "Enfermera"
                            } else if(binding.checkbox2.isChecked){
                                rol = "Administrador"
                            } else if(binding.checkbox3.isChecked){
                                rol = "Medico"
                            }



                            val usuario = User(binding.tipoDeDoc.text.toString(),binding.numeroDeDoc.text.toString(),binding.correo.text.toString(),binding.contrasenia.text.toString(),rol)

                            if (userid != null) {
                                messageRef.child("users").child(userid).setValue(usuario).addOnSuccessListener {
                                    Toast.makeText(this, "Usuario guardado correctamente", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener{
                                    Toast.makeText(this, "Error al guardar el usuario", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Toast.makeText(applicationContext, "Usuario registrado", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, IniciarSesionActivity::class.java))
                        } else {

                            Toast.makeText(applicationContext, "Error al actualizar el perfil del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {

                task.exception?.localizedMessage?.let {
                    val errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
                    Toast.makeText(applicationContext, "Fallo al registrar usuario: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}