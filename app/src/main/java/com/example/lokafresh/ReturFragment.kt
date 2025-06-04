package com.example.lokafresh

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        (activity as? MainActivity)?.hideNavigationElements()

        val sharedPreferences = requireContext().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        val itemList = parseScanResponse(responseJson, username)
        adapter = ReturAdapter(itemList)
        binding.rvRetur.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRetur.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            val updatedItems = adapter.getUpdatedItems()
            sendReturDataToBackend(updatedItems)
        }
    }


    private fun parseScanResponse(responseJson: String, username: String): List<ReturItem> {
        val itemList = mutableListOf<ReturItem>()

        try {
            val json = JSONObject(responseJson)
            val orderNumber = json.getString("order_number")
            val customerName = json.getJSONObject("customer").getString("name")
            val isSigned = json.getBoolean("is_signed")
            val itemsArray = json.getJSONArray("items")

            // Set teks ke UI
            binding.tvOrderNumber.text = "Order Number : $orderNumber"
            binding.tvCustomerName.text = "Customer : $customerName"
            binding.spinnerSigned.setSelection(if (isSigned) 1 else 0)

            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val fulfillment = item.getJSONObject("fulfillment_quantity")

                val returItem = ReturItem(
                    id = item.getInt("id"),
                    order_number = orderNumber,
                    name = item.getString("name"),
                    unitPrice = item.getInt("unit_price"),
                    quantity = item.getDouble("quantity"),
                    returnQty = 0.0, // Default value
                    store_name = customerName,
                    username = username, // Bisa ambil dari SharedPref kalau perlu
                    is_signed = isSigned,
                )
                itemList.add(returItem)
            }

        } catch (e: Exception) {
            Log.e("ReturFragment", "Failed to parse scan response: ${e.message}")
        }

        return itemList
    }

    private fun sendReturDataToBackend(items: List<ReturItem>) {
        val apiService = ApiConfig.getApiService()

        apiService.addRetur(items).enqueue(object : retrofit2.Callback<ReturResponse> {
            override fun onResponse(call: Call<ReturResponse>, response: retrofit2.Response<ReturResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        Log.d("Retur", "Success: ${body.message}")
                    } else {
                        Toast.makeText(requireContext(), "Return sent, but no message", Toast.LENGTH_SHORT).show()
                        Log.d("Retur", "Success: empty body")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Failed: $errorBody", Toast.LENGTH_SHORT).show()
                    Log.e("Retur", "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ReturResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Retur", "Network error", t)
            }
        })
    }
}
