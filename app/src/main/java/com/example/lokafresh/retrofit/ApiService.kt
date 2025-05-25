package com.example.lokafresh.retrofit

import com.example.lokafresh.ReturItem
import com.example.lokafresh.ReturResponse
import com.example.lokafresh.response.AddItemRequest
import com.example.lokafresh.response.ChatbotRequest
import com.example.lokafresh.response.ChatbotResponse
import com.example.lokafresh.response.CreateDoRequest
import com.example.lokafresh.response.DoData
import com.example.lokafresh.response.GenericResponse
import com.example.lokafresh.response.ItemData
import com.example.lokafresh.response.OrderIdRequest
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.response.TspResponse
import com.example.lokafresh.response.UpdateDoRequest
import com.example.lokafresh.response.User
import com.example.lokafresh.response.UsernameCheckResponse
import com.example.lokafresh.response.UsernameRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    // Upload scanned document
    @POST("webhook/doc-scan")
    fun uploadScan(@Body imageListRequest: ImageListRequest): Call<ResponseBody>

    // Chatbot prompt
    @POST("webhook/prompt")
    fun getChatbotResponse(@Body request: ChatbotRequest): Call<ChatbotResponse>

    @POST("webhook/get-tsp")
    fun getTspRoute(@Body request: UsernameRequest): Call<List<TspResponse>>

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

    @GET("webhook/get-do-user-data")
    fun getDoUser(@Query("username") username: String): Call<List<DoData>>

    @POST("webhook/add-item")
    fun addItem(@Body request: AddItemRequest): Call<GenericResponse>

    @DELETE("webhook/delete-item")
    fun deleteItem(
        @Query("order_id") orderId: String,
        @Query("nama") nama: String
    ): Call<GenericResponse>

    @GET("webhook/get-list-items")
    fun getListItems(): Call<List<ItemData>>

    @GET("webhook/get-do-item")
    fun getDoItem(@Query("order_id") orderId: String): Call<List<ItemData>>




    //==========================
    //STORE ENDPOINTS
    //==========================

    @GET("webhook/get-all-store-data")
    fun getAllStoreData(): Call<List<StoreData>>

    @POST("webhook/create-store")
    @FormUrlEncoded
    fun createStore(

        @Field("nama") nama: String,
        @Field("link") link: String

    ): Call<ResponseBody>

    @DELETE("webhook-test/delete-store")
    fun deleteStore(
        @Query("id") nama: Int
    ): Call<ResponseBody>

    @PUT("webhook/update-store")
    @FormUrlEncoded
    fun updateStore(

        @Field("nama") nama: String,
        @Field("link") link: String

    ): Call<ResponseBody>

    @POST("webhook/add-ro")
    fun addRetur(@Body returItems: List<ReturItem>): Call<ReturResponse>
}