package com.example.lokafresh

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.lokafresh.response.User

class EditUserDialogFragment(private val user: User) : DialogFragment() {

    private var onSaveListener: ((User) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_edit_user_dialog, null)

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)

        // Set existing values
        etUsername.setText(user.username)
        etFullName.setText(user.fullname)
        etEmail.setText(user.email)

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit User")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val updatedUser = User(
                    etUsername.text.toString(),
                    user.password, // Don't change password
                    etFullName.text.toString(),
                    etEmail.text.toString()
                )

                if (TextUtils.isEmpty(updatedUser.username) ||
                    TextUtils.isEmpty(updatedUser.fullname) ||
                    TextUtils.isEmpty(updatedUser.email)
                ) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    onSaveListener?.invoke(updatedUser)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    fun setOnSaveListener(listener: (User) -> Unit) {
        onSaveListener = listener
    }
}
