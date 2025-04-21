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

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)

        btnLogin.setOnClickListener {
            val usernameInput = emailEditText.text.toString().trim()
            val passwordInput = passwordEditText.text.toString().trim()

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiService = ApiConfig.getApiService()
            apiService.getUserData().enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val userList = response.body()
                        val user = userList?.find { it.username == usernameInput && it.password == passwordInput }

                        if (user != null) {
                            Toast.makeText(this@Login, "Login berhasil! Selamat datang, ${user.fullname}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "Username atau password salah", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Login, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(this@Login, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        val txviewRegisterLink = findViewById<TextView>(R.id.registerLink)
        txviewRegisterLink.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }
}
