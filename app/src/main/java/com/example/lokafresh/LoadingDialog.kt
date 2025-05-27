package com.example.lokafresh

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView

class LoadingDialog(context: Context) {

    private val dialog = Dialog(context)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun show(message: String = "Loading...") {
        val tv = dialog.findViewById<TextView>(R.id.loadingText)
        tv.text = message
        dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
