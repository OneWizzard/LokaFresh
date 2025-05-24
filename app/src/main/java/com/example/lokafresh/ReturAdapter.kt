package com.example.lokafresh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.databinding.ItemReturBinding

class ReturAdapter(
    private val items: List<ReturItem>
) : RecyclerView.Adapter<ReturAdapter.ReturViewHolder>() {

    inner class ReturViewHolder(val binding: ItemReturBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReturViewHolder {
        val binding = ItemReturBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReturViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReturViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvName.text = item.name
            etQuantity.setText(item.quantity.toString())
            etReturn.setText(item.returnQty.toString())
            tvUnitPrice.text = item.unitPrice.toString()
            tvTotal.text = (item.unitPrice * item.quantity).toString()

            // Update returnQty saat user input
            etReturn.doAfterTextChanged {
                item.returnQty = it.toString().toDoubleOrNull() ?: 0.0
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getUpdatedItems(): List<ReturItem> = items
}