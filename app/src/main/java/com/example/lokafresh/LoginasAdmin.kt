package com.example.lokafresh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginAsAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginas_admin) //

        val edtUsername = findViewById<EditText>(R.id.username)
        val edtPassword = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username == "Admin" && password == "123") {
                Toast.makeText(this, "Login Admin Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminDashboard::class.java) //
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Username atau Password Admin salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
