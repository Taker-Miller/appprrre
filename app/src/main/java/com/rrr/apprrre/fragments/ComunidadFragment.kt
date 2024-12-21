package com.rrr.apprrre.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rrr.apprrre.R
import com.rrr.apprrre.adapters.CommentsAdapter
import com.rrr.apprrre.adapters.MenuAdapter
import com.rrr.apprrre.models.Comment
import com.rrr.apprrre.models.MenuItem

class ComunidadFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var adapter: CommentsAdapter
    private val commentsCollection by lazy { firestore.collection("comments") }
    private var commentsList = mutableListOf<Comment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_comunidad, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.commentsRecyclerView)
        menuRecyclerView = view.findViewById(R.id.menuRecyclerView)
        val addCommentButton: Button = view.findViewById(R.id.addCommentButton)
        val commentInput: EditText = view.findViewById(R.id.commentInput)

        adapter = CommentsAdapter(
            commentsList,
            auth.currentUser?.uid ?: "",
            onRate = { comment, rating ->
                val currentUserId = auth.currentUser?.uid
                if (comment.userId == currentUserId) {
                    Toast.makeText(requireContext(), "No puedes calificar tu propio comentario.", Toast.LENGTH_SHORT).show()
                } else if (currentUserId in comment.ratedBy) {
                    Toast.makeText(requireContext(), "Ya calificaste este comentario.", Toast.LENGTH_SHORT).show()
                } else {
                    updateRating(comment, rating, currentUserId!!)
                }
            },
            onDelete = { position ->
                deleteComment(position)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        addCommentButton.setOnClickListener {
            val text = commentInput.text.toString()
            if (text.isNotEmpty()) {
                addComment(text)
                commentInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Escribe un comentario.", Toast.LENGTH_SHORT).show()
            }
        }

        setupMenu()
        loadComments()

        return view
    }

    private fun setupMenu() {
        val menuItems = listOf(
            MenuItem("Latas", R.drawable.ic_lata),
            MenuItem("Vidrio", R.drawable.ic_vidrio),
            MenuItem("Plástico", R.drawable.ic_plastico),
            MenuItem("Otros Residuos", R.drawable.ic_residuos),
            MenuItem("Cartón y Papel", R.drawable.ic_papel),
            MenuItem("Reutilización", R.drawable.ic_reutilizar),
            MenuItem("Comunidad", R.drawable.ic_comunidad)
        )

        menuRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        menuRecyclerView.adapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.name) {
                "Latas" -> showFragment(LatasFragment())
                "Vidrio" -> showFragment(VidrioFragment())
                "Plástico" -> showFragment(PlasticoFragment())
                "Otros Residuos" -> showFragment(OtrosResiduosFragment())
                "Cartón y Papel" -> showFragment(CartonPapelFragment())
                "Reutilización" -> showFragment(ReutilizacionFragment())
                "Comunidad" -> showFragment(ComunidadFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadComments() {
        commentsCollection.get().addOnSuccessListener { documents ->
            commentsList.clear()
            for (document in documents) {
                val comment = document.toObject(Comment::class.java).copy(id = document.id)
                commentsList.add(comment)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar los comentarios.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addComment(text: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("usuarios").document(userId).get().addOnSuccessListener { document ->
                val authorName = document.getString("nombre") ?: "Anónimo"
                val comment = Comment(
                    text = text,
                    author = authorName,
                    userId = userId,
                    ratedBy = mutableListOf()
                )

                commentsCollection.add(comment).addOnSuccessListener {
                    loadComments()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al agregar el comentario.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener el nombre del usuario.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteComment(position: Int) {
        val comment = commentsList[position]
        commentsCollection.document(comment.id).delete().addOnSuccessListener {
            commentsList.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(requireContext(), "Comentario eliminado.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al eliminar el comentario.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRating(comment: Comment, rating: Float, userId: String) {
        val newTotalRatings = comment.totalRatings + 1
        val newAverageRating = ((comment.rating * comment.totalRatings) + rating) / newTotalRatings

        val updatedRatedBy = comment.ratedBy.toMutableList()
        updatedRatedBy.add(userId)

        commentsCollection.document(comment.id)
            .update(
                "rating", newAverageRating,
                "totalRatings", newTotalRatings,
                "ratedBy", updatedRatedBy
            )
            .addOnSuccessListener {
                loadComments()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar la calificación.", Toast.LENGTH_SHORT).show()
            }
    }
}
