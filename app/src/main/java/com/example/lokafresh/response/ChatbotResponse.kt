package com.example.lokafresh.response

import kotlinx.serialization.Serializable

@Serializable
data class ChatbotResponse(
    val responses: List<InnerResponse>
) {
    @Serializable
    data class InnerResponse(
        val response: String
    )
}