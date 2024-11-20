package com.example.midterm_proj.data

data class AirQualityResponse(
    val list: List<AirQualityData>
)

data class AirQualityData(
    val main: AirQualityMain
)

data class AirQualityMain(
    val aqi: Int // AQI is represented as an integer from 1 (Good) to 5 (Very Poor)
)

