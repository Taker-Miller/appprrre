package com.rrr.apprrre.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rrr.apprrre.R
import com.rrr.apprrre.models.MenuItem

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.nameTextView.text = menuItem.name
        holder.imageView.setImageResource(menuItem.imageResource)

        holder.itemView.setOnClickListener {
            onItemClick(menuItem)
        }
    }
    override fun getItemCount(): Int = menuItems.size

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.menuItemName)
        val imageView: ImageView = view.findViewById(R.id.menuItemImage)
    }
}
