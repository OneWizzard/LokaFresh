package com.example.lokafresh.response

import com.google.gson.annotations.SerializedName


data class ChatbotResponse(
    @SerializedName("message")
    val output: String?
)

data class Message(
    val text: String,
    val isUser: Boolean
)


data class ChatbotRequest(
    val prompt: String,
    val username: String
)

