package com.example.midterm_proj
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity


class City_Weather : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_weather)

        val cityName = intent.getStringExtra("cityName")
        val temperature = intent.getDoubleExtra("temperature", 0.0)
        val description = intent.getStringExtra("description")
        val humidity = intent.getIntExtra("humidity", 0)
        val windSpeed = intent.getFloatExtra("windSpeed", 0.0f) // Adjusted to float
        val pressure = intent.getIntExtra("pressure", 0)

        // Ensure TextViews are correctly referenced and display data
        findViewById<TextView>(R.id.cityName).text = cityName
        findViewById<TextView>(R.id.temperature).text = "${temperature}°C"
        findViewById<TextView>(R.id.weatherDescription).text = description?.replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.humidityValue).text = "$humidity%"
        findViewById<TextView>(R.id.windSpeedValue).text = "$windSpeed km/h"
        findViewById<TextView>(R.id.pressureValue).text = "$pressure hPa"
    }
}