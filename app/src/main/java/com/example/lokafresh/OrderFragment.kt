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

    private lateinit var emptyTextView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrderItemAdapter(dataList, this)
        recyclerView.adapter = adapter

        emptyTextView = view.findViewById(R.id.tv_empty)

        val sharedPreferences = requireContext().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences.getString("username", null)
        if (!loggedInUsername.isNullOrEmpty()) {
            fetchDeliveryOrders(loggedInUsername)
        }

        return view
    }

    private fun fetchDeliveryOrders(username: String) {
        val apiService = ApiConfig.getApiService()
        apiService.getDoUser(username).enqueue(object : Callback<List<DoData>> {
            override fun onResponse(call: Call<List<DoData>>, response: Response<List<DoData>>) {
                if (response.isSuccessful) {
                    response.body()?.let { orders ->
                        dataList.clear()
                        orders.forEach { doData ->
                            val orderNumber = doData.order_number ?: ""
                            val destination = doData.destination ?: ""

                            // Jika salah satu dari data penting kosong/null, skip item ini
                            if (orderNumber.isNotBlank() && destination.isNotBlank()) {
                                val listItem = ListItem(
                                    icon = R.drawable.baseline_location_pin_24,
                                    title = "Order #: $orderNumber",
                                    subtitle = "Tujuan: $destination",
                                    description = if (doData.delivered == 1) "Status: Sudah dikirim" else "Status: Belum dikirim"
                                )
                                dataList.add(listItem)
                            }
                        }
                        adapter.notifyDataSetChanged()
                        if (dataList.isEmpty()) {
                            emptyTextView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyTextView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.e("OrderFragment", "Gagal ambil DO: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DoData>>, t: Throwable) {
                Log.e("OrderFragment", "Error API DO: ${t.message}")
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