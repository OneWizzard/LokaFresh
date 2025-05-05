package com.example.lokafresh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileFragment : Fragment() {

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

        val logoutButton = view.findViewById<FloatingActionButton>(R.id.fab_logout)
        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply()

            // Arahkan ke LoginActivity
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        return view
    }
}
