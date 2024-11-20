package com.example.midterm_proj.data

data class WeatherResponse(
    val coord: Coord,
    val name: String, // City name
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Sys(
    val sunrise: Long, // Unix timestamp for sunrise
    val sunset: Long   // Unix timestamp for sunset
)

