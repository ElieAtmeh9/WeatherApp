package com.example.midterm_proj

import FavoritesManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.midterm_proj.data.FavoriteCity

class FavoritesActivity : AppCompatActivity() {
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var favoriteCities: MutableList<FavoriteCity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        favoriteCities = FavoritesManager(this).getFavoriteCities().toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        favoritesAdapter = FavoritesAdapter(favoriteCities) { city ->
            removeCityFromFavorites(city)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = favoritesAdapter
    }

    private fun removeCityFromFavorites(city: FavoriteCity) {
        FavoritesManager(this).removeFavoriteCity(city.cityName)
        favoritesAdapter.removeCity(city)
        Toast.makeText(this, "${city.cityName} removed from favorites!", Toast.LENGTH_SHORT).show()

        // Show empty state if no favorites left
        if (favoriteCities.isEmpty()) {
            findViewById<TextView>(R.id.emptyFavoritesTextView).visibility = View.VISIBLE
        }
    }
}