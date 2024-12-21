package com.rrr.apprrre.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rrr.apprrre.R
import com.rrr.apprrre.adapters.ImagesAdapter
import com.rrr.apprrre.adapters.MenuAdapter
import com.rrr.apprrre.models.MenuItem
import java.util.*

class VidrioFragment : Fragment() {

    private lateinit var uploadButton: Button
    private lateinit var deleteButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private val imageList = mutableListOf<String>()
    private val documentIdList = mutableListOf<String>()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val imagesCollection = firestore.collection("vidrio_images")

    companion object {
        private const val REQUEST_IMAGE_PICK = 1004
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_vidrio, container, false)

        uploadButton = view.findViewById(R.id.uploadImageButtonVidrio)
        deleteButton = view.findViewById(R.id.deleteImageButtonVidrio)
        progressBar = view.findViewById(R.id.progressBarVidrio)
        recyclerView = view.findViewById(R.id.recyclerViewVidrio)
        menuRecyclerView = view.findViewById(R.id.menuRecyclerView)

        setupMenu()
        setupRecyclerView()

        uploadButton.setOnClickListener {
            openGallery()
        }

        deleteButton.setOnClickListener {
            val selectedPosition = adapter.getSelectedPosition()
            if (selectedPosition != -1) {
                deleteImage(selectedPosition)
            } else {
                Toast.makeText(requireContext(), "Selecciona una imagen para eliminar.", Toast.LENGTH_SHORT).show()
            }
        }

        loadImagesForCurrentUser()
        return view
    }

    private fun setupMenu() {
        menuRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val menuItems = listOf(
            MenuItem("Latas", R.drawable.ic_lata),
            MenuItem("Vidrio", R.drawable.ic_vidrio),
            MenuItem("Plástico", R.drawable.ic_plastico),
            MenuItem("Otros Residuos", R.drawable.ic_residuos),
            MenuItem("Cartón-Papel", R.drawable.ic_papel),
            MenuItem("Reutilización", R.drawable.ic_reutilizar),
            MenuItem("Comunidad", R.drawable.ic_comunidad)
        )

        val menuAdapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.name) {
                "Latas" -> showFragment(LatasFragment())
                "Vidrio" -> showFragment(VidrioFragment())
                "Plástico" -> showFragment(PlasticoFragment())
                "Otros Residuos" -> showFragment(OtrosResiduosFragment())
                "Cartón-Papel" -> showFragment(CartonPapelFragment())
                "Reutilización" -> showFragment(ReutilizacionFragment())
                "Comunidad" -> showFragment(ComunidadFragment())
            }
        }

        menuRecyclerView.adapter = menuAdapter
    }

    private fun setupRecyclerView() {
        progressBar.visibility = View.GONE
        adapter = ImagesAdapter(imageList) { position -> deleteImage(position) }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun showFragment(fragment: Fragment) {
        parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            progressBar.visibility = View.VISIBLE
            val userId = auth.currentUser?.uid ?: return

            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    uploadImageToFirebase(imageUri, userId)
                }
            } else if (data?.data != null) {
                val imageUri = data.data!!
                uploadImageToFirebase(imageUri, userId)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, userId: String) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val folderName = "vidrio"
        val imageRef = FirebaseStorage.getInstance().reference.child("$userId/$folderName/$fileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString(), userId)
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String, userId: String) {
        val imageData = mapOf(
            "imageUrl" to imageUrl,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        imagesCollection.add(imageData)
            .addOnSuccessListener {
                loadImagesForCurrentUser()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al guardar la URL de la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImagesForCurrentUser() {
        val userId = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        imagesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                imageList.clear()
                documentIdList.clear()
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        imageList.add(imageUrl)
                        documentIdList.add(document.id)
                    }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar las imágenes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImage(position: Int) {
        val documentId = documentIdList[position]
        val imageUrl = imageList[position]

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.delete()
            .addOnSuccessListener {
                imagesCollection.document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        imageList.removeAt(position)
                        documentIdList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(requireContext(), "Imagen eliminada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar la imagen de la base de datos.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar la imagen del almacenamiento.", Toast.LENGTH_SHORT).show()
            }
    }
}
