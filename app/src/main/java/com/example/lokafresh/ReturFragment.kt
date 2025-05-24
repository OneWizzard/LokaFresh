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
import org.json.JSONArray
import org.json.JSONObject

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
        val itemList = mutableListOf<ReturItem>()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            itemList.add(
                ReturItem(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    unitPrice = item.getDouble("unit_price"),
                    quantity = item.getDouble("quantity"),
                    returnQty = 0.0 // default user input
                )
            )
        }

        return itemList
    }

    private fun sendReturDataToBackend(items: List<ReturItem>) {
        val requestBody = JSONObject()
        val itemArray = JSONArray()

        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("return_quantity", item.returnQty)
            itemArray.put(obj)
        }

        requestBody.put("items", itemArray)

        // Implement your Retrofit call here
        Log.d("Retur", "Sending retur: $requestBody")
    }
}
