package com.example.lokafresh.retrofit

data class Base64Data(
    val picture : String,
    val nama : String
)

data class ImageListRequest(
    val imgs: List<String>
)

