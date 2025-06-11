package com.example.lokafresh

import OrderAdapter
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.AddItemRequest
import com.example.lokafresh.response.CreateDoRequest
import com.example.lokafresh.response.DoData
import com.example.lokafresh.response.GenericResponse
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.response.User
import com.example.lokafresh.response.itemsData
import com.example.lokafresh.retrofit.ApiConfig
import com.example.lokafresh.retrofit.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class OrderDb : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var apiService: ApiService
    private lateinit var fabAddOrder: FloatingActionButton

    private var userList: List<User> = listOf()
    private var storeList: List<StoreData> = listOf()
    private var isUserReady = false
    private var isStoreReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_db, container, false)

        recyclerView = view.findViewById(R.id.orderRecyclerView)
        fabAddOrder = view.findViewById(R.id.fabAddOrder)

        apiService = ApiConfig.getApiService()

        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchUsersAndStores()

        fabAddOrder.setOnClickListener {
            showAddOrderDialog()
        }

        return view
    }

    private fun fetchUsersAndStores() {
        isUserReady = false
        isStoreReady = false

        apiService.getUserData().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                Log.d("OrderDb", "getUserData response code: ${response.code()}")
                if (response.isSuccessful) {
                    userList = response.body() ?: listOf()
                    Log.d("OrderDb", "getUserData response body: $userList")
                    isUserReady = true
                    if (isStoreReady) fetchOrders()
                } else {
                    Log.e("OrderDb", "getUserData failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("OrderDb", "Failed to load users: ${t.message}")
            }
        })

        apiService.getAllStoreData().enqueue(object : Callback<List<StoreData>> {
            override fun onResponse(call: Call<List<StoreData>>, response: Response<List<StoreData>>) {
                Log.d("OrderDb", "getAllStoreData response code: ${response.code()}")
                if (response.isSuccessful) {
                    storeList = response.body() ?: listOf()
                    Log.d("OrderDb", "getAllStoreData response body: $storeList")
                    isStoreReady = true
                    if (isUserReady) fetchOrders()
                } else {
                    Log.e("OrderDb", "getAllStoreData failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<StoreData>>, t: Throwable) {
                Log.e("OrderDb", "Failed to load stores: ${t.message}")
            }
        })
    }

    private fun fetchOrders() {
        apiService.getDoData().enqueue(object : Callback<List<DoData>> {
            override fun onResponse(call: Call<List<DoData>>, response: Response<List<DoData>>) {
                Log.d("OrderDb", "getDoData response code: ${response.code()}")
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    Log.d("OrderDb", "getDoData response body: $orders")
                    if (!::orderAdapter.isInitialized) {
                        orderAdapter = OrderAdapter(
                            orders,
                            storeList,
                            ::deleteOrder,
                            ::updateOrder,
                            ::openDetailFragment
                        )
                        recyclerView.adapter = orderAdapter
                    } else {
                        orderAdapter.updateData(orders, storeList)
                    }
                } else {
                    Log.e("OrderDb", "getDoData failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DoData>>, t: Throwable) {
                Log.e("OrderDb", "Failed to fetch orders: ${t.message}")
            }
        })
    }

    private fun openDetailFragment(order: DoData) {
        val detailFragment = DetailItemFragment()
        val bundle = Bundle()
        bundle.putString("order_id", order.order_id)
        detailFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showAddOrderDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null)

        val edtOrderNumber = dialogView.findViewById<EditText>(R.id.edtOrderNumber)
        val spinnerUsername = dialogView.findViewById<Spinner>(R.id.spinnerUsername)
        val spinnerDestination = dialogView.findViewById<Spinner>(R.id.spinnerDestination)

        val usernames = userList.map { it.username }
        val usernameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, usernames)
        usernameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUsername.adapter = usernameAdapter

        val storeNames = storeList.map { it.nama }
        val storeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)
        storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDestination.adapter = storeAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Order")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    btnSave.setOnClickListener {
                        val orderNumber = edtOrderNumber.text.toString()
                        val username = spinnerUsername.selectedItem?.toString() ?: ""
                        val storeName = spinnerDestination.selectedItem?.toString() ?: ""

                        val selectedStore = storeList.find { it.nama == storeName }
                        val storeId = selectedStore?.id?.toString() ?: ""

                        if (orderNumber.isNotEmpty()) {
                            val newOrderId = UUID.randomUUID().toString()
                            val request = CreateDoRequest(
                                order_id = newOrderId,
                                order_number = orderNumber,
                                username = username,
                                destination = storeId,
                                delivered = 0
                            )

                            apiService.createDo(request).enqueue(object : Callback<GenericResponse> {
                                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                                    Log.d("OrderDb", "createDo response code: ${response.code()}")
                                    if (response.isSuccessful) {
                                        Log.d("OrderDb", "createDo response body: ${response.body()}")
                                        Toast.makeText(context, "Order ditambahkan", Toast.LENGTH_SHORT).show()
                                        fetchOrders()
                                        dialog.dismiss()
                                        showAddItemDialog(newOrderId)
                                    } else {
                                        Log.e("OrderDb", "createDo failed with code: ${response.code()}")
                                        Toast.makeText(context, "Gagal menambahkan order", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                    Log.e("OrderDb", "createDo failure: ${t.message}")
                                    Toast.makeText(context, "Gagal menambahkan order", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else {
                            Toast.makeText(context, "Order number tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun showAddItemDialog(orderId: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)

        val spinnerItems = dialogView.findViewById<Spinner>(R.id.spinnerItems)
        val edtQuantity = dialogView.findViewById<EditText>(R.id.edtQuantity)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Tambah Barang")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Selesai", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnSave.isEnabled = false

            apiService.getListItems().enqueue(object : Callback<List<itemsData>> {
                override fun onResponse(call: Call<List<itemsData>>, response: Response<List<itemsData>>) {
                    Log.d("OrderDb", "getListItems response code: ${response.code()}")
                    if (response.isSuccessful) {
                        val items = response.body() ?: emptyList()
                        Log.d("OrderDb", "getListItems response body: $items")
                        if (items.isEmpty()) {
                            Toast.makeText(context, "Daftar barang kosong", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            return
                        }

                        val itemNames = items.map { it.nama  ?: "Nama tidak tersedia" }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerItems.adapter = adapter

                        spinnerItems.tag = items

                        btnSave.isEnabled = true
                        btnSave.isEnabled = true
                    } else {
                        Log.e("OrderDb", "getListItems failed with code: ${response.code()}")
                        Toast.makeText(context, "Gagal mengambil daftar barang", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }

                override fun onFailure(call: Call<List<itemsData>>, t: Throwable) {
                    Log.e("OrderDb", "getListItems failure: ${t.message}")
                    Toast.makeText(context, "Gagal mengambil daftar barang: ${t.message}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })

            btnSave.setOnClickListener {
                val quantityStr = edtQuantity.text.toString().trim()
                val quantity = quantityStr.toDoubleOrNull()

                if (quantity == null || quantity <= 0) {
                    Toast.makeText(context, "Jumlah barang harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val items = spinnerItems.tag as? List<itemsData>
                if (items.isNullOrEmpty()) {
                    Toast.makeText(context, "Data barang belum tersedia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedIndex = spinnerItems.selectedItemPosition
                if (selectedIndex < 0 || selectedIndex >= items.size) {
                    Toast.makeText(context, "Pilih barang yang valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedItem = items[selectedIndex]
                val itemRequest = AddItemRequest(
                    order_id = orderId,
                    nama = selectedItem.nama,
                    quantity = quantity
                )

                apiService.addItem(itemRequest).enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        Log.d("OrderDb", "addItem response code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("OrderDb", "addItem response body: ${response.body()}")
                            Toast.makeText(context, "Barang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            edtQuantity.text.clear()
                        } else {
                            Log.e("OrderDb", "addItem failed with code: ${response.code()}")
                            Toast.makeText(context, "Gagal menambahkan barang", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Log.e("OrderDb", "addItem failure: ${t.message}")
                        Toast.makeText(context, "Gagal menambahkan barang: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
                fetchOrders()
            }
        }

        dialog.show()
    }

    private fun deleteOrder(order: DoData) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus order ini?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Menghapus order...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                apiService.deleteDo(order.order_number).enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        progressDialog.dismiss()
                        Log.d("OrderDb", "deleteDo response code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("OrderDb", "deleteDo response body: ${response.body()}")
                            Toast.makeText(context, "Order berhasil dihapus", Toast.LENGTH_SHORT).show()
                            fetchOrders()
                        } else {
                            Log.e("OrderDb", "deleteDo failed with code: ${response.code()}")
                            Toast.makeText(context, "Gagal menghapus order", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        Log.e("OrderDb", "deleteDo failure: ${t.message}")
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateOrder(order: DoData) {
        // Implement update order jika perlu
    }
}
