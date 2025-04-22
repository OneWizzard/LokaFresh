package com.example.lokafresh.retrofit

import com.example.lokafresh.ChatbotResponse
import com.example.lokafresh.response.User
import com.example.lokafresh.response.UsernameCheckResponse
import com.example.lokafresh.response.UsernameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // Create user
    @POST("webhook/create-user")
    fun createUser(@Body user: User): Call<Void>

    // Get all user data
    @GET("webhook/get-user-data")
    fun getUserData(): Call<List<User>>

    // Check if username is available
    @POST("webhook/check-username")
    fun checkUsername(@Body request: UsernameRequest): Call<UsernameCheckResponse>

    // Update user
    @POST("webhook/update-user")
    fun updateUser(@Body user: User): Call<Void>

    // Delete user
    @DELETE("webhook/delete-user")
    fun deleteUser(@Body request: UsernameRequest): Call<Void>

    @GET("webhook/receiveprompt")
    fun getChatbotResponse(
        @Query("key") key: String,
        @Query("prompt") prompt: String,
        @Query("nama") nama: String
    ): Call<ChatbotResponse>
}