import android.content.Context
import android.content.SharedPreferences
import com.example.midterm_proj.data.FavoriteCity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save a favorite city to SharedPreferences
    fun addFavoriteCity(city: FavoriteCity) {
        val cities = getFavoriteCities().toMutableList()  // Get existing cities
        cities.add(city)  // Add the new city
        saveCitiesList(cities)  // Save the updated list back to SharedPreferences
    }

    // Get the list of favorite cities from SharedPreferences
    fun getFavoriteCities(): List<FavoriteCity> {
        val json = sharedPreferences.getString("favorite_cities", null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteCity>>() {}.type
        return gson.fromJson(json, type)
    }

    // Remove a favorite city by name
    fun removeFavoriteCity(cityName: String) {
        val cities = getFavoriteCities().filterNot { it.cityName == cityName }  // Filter out the city
        saveCitiesList(cities)  // Save the updated list back to SharedPreferences
    }

    // Save the list of cities to SharedPreferences
    private fun saveCitiesList(cities: List<FavoriteCity>) {
        val json = gson.toJson(cities)  // Convert the list to a JSON string
        sharedPreferences.edit().putString("favorite_cities", json).apply()  // Save it to SharedPreferences
    }
}
