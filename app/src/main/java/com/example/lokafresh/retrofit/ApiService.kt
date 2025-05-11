package com.example.lokafresh.retrofit

import com.example.lokafresh.response.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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

    // Upload scanned document
    @POST("webhook/doc-scan")
    fun uploadScan(@Body imageListRequest: ImageListRequest): Call<ResponseBody>

    // Chatbot prompt
    @POST("webhook/prompt")
    fun getChatbotResponse(@Body request: ChatbotRequest): Call<ChatbotResponse>


    // =======================
    // DO (Delivery Order) Endpoints
    // =======================

    @POST("webhook/check-do")
    fun checkDo(@Body request: OrderIdRequest): Call<GenericResponse>

    @GET("webhook/get-all-data")
    fun getDoData(): Call<List<DoData>>


    @POST("webhook/create-do")
    fun createDo(@Body request: CreateDoRequest): Call<GenericResponse>

    @DELETE("webhook/delete-do")
    fun deleteDo(@Query("order_id") orderId: String): Call<GenericResponse>

    @PUT("webhook/update-do")
    fun updateDo(@Body request: UpdateDoRequest): Call<GenericResponse>
}
