package com.rrr.apprrre

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rrr.apprrre.adapters.MenuAdapter
import com.rrr.apprrre.fragments.LatasFragment
import com.rrr.apprrre.models.MenuItem

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var bienvenidaTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        // Inicializando FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Referenciando el TextView
        bienvenidaTextView = findViewById(R.id.bienvenidaText)
        recyclerView = findViewById(R.id.menuRecyclerView)

        // Configurar el RecyclerView para el menú horizontal
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val menuItems = listOf(
            MenuItem("Latas", R.drawable.ic_lata),
            MenuItem("Otros Residuos", R.drawable.ic_residuos),
            MenuItem("Papel y Cartón", R.drawable.ic_papel),
            MenuItem("Plástico", R.drawable.ic_plastico),
            MenuItem("Vidrio", R.drawable.ic_vidrio),
            MenuItem("Reutilización", R.drawable.ic_reutilizar),
            MenuItem("Comunidad", R.drawable.ic_comunidad)
        )

        val adapter = MenuAdapter(menuItems) { item ->
            // Manejo de click en cada opción del menú
            when (item.name) {
                "Latas" -> navigateToFragment(LatasFragment())
                else -> Toast.makeText(this, "Próximamente: ${item.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        // Obteniendo el UID del usuario autenticado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Consultando Firestore para obtener el nombre del usuario
            firestore.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
                        bienvenidaTextView.text = "¡Bienvenido, $nombre!"
                    } else {
                        bienvenidaTextView.text = "¡Bienvenido, Usuario!"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener el nombre: ${e.message}", Toast.LENGTH_SHORT).show()
                    bienvenidaTextView.text = "¡Bienvenido, Usuario!"
                }
        } else {
            // Si no hay usuario autenticado, mostrar un mensaje genérico
            bienvenidaTextView.text = "¡Bienvenido, Usuario!"
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
