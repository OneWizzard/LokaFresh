package com.example.lokafresh.response

data class User(
    val username: String,
    val password: String,
    val fullname: String,
    val email: String
)

data class UsernameRequest(
    val username: String
)

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)

data class OrderIdRequest(
    val order_id: String
)

data class CreateDoRequest(
    val order_id: String,
    val order_number: String,
    val username: String,
    val destination: String,
    val delivered: Boolean
)

data class UpdateDoRequest(
    val order_id: String,
    val order_number: String,
    val username: String,
    val destination: String,
    val delivered: Boolean,
    val date: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String
)

data class DoDataResponse(
    val data: List<DoData>
)

data class DoData(
    val order_id: String,
    val order_number: String,
    val username: String,
    val destination: String,
    val delivered: Boolean,
    val date: String
)
