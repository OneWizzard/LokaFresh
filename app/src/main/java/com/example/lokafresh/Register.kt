package com.example.lokafresh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lokafresh.response.User
import com.example.lokafresh.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Register : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFullname: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.username)
        etPassword = findViewById(R.id.password)
        etConfirmPassword = findViewById(R.id.confirm_password)
        etEmail = findViewById(R.id.email)
        etFullname = findViewById(R.id.fullname)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            registerUser()
        }

        val txBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)
        txBackToLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val fullname = etFullname.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || fullname.isEmpty()) {
            Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password harus minimal 8 karakter, mengandung huruf besar, huruf kecil, angka, dan karakter spesial.", Toast.LENGTH_LONG).show()
            return
        }

        val hashedPassword = hashPassword(password)

        val user = User(
            username = username,
            password = hashedPassword,
            fullname = fullname,
            email = email // pastikan model User kamu ada field email juga!
        )

        val client = ApiConfig.getApiService().createUser(user)
        client.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Register, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Register, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Register, "Registrasi Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@Register, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashedBytes = digest.digest(password.toByteArray())
            bytesToHex(hashedBytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in bytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun isPasswordValid(password: String): Boolean {
        // Harus ada 1 huruf kecil, 1 huruf besar, 1 angka, 1 special character, minimal 8 karakter
        val passwordPattern = """^(?=\S{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#\$%^&*()_\-+=\[{\]};:'",<.>/?\\|`~])[A-Za-z\d!@#\$%^&*()_\-+=\[{\]};:'",<.>/?\\|`~]+$"""
        return password.matches(passwordPattern.toRegex())
    }
}
