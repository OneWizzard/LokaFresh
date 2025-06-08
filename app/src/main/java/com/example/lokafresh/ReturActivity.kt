package com.example.lokafresh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lokafresh.databinding.FragmentReturBinding
import com.example.lokafresh.retrofit.ApiConfig
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReturActivity : AppCompatActivity() {

    private lateinit var binding: FragmentReturBinding
    private lateinit var adapter: ReturAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentReturBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val responseJson = intent.getStringExtra("scan_response")
        if (responseJson == null) {
            Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        val itemList = parseScanResponse(responseJson, username)
        adapter = ReturAdapter(itemList)
        binding.rvRetur.layoutManager = LinearLayoutManager(this)
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
            binding.checkboxSigned.isChecked = isSigned

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

        apiService.addRetur(items).enqueue(object : Callback<ReturResponse> {
            override fun onResponse(call: Call<ReturResponse>, response: Response<ReturResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Toast.makeText(this@ReturActivity, body?.message ?: "Success", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@ReturActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ReturActivity, "Failed to submit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReturResponse>, t: Throwable) {
                Toast.makeText(this@ReturActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
