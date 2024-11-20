package com.example.midterm_proj

import FavoritesManager
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.midterm_proj.data.AirQualityResponse
import com.example.midterm_proj.data.FavoriteCity
import com.example.midterm_proj.data.WeatherResponse
import com.example.midterm_proj.databinding.ActivityCitySearchBinding
import com.example.midterm_proj.network.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CitySearchActivity : ComponentActivity() {

    private lateinit var binding: ActivityCitySearchBinding
    private lateinit var favoritesManager: FavoritesManager
    private var searchedCityName: String? = null
    private var searchedCityTemperature: Double? = null

    private val apiKey = "5fdfe74706adf27b7c1c90d432144d10"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoritesManager = FavoritesManager(this)


        //add fav button
        binding.addFavoriteButton.setOnClickListener {
            Log.d("AddFavorite", "City: $searchedCityName, Temp: $searchedCityTemperature")

            searchedCityName?.let { cityName ->
                searchedCityTemperature?.let { temperature ->
                    val city = FavoriteCity(cityName, temperature)
                    favoritesManager.addFavoriteCity(city)
                    Toast.makeText(this, "$cityName added to favorites!", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "Temperature not available", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "City name not available", Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (checkLocationPermission()) {
            fetchCurrentLocationWeather()
        } else {
            requestLocationPermission()
        }


        binding.searchButton.setOnClickListener {
            val cityName = binding.citySearchBox.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        binding.openFavoritesButton.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
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
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                fetchWeatherDataForLocation(lat, lon)
            } ?: run {
                Toast.makeText(this, "Failed to retrieve location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWeatherUI(weather: WeatherResponse) {
        binding.cityName.text = weather.name
        binding.temperature.text = "${weather.main.temp}°C"
        binding.weatherDescription.text = weather.weather[0].description.replaceFirstChar { it.uppercase() }

        // Optional additional details
        binding.humidityValue.text = "${weather.main.humidity}%"
        binding.windSpeedValue.text = "${weather.wind.speed} km/h"
        binding.pressureValue.text = "${weather.main.pressure} hPa"
    }

    private fun fetchWeatherDataForLocation(lat: Double, lon: Double) {
        RetrofitInstance.api.getWeatherByCoordinates(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!

                    // Update UI with weather data
                    binding.currentCityName.text = "City: ${weather.name}"
                    binding.currentTemperature.text = "Temperature: ${weather.main.temp}°C"
                    binding.currentWeatherDescription.text = "Description: ${weather.weather[0].description}"
                    binding.currentLocationTitle.text = "${weather.name} Weather"

                    Log.d("WeatherAPI", "Current Location Weather: ${weather.name}, Temp: ${weather.main.temp}")

                    fetchAirQuality(lat, lon)
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
        // Disable buttons while fetching data to prevent multiple clicks
        disableButtons()

        RetrofitInstance.api.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                // Always re-enable buttons regardless of success or failure
                enableButtons()

                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!

                    // Update the UI with weather data
                    binding.cityName.text = weather.name
                    binding.temperature.text = "${weather.main.temp}°C"
                    binding.weatherDescription.text = weather.weather[0].description.replaceFirstChar { it.uppercase() }
                    binding.humidityValue.text = "${weather.main.humidity}%"
                    binding.windSpeedValue.text = "${weather.wind.speed} km/h"
                    binding.pressureValue.text = "${weather.main.pressure} hPa"

                    // Assign data for Add to Favorites functionality
                    searchedCityName = weather.name
                    searchedCityTemperature = weather.main.temp

                    Log.d("WeatherAPI", "Searched City Weather: ${weather.name}, Temp: ${weather.main.temp}")

                    // Fetch AQI data for the location
                    fetchAirQuality(weather.coord.lat, weather.coord.lon)
                } else {
                    Toast.makeText(this@CitySearchActivity, "City not found", Toast.LENGTH_SHORT).show()
                    Log.e("WeatherAPI", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Always re-enable buttons regardless of success or failure
                enableButtons()

                Toast.makeText(this@CitySearchActivity, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                Log.e("WeatherAPI", "API call failure: ${t.message}")
            }
        })
    }

    // Disable buttons to prevent multiple clicks
    private fun disableButtons() {
        binding.searchButton.isEnabled = false
        binding.addFavoriteButton.isEnabled = false
        // Add other buttons if needed
    }

    // Re-enable buttons after the API call finishes
    private fun enableButtons() {
        binding.searchButton.isEnabled = true
        binding.addFavoriteButton.isEnabled = true
        // Add other buttons if needed
    }


    private fun fetchAirQuality(lat: Double, lon: Double) {
        RetrofitInstance.api.getAirQuality(lat, lon, apiKey).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(call: Call<AirQualityResponse>, response: Response<AirQualityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val aqi = response.body()!!.list[0].main.aqi
                    updateAQIUI(aqi)
                } else {
                    Toast.makeText(this@CitySearchActivity, "Error fetching AQI data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                Toast.makeText(this@CitySearchActivity, "Failed to fetch AQI data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAQIUI(aqi: Int) {
        val aqiDescription = when (aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very Poor"
            else -> "Unknown"
        }
        findViewById<TextView>(R.id.aqiTextView).text = "Air Quality: $aqiDescription"
    }

}
