package com.example.lokafresh

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboard : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        progressBar = findViewById(R.id.progress_bar)
        bottomNav = findViewById(R.id.bottom_nav_view)

        if (savedInstanceState == null) {
            showLoading(true)
            loadFragment(OrderDb())
        }

        bottomNav.setOnItemSelectedListener { item ->
            showLoading(true)
            when (item.itemId) {
                R.id.nav_order -> {
                    loadFragment(OrderDb())
                    true
                }
                R.id.nav_store -> {
                    loadFragment(StoreDb())
                    true
                }
                R.id.user -> {
                    loadFragment(UserDb())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .runOnCommit {
                showLoading(false) // Sembunyikan loading saat fragment selesai dimuat
            }
            .commit()
    }

    private fun showLoading(state: Boolean) {
        progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}
