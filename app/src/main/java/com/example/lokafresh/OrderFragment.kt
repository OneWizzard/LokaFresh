package com.example.lokafresh

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.DoData
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.response.TspResponse
import com.example.lokafresh.response.UsernameRequest
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class OrderFragment : Fragment(), OrderItemAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderItemAdapter
    private val dataList = mutableListOf<ListItem>()

    private lateinit var emptyTextView: View
    private var storeList: List<StoreData> = listOf()

    private var gmapsLink: String? = null
    private lateinit var progressBar: View

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

        progressBar = view.findViewById(R.id.progressBar)

        val sharedPreferences = requireContext().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences.getString("username", null)
        if (!loggedInUsername.isNullOrEmpty()) {
            fetchStoresAndOrders(loggedInUsername)
        }

        val fabMap = view.findViewById<View>(R.id.fab_map)
        fabMap.setOnClickListener {
            if (!gmapsLink.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(gmapsLink)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Link Google Maps belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchStoresAndOrders(username: String) {
        val apiService = ApiConfig.getApiService()

        // Fetch store data first
        apiService.getAllStoreData().enqueue(object : Callback<List<StoreData>> {
            override fun onResponse(call: Call<List<StoreData>>, response: Response<List<StoreData>>) {
                if (response.isSuccessful && response.body() != null) {
                    storeList = response.body()!!
                    fetchDeliveryOrders(username)
                } else {
                    Log.e("OrderFragment", "Gagal ambil data toko: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<StoreData>>, t: Throwable) {
                Log.e("OrderFragment", "Error fetch store: ${t.message}")
            }
        })
    }

    private fun fetchDeliveryOrders(username: String) {
        progressBar.visibility = View.VISIBLE
        val apiService = ApiConfig.getApiService()

        apiService.getDoUser(username).enqueue(object : Callback<List<DoData>> {
            override fun onResponse(call: Call<List<DoData>>, response: Response<List<DoData>>) {
                if (response.isSuccessful && response.body() != null) {
                    val doList = response.body()!!

                    apiService.getTspRoute(UsernameRequest(username)).enqueue(object : Callback<List<TspResponse>> {
                        override fun onResponse(call: Call<List<TspResponse>>, tspResponse: Response<List<TspResponse>>) {
                            progressBar.visibility = View.GONE
                            if (tspResponse.isSuccessful && !tspResponse.body().isNullOrEmpty()) {
                                val tspData = tspResponse.body()!![0]
                                val recommendedRoute = tspData.recommended_route
                                gmapsLink = tspData.gmaps_link

                                val sortedDoList = recommendedRoute.mapNotNull { routeNum ->
                                    doList.find { doData ->
                                        doData.destination == routeNum.toString()
                                    }
                                }

                                dataList.clear()
                                sortedDoList.forEach { doData ->
                                    val storeName = storeList.find { it.id.toString() == doData.destination }?.nama ?: "Unknown Store"

                                    // ListItem perlu property isChecked (status delivered)
                                    val listItem = ListItem(
                                        icon = R.drawable.baseline_location_pin_24,
                                        title = "Order #: ${doData.order_number}",
                                        subtitle = "Tujuan: $storeName",
                                        description = if (doData.delivered == 1) "Status: Sudah dikirim" else "Status: Belum dikirim",
                                        isChecked = doData.delivered == 1
                                    )
                                    dataList.add(listItem)
                                }

                                adapter.notifyDataSetChanged()

                                emptyTextView.visibility = if (dataList.isEmpty()) View.VISIBLE else View.GONE
                                recyclerView.visibility = if (dataList.isEmpty()) View.GONE else View.VISIBLE

                                updateOrderCountsInPrefs() // Simpan ke SharedPreferences

                            } else {
                                progressBar.visibility = View.GONE
                                Log.e("OrderFragment", "Gagal get TSP: ${tspResponse.code()}")
                            }
                        }

                        override fun onFailure(call: Call<List<TspResponse>>, t: Throwable) {
                            progressBar.visibility = View.GONE
                            Log.e("OrderFragment", "Error TSP: ${t.message}")
                        }
                    })
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
        if (position !in dataList.indices) return
        val item = dataList[position]

        if (item.isChecked) {
            AlertDialog.Builder(requireContext())
                .setTitle("Batalkan Penandaan?")
                .setMessage("Apakah kamu ingin membatalkan penandaan \"${item.title}\"?")
                .setPositiveButton("Ya") { _, _ ->
                    item.isChecked = false
                    item.description = "Status: Belum dikirim"
                    adapter.notifyItemChanged(position)
                    updateOrderCountsInPrefs()
                }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Tandai Order?")
                .setMessage("Apakah kamu ingin menandai \"${item.title}\" sebagai selesai?")
                .setPositiveButton("Ya") { _, _ ->
                    item.isChecked = true
                    item.description = "Status: Sudah dikirim"
                    adapter.notifyItemChanged(position)
                    updateOrderCountsInPrefs()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun updateOrderCountsInPrefs() {
        val totalOrders = dataList.size
        val totalDelivered = dataList.count { it.isChecked }
        val sharedPreferences = requireContext().getSharedPreferences("order_counts", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("total_orders", totalOrders)
            .putInt("total_delivered", totalDelivered)
            .apply()
    }

}
