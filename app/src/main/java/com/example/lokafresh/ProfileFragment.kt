package com.example.lokafresh


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileFragment : Fragment() {

    private var tvJumlahPesanan: TextView? = null
    private var tvSudahDiantar: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Ambil fullname dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val fullname = sharedPreferences.getString("fullname", "Pengguna")

        // Ganti teks greeting
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        greetingTextView.text = "Hi, $fullname"

        tvJumlahPesanan = view.findViewById(R.id.tv_jumlah_pesanan)
        tvSudahDiantar = view.findViewById(R.id.tv_sudah_diantar)

        val logoutButton = view.findViewById<FloatingActionButton>(R.id.fab_logout)
        logoutButton.setOnClickListener {
            // Konfirmasi logout
            AlertDialog.Builder(requireContext())
                .setMessage("Apakah Anda yakin ingin logout?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    // Clear SharedPreferences
                    sharedPreferences.edit().clear().apply()

                    // Arahkan ke LoginActivity
                    val intent = Intent(requireContext(), Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferencesOrder = requireContext().getSharedPreferences("order_counts", Context.MODE_PRIVATE)
        val totalOrders = sharedPreferencesOrder.getInt("total_orders", 0)
        val totalDelivered = sharedPreferencesOrder.getInt("total_delivered", 0)

        tvJumlahPesanan?.text = totalOrders.toString()
        tvSudahDiantar?.text = totalDelivered.toString()
    }
}

