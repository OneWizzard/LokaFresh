package com.example.lokafresh

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.CreateDoRequest
import com.example.lokafresh.response.DoData
import com.example.lokafresh.response.DoDataResponse
import com.example.lokafresh.response.GenericResponse
import com.example.lokafresh.response.UpdateDoRequest
import com.example.lokafresh.retrofit.ApiConfig
import com.example.lokafresh.retrofit.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class OrderDb : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var apiService: ApiService
    private lateinit var fabAddOrder: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_db, container, false)

        recyclerView = view.findViewById(R.id.orderRecyclerView)
        fabAddOrder = view.findViewById(R.id.fabAddOrder)

        apiService = ApiConfig.getApiService()

        setupRecyclerView()
        fetchOrders()

        fabAddOrder.setOnClickListener {
            createOrder()
        }

        return view
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(listOf(), ::deleteOrder, ::updateOrder)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = orderAdapter
    }

    private fun fetchOrders() {
        Log.d("OrderDb", "Fetching orders...")

        apiService.getDoData().enqueue(object : Callback<List<DoData>> {
            override fun onResponse(call: Call<List<DoData>>, response: Response<List<DoData>>) {
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    Log.d("OrderDb", "Fetched orders: $orders")
                    orderAdapter.updateData(orders)
                } else {
                    Log.e("OrderDb", "Error Response: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<DoData>>, t: Throwable) {
                Log.e("OrderDb", "Failed to fetch data: ${t.message}")
            }
        })
    }


    private fun createOrder() {
        val request = CreateDoRequest(
            order_id = UUID.randomUUID().toString(),
            order_number = "ORD-${System.currentTimeMillis()}",
            username = "userA",
            destination = "Jakarta",
            delivered = false
        )
        apiService.createDo(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                fetchOrders()
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("CreateOrder", "Error: ${t.message}")
            }
        })
    }

    private fun deleteOrder(order: DoData) {
        apiService.deleteDo(order.order_id).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                fetchOrders()
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("DeleteOrder", "Error: ${t.message}")
            }
        })
    }

    private fun updateOrder(order: DoData) {
        val request = UpdateDoRequest(
            order_id = order.order_id,
            order_number = order.order_number,
            username = order.username,
            destination = order.destination,
            delivered = !order.delivered,
            date = "2025-05-11" // atau pakai SimpleDateFormat untuk tanggal sekarang
        )
        apiService.updateDo(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                fetchOrders()
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("UpdateOrder", "Error: ${t.message}")
            }
        })
    }
}
