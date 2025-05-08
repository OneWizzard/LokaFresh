package com.example.lokafresh.retrofit

import com.example.lokafresh.response.ChatbotRequest
import com.example.lokafresh.response.ChatbotResponse
import com.example.lokafresh.response.User
import com.example.lokafresh.response.UsernameCheckResponse
import com.example.lokafresh.response.UsernameRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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
    @PUT("webhook/update-user")
    fun updateUser(@Body user: User): Call<Void>


    // Delete user
    @DELETE("webhook/delete-user")
    fun deleteUser(@Query("username") username: String): Call<Void>


    @POST("webhook/doc-scan")
    fun uploadScan(@Body imageListRequest: ImageListRequest): Call<ResponseBody>


    @POST("webhook/prompt")
    fun getChatbotResponse(@Body request: ChatbotRequest): Call<ChatbotResponse>

    }

