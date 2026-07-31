package com.example.petadopt.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset

class GigaChatRiskAssessmentTest {

    @Test
    fun normalizeRecommendationText_repairsLegacyMojibake() {
        val mojibake = String(
            "Рекомендуется одобрить заявку".toByteArray(Charsets.UTF_8),
            Charset.forName("windows-1251")
        )

        assertEquals("Рекомендуется одобрить", normalizeRecommendationText(mojibake))
    }

    @Test
    fun normalizeRecommendationText_translatesCanonicalCode() {
        assertEquals("Одобрить с условиями", normalizeRecommendationText("APPROVE_WITH_CONDITIONS"))
    }
}
