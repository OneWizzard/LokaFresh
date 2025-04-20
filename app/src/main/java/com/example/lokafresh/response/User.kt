package com.example.lokafresh.response

data class User(
    val username: String,
    val password: String,
    val fullname: String
)

data class UsernameRequest(
    val username: String
)

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)