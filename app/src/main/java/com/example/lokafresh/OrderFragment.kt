package com.example.lokafresh

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.DoData
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderFragment : Fragment(), OrderItemAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderItemAdapter
    private val dataList = mutableListOf<ListItem>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrderItemAdapter(dataList, this)
        recyclerView.adapter = adapter


        val sharedPreferences = requireContext().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        username?.let {
            fetchDoDataByUsername(it)
        }

        return view
    }

    private fun fetchDoDataByUsername(username: String) {
        val apiService = ApiConfig.getApiService()
        apiService.getDoUser(username).enqueue(object : Callback<List<DoData>> {
            override fun onResponse(call: Call<List<DoData>>, response: Response<List<DoData>>) {
                if (response.isSuccessful && response.body() != null) {
                    val doList = response.body()!!
                    dataList.clear()
                    for (item in doList) {
                        val listItem = ListItem(
                            icon = R.drawable.baseline_location_pin_24,
                            title = item.order_number,
                            subtitle = item.destination,
                            description = "Tanggal: ${item.date}, Delivered: ${item.delivered == 1}"
                        )
                        dataList.add(listItem)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("OrderFragment", "Gagal mendapatkan data DO: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DoData>>, t: Throwable) {
                Log.e("OrderFragment", "Error fetch DO: ${t.message}")
            }
        })
    }

    override fun onItemClick(position: Int) {
        if (position in dataList.indices) {
            val item = dataList[position]

            if (item.isChecked) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Batalkan Penandaan?")
                    .setMessage("Apakah kamu ingin membatalkan penandaan \"${item.title}\"?")
                    .setPositiveButton("Ya") { _, _ ->
                        item.isChecked = false
                        adapter.notifyItemChanged(position)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Tandai Order?")
                    .setMessage("Apakah kamu ingin menandai \"${item.title}\" sebagai selesai?")
                    .setPositiveButton("Ya") { _, _ ->
                        item.isChecked = true
                        adapter.notifyItemChanged(position)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }
}