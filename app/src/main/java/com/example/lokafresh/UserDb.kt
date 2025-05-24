package com.example.lokafresh

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.User
import com.example.lokafresh.retrofit.ApiConfig
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDb : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val apiService = ApiConfig.getApiService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_db, container, false)
        recyclerView = view.findViewById(R.id.rvUsers)
        recyclerView.layoutManager = LinearLayoutManager(context)

        userAdapter = UserAdapter(emptyList(),
            onDeleteClick = { user -> confirmDeleteUser(user.username) },
            onEditClick = { user -> showEditDialog(user) }
        )

        recyclerView.adapter = userAdapter

        loadUsers()

        val fabAddUser: FloatingActionButton = view.findViewById(R.id.fabAddUser)
        fabAddUser.setOnClickListener {
            val intent = Intent(requireContext(), Register::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadUsers() {
        apiService.getUserData().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    userAdapter.updateData(users)
                } else {
                    Toast.makeText(context, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(context, "Gagal mengambil data user: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmDeleteUser(username: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus User")
            .setMessage("Apakah Anda yakin ingin menghapus user $username?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteUser(username)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteUser(username: String) {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Menghapus user...")
            setCancelable(false)
            show()
        }

        apiService.deleteUser(username).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(context, "User berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(context, "Gagal hapus user: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(context, "Gagal hapus user: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(user: User) {
        val dialog = EditUserDialogFragment(user)
        dialog.setOnSaveListener { updatedUser -> updateUser(updatedUser) }
        dialog.show(childFragmentManager, "editDialog")
    }

    private fun updateUser(user: User) {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Memperbarui user...")
            setCancelable(false)
            show()
        }

        apiService.updateUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(context, "User berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(context, "Gagal update user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(context, "Gagal update user: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
