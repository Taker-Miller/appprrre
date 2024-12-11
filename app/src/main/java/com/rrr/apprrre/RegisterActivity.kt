package com.rrr.apprrre

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var nombreInputLayout: TextInputLayout
    private lateinit var apellidoInputLayout: TextInputLayout
    private lateinit var correoInputLayout: TextInputLayout
    private lateinit var contrasenaInputLayout: TextInputLayout
    private lateinit var confirmarContrasenaInputLayout: TextInputLayout

    private lateinit var registrarButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializando FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inicializando los campos de entrada
        nombreInputLayout = findViewById(R.id.nombreInputLayout)
        apellidoInputLayout = findViewById(R.id.apellidoInputLayout)
        correoInputLayout = findViewById(R.id.correoInputLayout)
        contrasenaInputLayout = findViewById(R.id.contrasenaInputLayout)
        confirmarContrasenaInputLayout = findViewById(R.id.confirmarContrasenaInputLayout)

        // Inicializando el botón de registro
        registrarButton = findViewById(R.id.registerButton)

        // Seteando el click listener del botón de registro
        registrarButton.setOnClickListener {
            val nombre = nombreInputLayout.editText?.text.toString()
            val apellido = apellidoInputLayout.editText?.text.toString()
            val correo = correoInputLayout.editText?.text.toString()
            val contrasena = contrasenaInputLayout.editText?.text.toString()
            val confirmarContrasena = confirmarContrasenaInputLayout.editText?.text.toString()

            // Validación simple
            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show()
            } else if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
            } else {
                // Registro en Firebase
                registerUser(correo, contrasena, nombre, apellido)
            }
        }
    }

    // Función para registrar el usuario en Firebase
    private fun registerUser(correo: String, contrasena: String, nombre: String, apellido: String) {
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val userMap = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "correo" to correo
                    )

                    userId?.let {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.collection("usuarios")
                            .document(it)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso. Redirigiendo al inicio de sesión...", Toast.LENGTH_SHORT).show()
                                redirectToLogin()
                            }
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(this, "Error al guardar los datos en la base de datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
                    Toast.makeText(this, "Error en el registro: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Función para redirigir al LoginActivity
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
