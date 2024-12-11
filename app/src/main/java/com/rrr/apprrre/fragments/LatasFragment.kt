package com.rrr.apprrre.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.rrr.apprrre.R

class LatasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_latas, container, false)

        // Referenciar vistas si es necesario
        val titleTextView: TextView = view.findViewById(R.id.latasTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.latasDescription)

        // Configurar contenido
        titleTextView.text = "Reciclaje de Latas"
        descriptionTextView.text = "El reciclaje de latas de aluminio y otros metales es esencial para reducir la contaminación y ahorrar recursos. Reciclar latas requiere un 95% menos de energía que producirlas desde materias primas."

        return view
    }
}
