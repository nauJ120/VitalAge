package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.clases.MenuOption

class MenuAdapter(
    private val options: List<MenuOption>,
    private val onClick: (MenuOption) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    // ViewHolder: Representa cada ítem del RecyclerView
    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tv_option_title)
        private val icon: ImageView = view.findViewById(R.id.iv_option_icon)

        fun bind(option: MenuOption) {
            title.text = option.title
            icon.setImageResource(option.icon)
            itemView.setOnClickListener { onClick(option) }
        }
    }

    // Infla el diseño del ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_option, parent, false)
        return MenuViewHolder(view)
    }

    // Vincula los datos con cada ViewHolder
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(options[position])
    }

    // Retorna el número de ítems
    override fun getItemCount(): Int = options.size
}
