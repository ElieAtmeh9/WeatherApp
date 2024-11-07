package com.example.midterm_proj.data

data class WeatherResponse(
    val name: String, // City name
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

