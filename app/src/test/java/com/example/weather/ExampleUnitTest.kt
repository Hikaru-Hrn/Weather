package com.example.weather

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun tempAndWindFormatting_isCorrect() {
        assertEquals("0 °C", MainActivity.formatTemp(0.0, false))
        assertEquals("32 °F", MainActivity.formatTemp(0.0, true))
        assertEquals("С (0°)", MainActivity.degToCompass(0))
        assertEquals("В (90°)", MainActivity.degToCompass(90))
        assertEquals("СЗ (315°)", MainActivity.degToCompass(315))
    }
}