package com.example.weather

data class WeatherData(
    val cityName: String = "Иркутск",
    val temperature: String = "-- °C",
    val feelsLike: String = "Ощущается как: -- °C",
    val description: String = "Загрузка данных...",
    val humidity: String = "Влажность: --%",
    val windSpeed: String = "Ветер: -- м/с",
    val pressure: String = "Давление: -- гПа"
)