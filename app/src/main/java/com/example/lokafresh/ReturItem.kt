package com.example.lokafresh

data class ReturItem(
    val id: Int,
    val name: String,
    val unitPrice: Double,
    val quantity: Double,
    var returnQty: Double
)