package com.example.weather

data class OpenWeatherResponse(
    val name: String,
    val main: MainInfo,
    val weather: List<WeatherInfo>,
    val wind: WindInfo
)

data class MainInfo(
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int
)

data class WeatherInfo(
    val main: String,
    val description: String,
    val icon: String
)

data class WindInfo(
    val speed: Double
)