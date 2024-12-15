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
import java.util.*

class OtrosResiduosFragment : Fragment() {

    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private val imageList = mutableListOf<String>()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference
    private val imagesCollection = firestore.collection("otros_residuos_images")

    companion object {
        private const val REQUEST_IMAGE_PICK = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_otros_residuos, container, false)

        uploadButton = view.findViewById(R.id.uploadImageButton)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerViewImages)

        progressBar.visibility = View.GONE
        adapter = ImagesAdapter(imageList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        uploadButton.setOnClickListener {
            openGallery()
        }

        loadImagesForCurrentUser()
        return view
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
        val fileName = "otros_residuos/${UUID.randomUUID()}.jpg"
        val imageRef = storageReference.child("$userId/$fileName")

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
        val imageData = hashMapOf(
            "url" to imageUrl,
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
                for (document in documents) {
                    val imageUrl = document.getString("url")
                    imageUrl?.let { imageList.add(it) }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar las imágenes", Toast.LENGTH_SHORT).show()
            }
    }
}
