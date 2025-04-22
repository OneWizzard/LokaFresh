package com.example.lokafresh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog

class OrderFragment : Fragment(), OrderItemAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderItemAdapter

    private val dummyData = mutableListOf(
        ListItem(R.drawable.baseline_person_24, "Warung Makan Bahari", "Seafood • $$ • 0.5 miles away", "Spesialis masakan laut segar dengan harga terjangkau.", false),
        ListItem(R.drawable.baseline_person_24, "Kedai Kopi Senja", "Coffee & Snacks • $ • 1.8 miles away", "Tempat nongkrong asik dengan berbagai pilihan kopi dan camilan.", true),
        ListItem(R.drawable.baseline_person_24, "Pizza Ria", "Pizza • $$$ • 2.1 miles away", "Pizza dengan berbagai topping pilihan, cocok untuk keluarga.", false),
        ListItem(R.drawable.baseline_person_24, "Toko Buku Ilmu", "Books & Stationery • $ • 0.9 miles away", "Menyediakan berbagai macam buku dan alat tulis.", true),
        ListItem(R.drawable.baseline_person_24, "Bengkel Jaya Motor", "Automotive • $$ • 3.5 miles away", "Melayani service dan perbaikan berbagai jenis kendaraan.", false),
        ListItem(R.drawable.baseline_person_24, "Bengkel Jaya Motor", "Automotive • $$ • 3.5 miles away", "Melayani service dan perbaikan berbagai jenis kendaraan.", false),
        ListItem(R.drawable.baseline_person_24, "Bengkel Jaya Motor", "Automotive • $$ • 3.5 miles away", "Melayani service dan perbaikan berbagai jenis kendaraan.", false),
        ListItem(R.drawable.baseline_person_24, "Bengkel Jaya Motor", "Automotive • $$ • 3.5 miles away", "Melayani service dan perbaikan berbagai jenis kendaraan.", false),
        ListItem(R.drawable.baseline_person_24, "Bengkel Jaya Motor", "Automotive • $$ • 3.5 miles away", "Melayani service dan perbaikan berbagai jenis kendaraan.", false)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrderItemAdapter(dummyData, this)
        recyclerView.adapter = adapter
        return view
    }

    override fun onItemClick(position: Int) {
        if (position in dummyData.indices) {
            val item = dummyData[position]

            if (item.isChecked) {
                // Jika sudah ditandai, konfirmasi untuk batalkan
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
                // Jika belum ditandai, konfirmasi untuk tandai
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
