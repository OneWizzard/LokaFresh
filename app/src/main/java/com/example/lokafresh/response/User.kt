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
    val order_number: String,
    val username: String,
    val destination: String,
    val delivered: Int, // ← sebelumnya Boolean
)

data class UpdateDoResponse(
    val message: String
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
    val date: String,
    val is_signed: Int
)

data class StoreData(
    @SerializedName("id")
    val id: Int,
    val nama: String,
    val alamat: String,
    val link: String

)

data class TspResponse(
    val recommended_route: List<Int>,
    val gmaps_link: String
)

data class AddItemRequest(
    val order_id: String,
    val nama: String,
    val quantity: Double,
    val total_price: Int = 0,
    val weight: Int = 0,
    val unit_price: Int = 0,
    val unit_metrics: String = "pcs"
)


data class ItemData(
    val order_id: String,
    val name: String,
    val quantity: Double,
    val total_price: Int,
    val weight: Double,
    val unit_price: Int,
    val unit_metrics: String
)


data class itemsData(
    val nama: String,
    val unit_metrics: String,
    val price: Int,
)
data class ListItem(
    val icon: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val isChecked: Boolean,
    val orderId: String

)
data class OrderItemDetail(
    val name: String,
    val quantity: Int
)
data class CariStoreResponse(
    val store_name: String,
    val formatted_address: String,
    val latitude: Double,
    val longitude: Double,
    val maps_link: String
)

data class CreateStoreRequest(
    val store_name: String,
    val latitude: Double,
    val longitude: Double
)




