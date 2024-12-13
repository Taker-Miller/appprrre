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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.storage.FirebaseStorage
import com.rrr.apprrre.R
import java.util.*

class LatasFragment : Fragment() {

    private lateinit var uploadButton: Button
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private var selectedImageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference.child("latas")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_latas, container, false)

        uploadButton = view.findViewById(R.id.uploadImageButton)
        imageView = view.findViewById(R.id.selectedImageView)
        progressBar = view.findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        uploadButton.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase() {
        if (selectedImageUri != null) {
            progressBar.visibility = View.VISIBLE
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storageReference.child(fileName)

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Imagen subida exitosamente: $uri", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
