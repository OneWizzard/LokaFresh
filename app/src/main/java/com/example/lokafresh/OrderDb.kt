package com.example.lokafresh

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.*
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

    private var userList: List<User> = listOf()
    private var storeList: List<StoreData> = listOf()

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
        fetchUsersAndStores()

        fabAddOrder.setOnClickListener {
            showAddOrderDialog()
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

    private fun fetchUsersAndStores() {
        apiService.getUserData().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    userList = response.body() ?: listOf()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("OrderDb", "Failed to load users: ${t.message}")
            }
        })

        apiService.getAllStoreData().enqueue(object : Callback<List<StoreData>> {
            override fun onResponse(call: Call<List<StoreData>>, response: Response<List<StoreData>>) {
                if (response.isSuccessful) {
                    storeList = response.body() ?: listOf()
                }
            }

            override fun onFailure(call: Call<List<StoreData>>, t: Throwable) {
                Log.e("OrderDb", "Failed to load stores: ${t.message}")
            }
        })
    }

    private fun showAddOrderDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null)

        val edtOrderNumber = dialogView.findViewById<EditText>(R.id.edtOrderNumber)
        val spinnerUsername = dialogView.findViewById<Spinner>(R.id.spinnerUsername)
        val spinnerDestination = dialogView.findViewById<Spinner>(R.id.spinnerDestination)

        // Set up Username Spinner
        val usernames = userList.map { it.username }
        val usernameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, usernames)
        usernameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUsername.adapter = usernameAdapter

        // Set up Store Spinner
        val storeNames = storeList.map { it.nama }
        val storeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)
        storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDestination.adapter = storeAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Order")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val orderNumber = edtOrderNumber.text.toString()
                val username = spinnerUsername.selectedItem.toString()
                val storeName = spinnerDestination.selectedItem.toString()

                val selectedStore = storeList.find { it.nama == storeName }
                val storeId = selectedStore?.id?.toString() ?: ""


                if (orderNumber.isNotEmpty()) {
                    val request = CreateDoRequest(
                        order_id = UUID.randomUUID().toString(),
                        order_number = orderNumber,
                        username = username,
                        destination = storeId,
                        delivered = 0
                    )

                    apiService.createDo(request).enqueue(object : Callback<GenericResponse> {
                        override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                            fetchOrders()
                            Toast.makeText(context, "Order ditambahkan", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            Toast.makeText(context, "Gagal menambahkan order", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Order number tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()

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
            delivered = order.delivered,
            date = "2025-05-11"
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
