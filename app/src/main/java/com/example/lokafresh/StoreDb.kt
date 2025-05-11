package com.example.lokafresh

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoreDb : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private var storeList = mutableListOf<StoreData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store_db, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rvStores)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        storeAdapter = StoreAdapter(storeList,
            onUpdateClick = { store -> /* TODO: Implement update */ },
            onDeleteClick = { store -> deleteStore(store.id) }
        )
        recyclerView.adapter = storeAdapter

        fetchStores()
    }

    private fun fetchStores() {
        val client = ApiConfig.getApiService().getAllStoreData()
        client.enqueue(object : Callback<List<StoreData>> {
            override fun onResponse(call: Call<List<StoreData>>, response: Response<List<StoreData>>) {
                if (response.isSuccessful) {
                    storeList.clear()
                    response.body()?.let { storeList.addAll(it) }
                    storeAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<StoreData>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch store data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteStore(id: Int) {
        // TODO: Panggil endpoint delete di sini
        Toast.makeText(requireContext(), "Delete store with ID $id", Toast.LENGTH_SHORT).show()
    }
}
