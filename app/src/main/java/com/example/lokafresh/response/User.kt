package com.example.lokafresh.response
import com.google.gson.annotations.SerializedName
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
    val delivered: Int // ← sebelumnya Boolean
)

data class UpdateDoRequest(
    val order_id: String,
    val order_number: String,
    val username: String,
    val destination: String,
    val delivered: Int, // ← sebelumnya Boolean
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
    val delivered: Int,
    val date: String
)
data class StoreData(
    @SerializedName("id")
    val id: Int,
    val nama: String,
    val link: String

)
