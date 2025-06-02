package com.example.lokafresh

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileFragment : Fragment() {

    private lateinit var tvJumlahPesanan: TextView
    private lateinit var tvSudahDiantar: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(profileImageView)


                sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
            }
        }

        val fullname = sharedPreferences.getString("fullname", "Pengguna")
        view.findViewById<TextView>(R.id.tv_greeting).text = "Hi, $fullname"

        tvJumlahPesanan = view.findViewById(R.id.tv_jumlah_pesanan)
        tvSudahDiantar = view.findViewById(R.id.tv_sudah_diantar)
        profileImageView = view.findViewById(R.id.iv_profile)

        // Ambil URI gambar dari SharedPreferences
        val savedImageUri = sharedPreferences.getString("profile_image_uri", null)
        if (!savedImageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(savedImageUri))
                .circleCrop()
                .error(R.drawable.baseline_person_24)
                .into(profileImageView)

        } else {
            profileImageView.setImageResource(R.drawable.baseline_person_24)
        }

        // Klik gambar profil untuk memilih gambar baru
        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        view.findViewById<FloatingActionButton>(R.id.fab_logout).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Apakah Anda yakin ingin logout?")
                .setCancelable(false)
                .setPositiveButton("Ya") { _, _ ->
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(requireContext(), Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val orderPrefs = requireContext().getSharedPreferences("order_counts", Context.MODE_PRIVATE)
        val totalOrders = orderPrefs.getInt("total_orders", 0)
        val totalDelivered = orderPrefs.getInt("total_delivered", 0)

        tvJumlahPesanan.text = totalOrders.toString()
        tvSudahDiantar.text = totalDelivered.toString()
    }
}
