package com.example.petadopt.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RussianTextTest {

    @Test
    fun formatYears_usesRussianPluralForms() {
        val expected = mapOf(
            1 to "1 год",
            2 to "2 года",
            4 to "4 года",
            5 to "5 лет",
            11 to "11 лет",
            14 to "14 лет",
            21 to "21 год",
            23 to "23 года",
            25 to "25 лет"
        )

        expected.forEach { (age, text) ->
            assertEquals(text, formatYears(age))
        }
    }
}
