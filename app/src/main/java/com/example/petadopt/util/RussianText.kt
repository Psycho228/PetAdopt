package com.example.petadopt.util

import kotlin.math.abs

fun formatYears(value: Int): String {
    val normalized = abs(value)
    val lastTwoDigits = normalized % 100
    val lastDigit = normalized % 10
    val word = when {
        lastTwoDigits in 11..14 -> "лет"
        lastDigit == 1 -> "год"
        lastDigit in 2..4 -> "года"
        else -> "лет"
    }
    return "$value $word"
}
