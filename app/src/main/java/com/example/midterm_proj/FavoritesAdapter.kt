package com.example.midterm_proj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.midterm_proj.data.FavoriteCity

class FavoritesAdapter(
    private val favoriteCities: MutableList<FavoriteCity>,
    private val onRemoveClicked: (FavoriteCity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityNameTextView: TextView = itemView.findViewById(R.id.cityNameTextView)
        val cityTemperatureTextView: TextView = itemView.findViewById(R.id.cityTemperatureTextView)
        val removeFavoriteButton: Button = itemView.findViewById(R.id.removeFavoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_city, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val city = favoriteCities[position]

        holder.cityNameTextView.text = city.cityName
        holder.cityTemperatureTextView.text = "${city.temperature}°C"
        holder.removeFavoriteButton.setOnClickListener {
            onRemoveClicked(city)
        }
    }

    override fun getItemCount(): Int = favoriteCities.size

    fun removeCity(city: FavoriteCity) {
        val position = favoriteCities.indexOf(city)
        if (position != -1) {
            favoriteCities.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
