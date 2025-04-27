package com.example.lokafresh

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import kotlin.math.abs

class MainActivity : AppCompatActivity(), NavigationVisibilityListener {

    private val bottomNavigation: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation) }
    private val fabCamera: FloatingActionButton by lazy { findViewById(R.id.fab_camera) }
    private val fabChatbot: FloatingActionButton by lazy { findViewById(R.id.fab_chatbot) }
    private var selectedItemId: Int = R.id.nav_order // Set initial selected item



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
        updateBottomNavigationSelection() // Set initial color
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId != selectedItemId) {
                selectedItemId = item.itemId
                updateBottomNavigationSelection() // Update warna icon
                when (item.itemId) {
                    R.id.nav_order -> replaceFragment(OrderFragment())
                    R.id.nav_profile -> replaceFragment(ProfileFragment())
                    else -> false
                }
            }
            true // Pastikan item yang dipilih tetap diproses
        }
    }

    private fun updateBottomNavigationSelection() {
        for (i in 0 until bottomNavigation.menu.size()) {
            val menuItem = bottomNavigation.menu.getItem(i)

            // Jika item dipilih, beri warna hijau
            if (menuItem.itemId == selectedItemId) {
                menuItem.iconTintList = ContextCompat.getColorStateList(this, R.color.green)
            } else {
                // Jika item tidak dipilih, beri warna abu-abu
                menuItem.iconTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
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
                    initialX = view.x
                    initialY = view.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f)
                    val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f)
                    AnimatorSet().apply {
                        playTogether(scaleDownX, scaleDownY)
                        duration = 150
                        start()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    view.x = initialX + dx
                    view.y = initialY + dy
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f)
                    val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f)
                    AnimatorSet().apply {
                        playTogether(scaleUpX, scaleUpY)
                        duration = 150
                        interpolator = OvershootInterpolator()
                        start()
                    }

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
            val rotateOut = ObjectAnimator.ofFloat(fabChatbot, "rotation", 0f, 360f)
            val scaleOutX = ObjectAnimator.ofFloat(fabChatbot, "scaleX", 1f, 0f)
            val scaleOutY = ObjectAnimator.ofFloat(fabChatbot, "scaleY", 1f, 0f)
            val fadeOut = ObjectAnimator.ofFloat(fabChatbot, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(rotateOut, scaleOutX, scaleOutY, fadeOut)
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        replaceFragment(ChatbotFragment())
                        resetFabAnimation() // Reset FAB setelah fragment diganti
                    }
                })
                start()
            }
        }
    }

    private fun resetFabAnimation() {
        fabChatbot.rotation = 0f
        fabChatbot.scaleX = 1f
        fabChatbot.scaleY = 1f
        fabChatbot.alpha = 1f
    }

    private fun setInitialFragment() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(OrderFragment(), false)
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true): Boolean {
        // Set visibilitas bottom nav dan FAB berdasarkan fragment yang ditampilkan
        setNavigationVisibility(fragment !is ChatbotFragment)

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }

    fun hideNavigationElements() {
        bottomNavigation.visibility = View.GONE
        fabCamera.visibility = View.GONE
        fabChatbot.visibility = View.GONE
    }

    override fun setNavigationVisibility(visible: Boolean) {
        bottomNavigation.visibility = if (visible) View.VISIBLE else View.GONE
        fabCamera.visibility = if (visible) View.VISIBLE else View.GONE
        fabChatbot.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        // Set visibilitas bottom nav dan FAB kembali sebelum pop back stack
        if (supportFragmentManager.backStackEntryCount > 1 && supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name == null) {
            val previousFragment = supportFragmentManager.fragments.lastOrNull()
            setNavigationVisibility(previousFragment !is ChatbotFragment)
        } else if (supportFragmentManager.backStackEntryCount == 1) {
            setNavigationVisibility(true) // Jika kembali dari Chatbot ke fragment awal
        }
        super.onBackPressed()
    }
}
