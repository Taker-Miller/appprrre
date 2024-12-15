package com.rrr.apprrre

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rrr.apprrre.adapters.MenuAdapter
import com.rrr.apprrre.fragments.CartonPapelFragment
import com.rrr.apprrre.fragments.ComunidadFragment
import com.rrr.apprrre.fragments.LatasFragment
import com.rrr.apprrre.fragments.OtrosResiduosFragment
import com.rrr.apprrre.models.MenuItem

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var bienvenidaTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        bienvenidaTextView = findViewById(R.id.bienvenidaText)
        recyclerView = findViewById(R.id.menuRecyclerView)

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
            when (item.name) {
                "Latas" -> showFragment(LatasFragment())
                "Comunidad" -> showFragment(ComunidadFragment())
                "Otros Residuos" -> showFragment(OtrosResiduosFragment())
                "Papel y Cartón" -> showFragment(CartonPapelFragment())
                else -> Toast.makeText(this, "Opción no implementada: ${item.name}", Toast.LENGTH_SHORT).show()
            }
        }


        recyclerView.adapter = adapter

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
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
            bienvenidaTextView.text = "¡Bienvenido, Usuario!"
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager
                if (fragmentManager.backStackEntryCount > 0) {
                    fragmentManager.popBackStack()
                    restoreWelcomeView()
                } else {
                    showLogoutDialog()
                }
            }
        })
    }

    private fun showFragment(fragment: Fragment) {
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.recyclingBlock).visibility = android.view.View.GONE
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.menuBlock).visibility = android.view.View.GONE
        bienvenidaTextView.visibility = android.view.View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun restoreWelcomeView() {
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.recyclingBlock).visibility = android.view.View.VISIBLE
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.menuBlock).visibility = android.view.View.VISIBLE
        bienvenidaTextView.visibility = android.view.View.VISIBLE
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }
}
