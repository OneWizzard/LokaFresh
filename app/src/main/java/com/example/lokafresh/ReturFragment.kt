package com.example.lokafresh

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lokafresh.databinding.FragmentReturBinding
import com.example.lokafresh.retrofit.ApiConfig
import org.json.JSONObject
import retrofit2.Call

class ReturFragment : Fragment() {

    private lateinit var binding: FragmentReturBinding
    private lateinit var adapter: ReturAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReturBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val responseJson = arguments?.getString("scan_response")
        if (responseJson == null) {
            Toast.makeText(requireContext(), "No data received", Toast.LENGTH_SHORT).show()
            return
        }

        val itemList = parseScanResponse(responseJson)
        adapter = ReturAdapter(itemList)
        binding.rvRetur.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRetur.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            val updatedItems = adapter.getUpdatedItems()
            sendReturDataToBackend(updatedItems)
        }
    }

    private fun parseScanResponse(json: String): List<ReturItem> {
        val jsonObject = JSONObject(json)
        val itemsArray = jsonObject.getJSONArray("items")
        val orderNumber = jsonObject.getString("order_number")
        val itemList = mutableListOf<ReturItem>()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            itemList.add(
                ReturItem(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    unitPrice = item.getDouble("unit_price"),
                    quantity = item.getDouble("quantity"),
                    returnQty = 0.0, // default user input
                    order_number = orderNumber
                )
            )
        }

        return itemList
    }

    private fun sendReturDataToBackend(items: List<ReturItem>) {
        val apiService = ApiConfig.getApiService()

        apiService.addRetur(items).enqueue(object : retrofit2.Callback<ReturResponse> {
            override fun onResponse(call: Call<ReturResponse>, response: retrofit2.Response<ReturResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Return sent successfully"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    Log.d("Retur", "Success: $message")
                } else {
                    Toast.makeText(requireContext(), "Failed to send return data", Toast.LENGTH_SHORT).show()
                    Log.e("Retur", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ReturResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Retur", "Network error", t)
            }
        })
    }
}
