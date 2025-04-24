package com.example.lokafresh

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

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

        return view
    }
}
