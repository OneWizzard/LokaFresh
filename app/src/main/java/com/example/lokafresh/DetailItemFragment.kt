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

class DetailItemFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DetailItemAdapter
    private val itemList = mutableListOf<ItemData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_item, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewDetail)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DetailItemAdapter(itemList)
        recyclerView.adapter = adapter

        val orderId = arguments?.getString("order_id")
        if (!orderId.isNullOrEmpty()) {
            fetchOrderItems(orderId)
        }

        return view
    }

    private fun fetchOrderItems(orderId: String) {
        val apiService = ApiConfig.getApiService()
        apiService.getDoItem(orderId).enqueue(object : Callback<List<ItemData>> {
            override fun onResponse(call: Call<List<ItemData>>, response: Response<List<ItemData>>) {
                if (response.isSuccessful && response.body() != null) {
                    itemList.clear()
                    itemList.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("DetailItemFragment", "Failed to load items: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ItemData>>, t: Throwable) {
                Log.e("DetailItemFragment", "Error loading items: ${t.message}")
            }
        })
    }
}
