package com.rrr.apprrre.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rrr.apprrre.R
import com.rrr.apprrre.models.Comment

class CommentsAdapter(
    private val comments: List<Comment>,
    private val currentUserId: String,
    private val onRate: (Comment, Float) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentAuthor: TextView = view.findViewById(R.id.commentAuthor)
        val commentText: TextView = view.findViewById(R.id.commentText)
        val commentRating: RatingBar = view.findViewById(R.id.commentRating)
        val commentRatingPercentage: TextView = view.findViewById(R.id.commentRatingPercentage)
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

        // Deshabilitar calificación si el usuario actual es el autor del comentario o ya lo calificó
        val canRate = comment.authorId != currentUserId && !comment.ratedBy.contains(currentUserId)
        holder.commentRating.isEnabled = canRate

        holder.commentRating.setOnRatingBarChangeListener { _, rating, _ ->
            if (canRate) {
                onRate(comment, rating)
            }
        }
    }

    override fun getItemCount() = comments.size
}
