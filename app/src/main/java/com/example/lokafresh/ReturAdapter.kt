package com.example.lokafresh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.databinding.ItemReturBinding
import java.text.NumberFormat
import java.util.Locale

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
            tvQuantity.text = item.quantity.toString()
            etReturn.setText(item.returnQty.toString())
            tvUnitPrice.text = formatCurrency(item.unitPrice)

            val totalHarga = (item.quantity - item.returnQty) * item.unitPrice
            tvTotal.text = formatCurrency(totalHarga)

            etReturn.doAfterTextChanged {
                val inputQty = it.toString().toDoubleOrNull() ?: 0.0
                item.returnQty = inputQty

                // Update total saat return diubah
                val newTotal = (item.quantity - inputQty) * item.unitPrice
                tvTotal.text = formatCurrency(newTotal)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getUpdatedItems(): List<ReturItem> = items

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    private fun formatCurrency(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }
}
