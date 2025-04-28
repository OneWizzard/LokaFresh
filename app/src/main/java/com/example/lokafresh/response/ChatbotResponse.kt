package com.example.lokafresh.response

data class ChatbotResponse(
    val output: String
)

data class Message(
    val text: String,
    val isUser: Boolean
)


data class ChatbotRequest(
    val prompt: String,
    val username: String
)

