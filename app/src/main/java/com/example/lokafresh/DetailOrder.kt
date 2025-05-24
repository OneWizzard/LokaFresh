package com.example.lokafresh

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.ItemData
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailOrder : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderDetailAdapter
    private val orderItems = mutableListOf<ItemData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_order, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewDetail)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrderDetailAdapter(orderItems)
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val orderNumber = arguments?.getString("order_number") ?: run {
            Log.e("DetailOrder", "order_number argument missing")
            return
        }

        fetchOrderDetails(orderNumber)
    }

    private fun fetchOrderDetails(orderNumber: String) {
        val apiService = ApiConfig.getApiService()
        apiService.getDoItem(orderNumber).enqueue(object : Callback<List<ItemData>> {
            override fun onResponse(
                call: Call<List<ItemData>>,
                response: Response<List<ItemData>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    orderItems.clear()
                    orderItems.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("DetailOrder", "Failed to get data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ItemData>>, t: Throwable) {
                Log.e("DetailOrder", "Error fetching order details: ${t.message}")
            }
        })
    }
}
