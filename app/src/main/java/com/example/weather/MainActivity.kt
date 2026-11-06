package com.example.weather

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.weather.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подключаем разметку через DataBindingUtil
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Задаём стартовые значения по умолчанию
        binding.weather = WeatherData()

        // Слушатель выбора города из выпадающего списка (Spinner)
        binding.spinnerCities.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCity = parent?.getItemAtPosition(position).toString()
                binding.etCustomCity.setText("")
                loadWeather(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Кнопка обновления погоды (берёт город из EditText, если он введён, иначе — из Spinner)
        binding.btnLoadWeather.setOnClickListener {
            val customCity = binding.etCustomCity.text.toString().trim()
            val cityToQuery = if (customCity.isNotEmpty()) {
                customCity
            } else {
                binding.spinnerCities.selectedItem.toString()
            }
            loadWeather(cityToQuery)
        }
    }

    private fun loadWeather(city: String) {
        val apiKey = getString(R.string.api_key).trim()

        // Показываем статус загрузки
        binding.weather = WeatherData(
            cityName = city,
            description = "Запрос к серверу..."
        )

        CoroutineScope(Dispatchers.IO).launch {
            val weatherResult = fetchWeatherFromApi(city, apiKey)

            withContext(Dispatchers.Main) {
                // Обновляем привязанный объект и принудительно обновляем UI
                binding.weather = weatherResult
                binding.executePendingBindings()
            }
        }
    }

    private fun fetchWeatherFromApi(city: String, apiKey: String): WeatherData {
        return try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val weatherUrl =
                "https://api.openweathermap.org/data/2.5/weather?q=$encodedCity&units=metric&lang=ru&appid=$apiKey"

            val url = URL(weatherUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
            }

            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_OK) {
                InputStreamReader(connection.inputStream, "UTF-8").use { reader ->
                    val response = gson.fromJson(reader, OpenWeatherResponse::class.java)
                    val desc = response.weather.firstOrNull()?.description
                        ?.replaceFirstChar { it.uppercase() } ?: "Нет данных"

                    WeatherData(
                        cityName = response.name,
                        temperature = "${response.main.temp.roundToInt()} °C",
                        feelsLike = "Ощущается как: ${response.main.feels_like.roundToInt()} °C",
                        description = desc,
                        humidity = "Влажность: ${response.main.humidity}%",
                        windSpeed = "Ветер: ${response.wind.speed} м/с",
                        pressure = "Давление: ${response.main.pressure} гПа"
                    )
                }
            } else {
                // Читаем текст ошибки от сервера OpenWeatherMap
                val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "Код $code"
                android.util.Log.e("WEATHER_API", "HTTP $code: $errorText")

                val reason = when (code) {
                    401 -> "Ошибка 401: Неверный или неактивный API ключ в strings.xml"
                    404 -> "Ошибка 404: Город '$city' не найден"
                    else -> "Ошибка сервера: код $code"
                }
                WeatherData(cityName = city, description = reason)
            }
        } catch (e: Exception) {
            android.util.Log.e("WEATHER_API", "Exception", e)
            WeatherData(
                cityName = city,
                description = "Ошибка сети: ${e.javaClass.simpleName}\n(${e.localizedMessage})"
            )
        }
    }
}