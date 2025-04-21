package com.example.lokafresh

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.animation.AnimatorSetCompat.playTogether
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class MainActivity : AppCompatActivity(), NavigationVisibilityListener { // Implement the top-level interface
    private val bottomNavigation: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation) }
    private val fabCamera: FloatingActionButton by lazy { findViewById(R.id.fab_camera) }
    private val fabChatbot: FloatingActionButton by lazy { findViewById(R.id.fab_chatbot) }

    // Variabel untuk melacak posisi drag
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        setupBottomNavigation()
        setupFloatingActionButton()
        setupAssistiveFab()
        setInitialFragment()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_order -> replaceFragment(OrderFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                else -> false
            }
        }
    }

    private fun setupFloatingActionButton() {
        fabCamera.setOnClickListener {
            replaceFragment(CameraFragment())
        }
    }

    private fun setupAssistiveFab() {
        fabChatbot.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Simpan posisi awal
                    initialX = view.x
                    initialY = view.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    // Animasi skala saat tombol ditekan
                    val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f)
                    val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f)
                    scaleDownX.duration = 150
                    scaleDownY.duration = 150
                    AnimatorSet().apply {
                        playTogether(scaleDownX, scaleDownY)
                        start()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Hitung perpindahan
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    // Update posisi view
                    view.x = initialX + dx
                    view.y = initialY + dy
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Animasi skala kembali saat tombol dilepas atau dibatalkan
                    val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f)
                    val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f)
                    scaleUpX.duration = 150
                    scaleUpY.duration = 150
                    AnimatorSet().apply {
                        playTogether(scaleUpX, scaleUpY)
                        interpolator = OvershootInterpolator() // Efek "melenting"
                        start()
                    }

                    // Jika pergerakan kurang dari threshold, anggap sebagai klik
                    if (abs(event.rawX - initialTouchX) < 10 &&
                        abs(event.rawY - initialTouchY) < 10) {
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }

        fabChatbot.setOnClickListener {
            // Animasi tambahan saat FAB diklik dan sebelum fragment diganti
            val rotateOut = ObjectAnimator.ofFloat(fabChatbot, "rotation", 0f, 360f)
            rotateOut.duration = 300
            val scaleOutX = ObjectAnimator.ofFloat(fabChatbot, "scaleX", 1f, 0f)
            val scaleOutY = ObjectAnimator.ofFloat(fabChatbot, "scaleY", 1f, 0f)
            scaleOutX.duration = 300
            scaleOutY.duration = 300
            val fadeOut = ObjectAnimator.ofFloat(fabChatbot, "alpha", 1f, 0f)
            fadeOut.duration = 300

            AnimatorSet().apply {
                playTogether(rotateOut, scaleOutX, scaleOutY, fadeOut)
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // Setelah animasi selesai, ganti fragment
                        replaceFragment(ChatbotFragment())

                        // Reset properti FAB setelah animasi selesai dan fragment diganti (opsional)
                        fabChatbot.rotation = 0f
                        fabChatbot.scaleX = 1f
                        fabChatbot.scaleY = 1f
                        fabChatbot.alpha = 1f
                    }
                })
                start()
            }
        }
    }

    private fun setInitialFragment() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(OrderFragment(), false) // Jangan tambahkan Fragment awal ke back stack
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true): Boolean {
        // Sembunyikan bottom nav jika fragment adalah ChatbotFragment
        if (fragment is ChatbotFragment) {
            setNavigationVisibility(false)
        } else {
            setNavigationVisibility(true)
        }

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null) // Tambahkan transaksi ke back stack
        }

        transaction.commit()
        return true
    }

    override fun setNavigationVisibility(visible: Boolean) { // Now correctly overrides the interface method
        bottomNavigation.visibility = if (visible) View.VISIBLE else View.GONE
        fabCamera.visibility = if (visible) View.VISIBLE else View.GONE
        fabChatbot.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed() // Panggil implementasi default untuk keluar aplikasi jika back stack kosong
        }
    }
}