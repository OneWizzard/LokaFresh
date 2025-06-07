package com.example.lokafresh

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.StoreData
import com.example.lokafresh.response.CariStoreResponse
import com.example.lokafresh.retrofit.ApiConfig
import okhttp3.ResponseBody
import org.json.JSONObject
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
    ): View = inflater.inflate(R.layout.fragment_store_db, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rvStores)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        storeAdapter = StoreAdapter(
            storeList,
            onUpdateClick = { store -> showStoreDialog(isUpdate = true, store = store) },
            onDeleteClick = { store -> deleteStore(store.id) }
        )
        recyclerView.adapter = storeAdapter

        view.findViewById<View>(R.id.fabAddStore).setOnClickListener {
            showFirstDialog()
        }

        fetchStores()
    }

    private fun fetchStores() {
        ApiConfig.getApiService().getAllStoreData().enqueue(object : Callback<List<StoreData>> {
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

    private fun showFirstDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_store_nama, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNama)

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Store")
            .setView(dialogView)
            .setPositiveButton("Lanjut") { _, _ ->
                val nama = etNama.text.toString().trim()
                if (nama.isNotBlank()) {
                    showSecondDialog(nama)
                } else {
                    Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showSecondDialog(nama: String) {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Mencari store...")
            setCancelable(false)
            show()
        }

        ApiConfig.getApiService().cariStore(nama).enqueue(object : Callback<List<CariStoreResponse>> {
            override fun onResponse(call: Call<List<CariStoreResponse>>, response: Response<List<CariStoreResponse>>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (!body.isNullOrEmpty()) {
                        val data = body[0]
                        Log.d("showSecondDialog", "Response success: $data")

                        val scrollView = ScrollView(requireContext())
                        val layout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(30, 30, 30, 30)
                        }

                        val tvNama = TextView(requireContext()).apply { text = "Nama: ${data.store_name}" }
                        val tvAlamat = TextView(requireContext()).apply { text = "Alamat: ${data.formatted_address}" }
                        val tvLink = TextView(requireContext()).apply { text = "Maps Link: ${data.maps_link}" }

                        layout.addView(tvNama)
                        layout.addView(tvAlamat)
                        layout.addView(tvLink)
                        scrollView.addView(layout)

                        AlertDialog.Builder(requireContext())
                            .setTitle("Hasil Pencarian Store")
                            .setView(scrollView)
                            .setPositiveButton("Tambah") { _, _ ->
                                createStore(data.store_name, data.latitude, data.longitude)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Store tidak ditemukan untuk nama \"$nama\"", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorBody ?: "")
                        val message = jsonObject.optString("message", "Terjadi kesalahan.")

                        when (message.lowercase()) {
                            "store already registered!" -> {
                                Toast.makeText(requireContext(), "Alamat sudah didaftarkan", Toast.LENGTH_SHORT).show()
                            }
                            "alamat dari nama toko tidak ditemukan!" -> {
                                Toast.makeText(requireContext(), "Alamat dari nama toko tidak ditemukan!", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Gagal memproses respon error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<CariStoreResponse>>, t: Throwable) {
                progressDialog.dismiss()
                val message = if (t.message?.contains("Unable to resolve host", true) == true ||
                    t.message?.contains("Failed to connect", true) == true) {
                    "Tidak dapat terhubung ke server, periksa koneksi internet Anda."
                } else {
                    "Alamat dari nama toko tidak ditemukan!"
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createStore(nama: String, latitude: Double, longitude: Double) {
        ApiConfig.getApiService().createStore(nama, latitude, longitude)
            .enqueue(object : Callback<StoreData> {
                override fun onResponse(call: Call<StoreData>, response: Response<StoreData>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        Log.d("createStore", "Store berhasil dibuat: ${response.body()}")
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Store berhasil ditambahkan", Toast.LENGTH_LONG).show()
                        }
                        val newStore = response.body()
                        if (newStore != null) {
                            storeAdapter.addStore(newStore)
                            recyclerView.scrollToPosition(0)

                            // Optional: delay fetchStores kalau mau reload data
                            // Handler(Looper.getMainLooper()).postDelayed({ fetchStores() }, 1000)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal menambahkan store", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StoreData>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showStoreDialog(isUpdate: Boolean = false, store: StoreData? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_store_result, null)
        val etNama = dialogView.findViewById<EditText>(R.id.tvNama)
        val etLink = dialogView.findViewById<EditText>(R.id.tvLink)
        val etAlamat = dialogView.findViewById<EditText>(R.id.tvAlamat)

        if (isUpdate && store != null) {
            etNama.setText(store.nama)
            etNama.isEnabled = false
            etLink.setText(store.link)
            etAlamat.setText(store.alamat)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isUpdate) "Update Store" else "Tambah Store")
            .setView(dialogView)
            .setPositiveButton(if (isUpdate) "Update" else "Tambah") { _, _ ->
                val nama = etNama.text.toString()
                val alamat = etAlamat.text.toString()
                val link = etLink.text.toString()

                if (nama.isNotBlank() && alamat.isNotBlank() && link.isNotBlank()) {
                    if (isUpdate) updateStore(nama, alamat, link)
                    else Toast.makeText(requireContext(), "Gunakan tombol FAB untuk tambah store", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStore(nama: String, alamat: String, linkMaps: String) {
        ApiConfig.getApiService().updateStore(nama, alamat, linkMaps).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!isAdded) return
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Store berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    fetchStores()
                } else {
                    Toast.makeText(requireContext(), "Gagal update store", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteStore(id: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus store ini?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage("Menghapus store...")
                    setCancelable(false)
                    show()
                }

                ApiConfig.getApiService().deleteStore(id).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progressDialog.dismiss()
                        if (!isAdded) return
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Store berhasil dihapus", Toast.LENGTH_SHORT).show()
                            fetchStores()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menghapus store", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        if (!isAdded) return
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
