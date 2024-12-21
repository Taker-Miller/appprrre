package com.rrr.apprrre.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rrr.apprrre.R
import com.rrr.apprrre.models.Comment

class CommentsAdapter(
    private val comments: List<Comment>,
    private val currentUserId: String,
    private val onRate: (Comment, Float) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentAuthor: TextView = view.findViewById(R.id.commentAuthor)
        val commentText: TextView = view.findViewById(R.id.commentText)
        val commentRating: RatingBar = view.findViewById(R.id.commentRating)
        val commentRatingPercentage: TextView = view.findViewById(R.id.commentRatingPercentage)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.commentAuthor.text = comment.author
        holder.commentText.text = comment.text
        holder.commentRating.rating = comment.rating.toFloat()
        holder.commentRatingPercentage.text = String.format("%.2f", comment.rating)

        // Mostrar el botón de eliminar solo si el comentario pertenece al usuario actual
        if (comment.userId == currentUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDelete(position)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }

        // Configurar la calificación solo si el usuario no es el autor y aún no ha calificado
        val canRate = comment.userId != currentUserId && !comment.ratedBy.contains(currentUserId)
        holder.commentRating.isEnabled = canRate

        holder.commentRating.setOnRatingBarChangeListener { _, rating, _ ->
            if (canRate) {
                onRate(comment, rating)
                holder.commentRating.isEnabled = false
            }
        }
    }

    override fun getItemCount() = comments.size
}
