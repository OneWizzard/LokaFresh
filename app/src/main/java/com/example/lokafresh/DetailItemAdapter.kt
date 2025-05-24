package com.example.lokafresh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.ItemData


class DetailItemAdapter(private val items: List<ItemData>) : RecyclerView.Adapter<DetailItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaItem)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)
        val tvUnitPrice: TextView = itemView.findViewById(R.id.tvUnitPrice)
        val tvUnitMetrics: TextView = itemView.findViewById(R.id.tvUnitMetrics)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvNama.text =  "Nama: ${item.name}"
        holder.tvQuantity.text = "Qty: ${item.quantity}"
        holder.tvTotalPrice.text = "Total Price: ${item.total_price}"
        holder.tvWeight.text = "Weight: ${item.weight}"
        holder.tvUnitPrice.text = "Unit Price: ${item.unit_price}"
        holder.tvUnitMetrics.text = "Unit: ${item.unit_metrics}"
    }

    override fun getItemCount(): Int = items.size
}
