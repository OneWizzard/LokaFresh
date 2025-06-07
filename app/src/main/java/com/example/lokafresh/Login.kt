package com.example.lokafresh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lokafresh.response.User
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.log

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        // Jika user sudah login sebelumnya
        if (savedUsername != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val tvLoginAdmin = findViewById<TextView>(R.id.tv_loginadmin)
        tvLoginAdmin.setOnClickListener {
            val intent = Intent(this, LoginAsAdmin::class.java)
            startActivity(intent)
        }


        btnLogin.setOnClickListener {
            val usernameInput = usernameEditText.text.toString().trim()
            val passwordInput = passwordEditText.text.toString().trim()

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Hash the password using SHA-256
            val hashedPassword = hashPassword(passwordInput)

            val apiService = ApiConfig.getApiService()
            apiService.getUserData().enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val userList = response.body()
                        val user =
                            userList?.find { it.username == usernameInput && it.password == hashedPassword }

                        if (user != null) {
                            val editor = sharedPreferences.edit()
                            editor.putString("username", user.username)
                            editor.putString("fullname", user.fullname)
                            editor.apply()
                            Toast.makeText(
                                this@Login,
                                "Login berhasil! Selamat datang, ${user.fullname}",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@Login,
                                "Username atau password salah",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@Login,
                            "Gagal mengambil data pengguna",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(
                        this@Login,
                        "Terjadi kesalahan: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }

    // Function to hash the password using SHA-256
    fun hashPassword(password: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashedBytes = digest.digest(password.toByteArray())
            return bytesToHex(hashedBytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return ""
        }
    }

    // Convert bytes to Hex
    fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in bytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}