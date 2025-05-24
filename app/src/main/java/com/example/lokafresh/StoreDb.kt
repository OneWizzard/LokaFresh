package com.example.lokafresh

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.retrofit.ApiConfig
import okhttp3.ResponseBody
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

        storeAdapter = StoreAdapter(
            storeList,
            onUpdateClick = { store -> showStoreDialog(isUpdate = true, store = store) },
            onDeleteClick = { store -> deleteStore(store.id) }
        )
        recyclerView.adapter = storeAdapter

        val fab = view.findViewById<View>(R.id.fabAddStore)
        fab.setOnClickListener {
            showStoreDialog()
        }

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
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<StoreData>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showStoreDialog(isUpdate: Boolean = false, store: StoreData? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_store_input, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNama)
        val etLink = dialogView.findViewById<EditText>(R.id.etLink)

        if (isUpdate && store != null) {
            etNama.setText(store.nama)
            etLink.setText(store.link)
            etNama.isEnabled = false // Nama tidak diubah saat update
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isUpdate) "Update Store" else "Tambah Store")
            .setView(dialogView)
            .setPositiveButton(if (isUpdate) "Update" else "Tambah") { _, _ ->
                val nama = etNama.text.toString()
                val link = etLink.text.toString()
                if (nama.isNotBlank() && link.isNotBlank()) {
                    if (isUpdate) updateStore(nama, link)
                    else createStore(nama, link)
                } else {
                    Toast.makeText(requireContext(), "Field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createStore(nama: String, link: String) {
        ApiConfig.getApiService().createStore(nama, link)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Store berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        fetchStores()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menambahkan store", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateStore(nama: String, link: String) {
        ApiConfig.getApiService().updateStore(nama, link)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Store berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        fetchStores()
                    } else {
                        Toast.makeText(requireContext(), "Gagal update store", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteStore(Id: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus store ini?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Menghapus store...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                ApiConfig.getApiService().deleteStore(Id)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            progressDialog.dismiss()
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Store berhasil dihapus", Toast.LENGTH_SHORT).show()
                                fetchStores()
                            } else {
                                Toast.makeText(requireContext(), "Gagal menghapus store", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    }
