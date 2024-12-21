package com.rrr.apprrre.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rrr.apprrre.R

class ImagesAdapter(
    private val images: List<String>,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var selectedPosition: Int = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        val imageUrl = images[currentPosition]

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView)
            holder.imageView.visibility = View.VISIBLE
        } else {
            holder.imageView.visibility = View.GONE
        }

        holder.itemView.setBackgroundResource(
            if (currentPosition == selectedPosition) R.drawable.selected_image_border else R.drawable.default_image_border
        )

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = currentPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onImageClick(currentPosition)
        }
    }

    override fun getItemCount(): Int = images.size

    fun getSelectedPosition(): Int = selectedPosition
}
