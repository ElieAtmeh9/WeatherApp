package com.example.midterm_proj

import FavoritesManager
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.midterm_proj.data.FavoriteCity
import com.example.midterm_proj.data.WeatherResponse
import com.example.midterm_proj.network.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CitySearchActivity : ComponentActivity() {

    private lateinit var favoritesManager: FavoritesManager
    private var searchedCityName: String? = null
    private var searchedCityTemperature: Double? = null

    private val apiKey = "5fdfe74706adf27b7c1c90d432144d10" // Replace with your actual API key
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_search)
        favoritesManager = FavoritesManager(this)

        // Add Favorite Button
        val addButton = findViewById<Button>(R.id.addFavoriteButton)
        addButton.setOnClickListener {
            searchedCityName?.let { cityName ->
                searchedCityTemperature?.let { temperature ->
                    val city = FavoriteCity(cityName, temperature)
                    favoritesManager.addFavoriteCity(city)
                    Toast.makeText(this, "$cityName added to favorites!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Display Favorite Button
        val displayFavoritesButton = findViewById<Button>(R.id.displayFavoritesButton)
        val favoritesTextView = findViewById<TextView>(R.id.favoriteCitiesText)

        displayFavoritesButton.setOnClickListener {
            val favoriteCities = favoritesManager.getFavoriteCities()
            val displayText = if (favoriteCities.isNotEmpty()) {
                favoriteCities.joinToString(separator = "\n") { "${it.cityName}: ${it.temperature}°C" }
            } else {
                "No favorites yet"
            }
            favoritesTextView.text = displayText
        }

        // Remove Favorite Button (Modified)
        val removeButton = findViewById<Button>(R.id.removeFavoriteButton)
        removeButton.setOnClickListener {
            searchedCityName?.let { cityName ->
                favoritesManager.removeFavoriteCity(cityName) // Remove the selected city
                Toast.makeText(this, "$cityName removed from favorites!", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location permissions and fetch current location weather
        if (checkLocationPermission()) {
            fetchCurrentLocationWeather()  // Automatically fetch current location weather
        } else {
            requestLocationPermission()
        }

        // Initialize UI elements
        val searchBox = findViewById<EditText>(R.id.citySearchBox)
        val searchButton = findViewById<Button>(R.id.searchButton)

        // Set click listener on the search button
        searchButton.setOnClickListener {
            val cityName = searchBox.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted && coarseLocationGranted
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            ) {
                fetchCurrentLocationWeather()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun fetchCurrentLocationWeather() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted; should not reach here
            return
        }

        // Retrieve the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                // Fetch weather data based on the current location coordinates
                fetchWeatherDataForLocation(lat, lon)
            } ?: run {
                Toast.makeText(this, "Failed to retrieve location", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun updateWeatherUI(weather: WeatherResponse) {
        findViewById<TextView>(R.id.cityName).text = weather.name
        findViewById<TextView>(R.id.temperature).text = "${weather.main.temp}°C"
        findViewById<TextView>(R.id.weatherDescription).text = weather.weather[0].description.replaceFirstChar { it.uppercase() }

        // Optional additional details
        findViewById<TextView>(R.id.humidityValue)?.text = "${weather.main.humidity}%"
        findViewById<TextView>(R.id.windSpeedValue)?.text = "${weather.wind.speed} km/h"
        findViewById<TextView>(R.id.pressureValue)?.text = "${weather.main.pressure} hPa"
    }




    private fun fetchWeatherDataForLocation(lat: Double, lon: Double) {
        RetrofitInstance.api.getWeatherByCoordinates(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!

                    // Update UI with current location weather
                    findViewById<TextView>(R.id.currentCityName).text = "City: ${weather.name}"
                    findViewById<TextView>(R.id.currentTemperature).text = "Temperature: ${weather.main.temp}°C"
                    findViewById<TextView>(R.id.currentWeatherDescription).text = "Description: ${weather.weather[0].description}"

                    // Update the title to display the city's name
                    findViewById<TextView>(R.id.currentLocationTitle).text = "${weather.name} Weather"

                    Log.d("WeatherAPI", "Current Location Weather: ${weather.name}, Temp: ${weather.main.temp}")
                } else {
                    Toast.makeText(this@CitySearchActivity, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@CitySearchActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchWeatherData(city: String) {
            RetrofitInstance.api.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val weather = response.body()!!
                        searchedCityName = weather.name
                        searchedCityTemperature = weather.main.temp

                        // Update UI with weather data
                        updateWeatherUI(weather)
                    } else {
                        Toast.makeText(this@CitySearchActivity, "City not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@CitySearchActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                }
        })
    }






}
