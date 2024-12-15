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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rrr.apprrre.R
import com.rrr.apprrre.adapters.ImagesAdapter
import java.util.*

class CartonPapelFragment : Fragment() {

    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private val imageList = mutableListOf<String>()

    private val storageReference =
        FirebaseStorage.getInstance().reference.child("carton_papel")
    private val firestore = FirebaseFirestore.getInstance()
    private val imageCollection = firestore.collection("carton_papel_images")

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_carton_papel, container, false)

        uploadButton = view.findViewById(R.id.uploadImageButtonCartonPapel)
        progressBar = view.findViewById(R.id.progressBarCartonPapel)
        recyclerView = view.findViewById(R.id.recyclerViewCartonPapel)

        progressBar.visibility = View.GONE
        adapter = ImagesAdapter(imageList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        uploadButton.setOnClickListener {
            openGallery()
        }

        loadImagesFromFirestore()
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
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    uploadImageToFirebase(imageUri)
                }
            } else if (data?.data != null) {
                val imageUri = data.data!!
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storageReference.child(fileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString())
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val imageData = hashMapOf(
            "url" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        imageCollection.add(imageData)
            .addOnSuccessListener {
                loadImagesFromFirestore()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al guardar la URL", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImagesFromFirestore() {
        progressBar.visibility = View.VISIBLE
        imageCollection.orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                imageList.clear()
                for (document in result) {
                    val url = document.getString("url")
                    url?.let { imageList.add(it) }
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
