package com.example.midterm_proj
import android.os.Bundle
import androidx.activity.ComponentActivity

import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.midterm_proj.data.WeatherResponse
import com.example.midterm_proj.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CitySearchActivity : ComponentActivity() {
    private val apiKey = "5fdfe74706adf27b7c1c90d432144d10"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_search)

        val searchBox = findViewById<EditText>(R.id.citySearchBox)
        val searchButton = findViewById<Button>(R.id.searchButton)

        // Set click listener on the search button
        searchButton.setOnClickListener {
            val cityName = searchBox.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }// Link to the city search layout XML
    }
    private fun fetchWeatherData(city: String) {
        RetrofitInstance.api.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    navigateToCityWeather(response.body()!!)
                } else {
                    Toast.makeText(this@CitySearchActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@CitySearchActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun navigateToCityWeather(weather: WeatherResponse) {
        val intent = Intent(this, City_Weather::class.java).apply {
            putExtra("cityName", weather.name)
            putExtra("temperature", weather.main.temp)
            putExtra("description", weather.weather[0].description)
            putExtra("humidity", weather.main.humidity)
            putExtra("windSpeed", weather.wind.speed)
            putExtra("pressure", weather.main.pressure)
        }
        startActivity(intent)
        finish() // Close CitySearchActivity
    }
}