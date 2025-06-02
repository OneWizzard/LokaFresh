package com.example.lokafresh

data class ReturItem(
    val id: Int,
    val order_number: String,
    val name: String,
    val unitPrice: Int,
    val quantity: Double,
    var returnQty: Double,
    val store_name: String,
    val username: String,
    val is_signed: Boolean
)

data class ReturResponse(
    val message: String
)